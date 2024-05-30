package com.dwarfeng.meepo.handler.daemon;

import com.dwarfeng.meepo.bean.dto.ExecuteResult;
import com.dwarfeng.meepo.util.Constants;
import com.dwarfeng.subgrade.sdk.exception.HandlerExceptionHelper;
import com.dwarfeng.subgrade.stack.exception.HandlerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Component
public class ArrivalHandlerImpl implements ArrivalHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExecuteHandlerImpl.class);

    private final ApplicationContext ctx;

    private final ExecuteHandler executeHandler;

    private final ThreadPoolTaskExecutor executor;
    private final ThreadPoolTaskScheduler scheduler;

    private final Lock lock = new ReentrantLock();
    private final Condition condition = lock.newCondition();
    private final Set<String> workerHandleSet = new HashSet<>();

    private boolean startFlag;

    private ServerSocket serverSocket;

    @Value("#{${daemon.arrival.server_socket_port:T(com.dwarfeng.meepo.util.Constants).ARRIVAL_DEFAULT_PORT}}")
    private int serverSocketPort;

    public ArrivalHandlerImpl(
            ApplicationContext ctx,
            ExecuteHandler executeHandler,
            ThreadPoolTaskExecutor executor,
            ThreadPoolTaskScheduler scheduler
    ) {
        this.ctx = ctx;
        this.executeHandler = executeHandler;
        this.executor = executor;
        this.scheduler = scheduler;
    }

    @PreDestroy
    public void dispose() throws Exception {
        lock.lock();
        try {
            if (!startFlag) {
                return;
            }
            doStop();
            startFlag = false;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public boolean isStarted() {
        lock.lock();
        try {
            return startFlag;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void start() throws HandlerException {
        lock.lock();
        try {
            if (startFlag) {
                return;
            }
            doStart();
            startFlag = true;
        } catch (Exception e) {
            throw HandlerExceptionHelper.parse(e);
        } finally {
            lock.unlock();
        }
    }

    private void doStart() throws Exception {
        // 日志记录。
        LOGGER.info("Arrival 处理器启动...");
        // 开启服务器套接字。
        serverSocket = new ServerSocket(serverSocketPort);
        // 开启 Boss 线程。
        BossTask bossTask = ctx.getBean(BossTask.class, this, serverSocket);
        executor.execute(bossTask);
    }

    @Override
    public void stop() throws HandlerException {
        lock.lock();
        try {
            if (!startFlag) {
                return;
            }
            doStop();
            startFlag = false;
        } catch (Exception e) {
            throw HandlerExceptionHelper.parse(e);
        } finally {
            lock.unlock();
        }
    }

    private void doStop() throws Exception {
        // 日志记录。
        LOGGER.info("Arrival 处理器停止...");
        // 关闭服务器套接字。
        if (serverSocket != null && !serverSocket.isClosed()) {
            serverSocket.close();
            serverSocket = null;
        }
        // 在阻塞的时间内，定期发送日志提醒操作员不要强行关闭程序。
        ScheduledFuture<?> future = scheduler.scheduleWithFixedDelay(
                () -> LOGGER.info("仍有执行中的 arrival 任务, 请耐心等待"),
                new Date(System.currentTimeMillis() + 1000),
                1000
        );
        // 阻塞，直到所有 arrival 任务执行完毕。
        while (!workerHandleSet.isEmpty()) {
            try {
                condition.await();
            } catch (InterruptedException ignored) {
            }
        }
        // 取消定时发送日志提醒。
        future.cancel(true);
    }

    @Component
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public class BossTask implements Runnable {

        private final ServerSocket serverSocket;

        // 该方法不是用于注入的，因此关闭警告。
        @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
        public BossTask(ServerSocket serverSocket) {
            this.serverSocket = serverSocket;
        }

        @Override
        public void run() {
            try {
                doRun();
            } catch (Exception e) {
                LOGGER.error("Boss 线程发生异常, 异常信息如下: ", e);
            }
        }

        private void doRun() {
            while (!serverSocket.isClosed()) {
                try {
                    // 接受新的连接
                    Socket clientSocket = serverSocket.accept();
                    // 对每个连接启动一个新的线程来处理业务
                    LOGGER.info("接受到新的连接，处理中...");
                    String workerHandle = UUID.randomUUID().toString();
                    WorkerTask workerTask = ctx.getBean(
                            WorkerTask.class, ArrivalHandlerImpl.this, clientSocket, workerHandle
                    );
                    // 将 workerHandle 添加到 workerHandleSet 中。
                    lock.lock();
                    try {
                        workerHandleSet.add(workerHandle);
                    } finally {
                        lock.unlock();
                    }
                    // 执行任务。
                    executor.execute(workerTask);
                } catch (IOException e) {
                    if (serverSocket.isClosed()) {
                        LOGGER.info("ServerSocket 已关闭，停止接受新的连接");
                    } else {
                        LOGGER.error("接受新的连接时发生错误", e);
                    }
                }
            }
        }
    }

    @Component
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public class WorkerTask implements Runnable {

        private static final int SOCKET_IN_BUFFER_SIZE = 4096;

        private final Socket clientSocket;
        private final String workerHandle;

        // 该方法不是用于注入的，因此关闭警告。
        @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
        public WorkerTask(Socket clientSocket, String workerHandle) {
            this.clientSocket = clientSocket;
            this.workerHandle = workerHandle;
        }

        @Override
        public void run() {
            try {
                doRun();
            } catch (Exception e) {
                LOGGER.warn("处理连接时发生异常, 异常信息如下: ", e);
            } finally {
                // 关闭连接。
                closeClientSocket();
                // 从 workerHandleSet 中移除 workerHandle。
                removeHandle();
            }
        }

        private void doRun() throws Exception {
            // 从 socketIn 中读取单行文本，作为执行器的 ID。
            String executorId = readContent(clientSocket.getInputStream());

            // 执行任务，并获取执行结果。
            ExecuteResult executeResult = null;
            Exception executeException = null;
            try {
                executeResult = executeHandler.executeAsync(executorId).get();
            } catch (Exception e) {
                if (e instanceof CompletionException) {
                    executeException = (Exception) e.getCause();
                } else {
                    executeException = e;
                }
            }

            // 如果 executeException 不是 null，则记录异常信息。
            if (Objects.nonNull(executeException)) {
                LOGGER.warn("执行任务时发生异常, 异常信息如下: ", executeException);
            }

            // 将 executeResult 转换为返回代码，并返回。
            String executeResultCode = convertExecuteResultToCode(executeResult, executeException);

            // 将返回代码写入 socketOut。
            writeContent(clientSocket.getOutputStream(), executeResultCode);
        }

        @SuppressWarnings("DuplicatedCode")
        private String readContent(InputStream socketIn) throws Exception {
            Reader reader = new InputStreamReader(socketIn, Constants.CHARSET);
            StringBuilder stringBuilder = new StringBuilder();
            char[] buffer = new char[SOCKET_IN_BUFFER_SIZE];
            int len;
            while ((len = reader.read(buffer)) != -1) {
                stringBuilder.append(buffer, 0, len);
                if (stringBuilder.indexOf(Constants.LINE_SEPARATOR) != -1) {
                    break;
                }
            }
            return stringBuilder.toString().trim();
        }

        private String convertExecuteResultToCode(ExecuteResult executeResult, Exception executeException) {
            // 如果没有异常。
            if (Objects.nonNull(executeResult)) {
                // 如果 !conditionPassed。
                if (!executeResult.isConditionPassed()) {
                    return Constants.POOF_CODE_CONDITION_NOT_PASSED;
                }
                // 如果 executeResult 中的所有指令均执行成功。
                else if (executeResult.getFailedCommandIds().isEmpty()) {
                    return Constants.POOF_CODE_SUCCESS;
                } else {
                    // 如果 executeResult 中至少有一条指令执行失败。
                    return Constants.POOF_CODE_FAILED;
                }
            }
            // 如果有异常。
            else if (Objects.nonNull(executeException)) {
                return Constants.POOF_CODE_EXCEPTION;
            }
            // 如果不属于上述情况。
            else {
                return Constants.POOF_CODE_UNKNOWN;
            }
        }

        private void writeContent(OutputStream socketOut, String content) throws Exception {
            Writer writer = new OutputStreamWriter(socketOut, Constants.CHARSET);
            writer.write(content + Constants.LINE_SEPARATOR);
            writer.flush();
        }

        private void closeClientSocket() {
            try {
                clientSocket.close();
            } catch (IOException e) {
                LOGGER.error("关闭连接时发生异常, 异常信息如下: ", e);
            }
        }

        private void removeHandle() {
            lock.lock();
            try {
                workerHandleSet.remove(workerHandle);
                condition.signalAll();
            } finally {
                lock.unlock();
            }
        }
    }
}
