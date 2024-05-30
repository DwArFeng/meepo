package com.dwarfeng.meepo.handler.daemon;

import com.dwarfeng.meepo.bean.dto.ExecuteInfo;
import com.dwarfeng.meepo.bean.dto.ExecuteResult;
import com.dwarfeng.meepo.execption.ExecuteHandlerStoppedException;
import com.dwarfeng.meepo.util.Constants;
import com.dwarfeng.subgrade.sdk.exception.HandlerExceptionHelper;
import com.dwarfeng.subgrade.sdk.interceptor.analyse.BehaviorAnalyse;
import com.dwarfeng.subgrade.stack.exception.HandlerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.ParserContext;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.io.File;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

@Component
public class ExecuteHandlerImpl implements ExecuteHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExecuteHandlerImpl.class);

    private final ApplicationContext ctx;

    private final ExecuteInfoHandler executeInfoHandler;

    private final HandlerValidator handlerValidator;

    private final ThreadPoolTaskExecutor executor;
    private final ThreadPoolTaskScheduler scheduler;

    private final ExpressionParser expressionParser;
    private final ParserContext parserContext;

    private final Lock lock = new ReentrantLock();
    private final Condition condition = lock.newCondition();
    private final Set<String> executorHandleSet = new HashSet<>();

    private boolean startFlag;

    public ExecuteHandlerImpl(
            ApplicationContext ctx,
            ExecuteInfoHandler executeInfoHandler,
            HandlerValidator handlerValidator,
            ThreadPoolTaskExecutor executor,
            ThreadPoolTaskScheduler scheduler,
            ExpressionParser expressionParser,
            ParserContext parserContext
    ) {
        this.ctx = ctx;
        this.executeInfoHandler = executeInfoHandler;
        this.handlerValidator = handlerValidator;
        this.executor = executor;
        this.scheduler = scheduler;
        this.expressionParser = expressionParser;
        this.parserContext = parserContext;
    }

    @PreDestroy
    public void dispose() {
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

    private void doStart() {
        LOGGER.info("执行处理器启动...");
    }

    @Override
    public void stop() {
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

    private void doStop() {
        LOGGER.info("执行处理器停止...");
        // 在阻塞的时间内，定期发送日志提醒操作员不要强行关闭程序。
        ScheduledFuture<?> future = scheduler.scheduleWithFixedDelay(
                () -> LOGGER.info("仍有执行中的执行任务, 请耐心等待"),
                new Date(System.currentTimeMillis() + 1000),
                1000
        );
        // 阻塞，直到所有执行器执行完毕。
        while (!executorHandleSet.isEmpty()) {
            try {
                condition.await();
            } catch (InterruptedException ignored) {
            }
        }
        // 取消定时发送日志提醒。
        future.cancel(true);
    }

    @SuppressWarnings("DuplicatedCode")
    @BehaviorAnalyse
    @Override
    public void execute(String id) throws HandlerException {
        // 验证 ID 对应的执行信息是否存在。
        handlerValidator.makeSureExecuteInfoExists(id);
        // 执行任务。
        lock.lock();
        try {
            // 如果执行处理器未启动，则抛出异常。
            if (!startFlag) {
                throw new ExecuteHandlerStoppedException();
            }
            // 从执行信息处理器中获取执行信息。
            ExecuteInfo executeInfo = executeInfoHandler.getExecuteInfo(id);
            // 创建执行任务。
            String executorHandle = UUID.randomUUID().toString();
            ExecutorTask executorTask = ctx.getBean(ExecutorTask.class, this, executeInfo, executorHandle);
            // 将 executorHandle 添加到 executorHandleSet 中。
            executorHandleSet.add(executorHandle);
            // 执行任务。
            executor.execute(executorTask);
        } catch (Exception e) {
            throw HandlerExceptionHelper.parse(e);
        } finally {
            lock.unlock();
        }
    }

    @SuppressWarnings("DuplicatedCode")
    @Override
    public CompletableFuture<ExecuteResult> executeAsync(String id) throws HandlerException {
        // 验证 ID 对应的执行信息是否存在。
        handlerValidator.makeSureExecuteInfoExists(id);
        // 执行任务。
        lock.lock();
        try {
            // 如果执行处理器未启动，则抛出异常。
            if (!startFlag) {
                throw new ExecuteHandlerStoppedException();
            }
            // 从执行信息处理器中获取执行信息。
            ExecuteInfo executeInfo = executeInfoHandler.getExecuteInfo(id);
            // 创建执行任务。
            String executorHandle = UUID.randomUUID().toString();
            ExecutorTask executorTask = ctx.getBean(ExecutorTask.class, this, executeInfo, executorHandle);
            // 将 executorHandle 添加到 executorHandleSet 中。
            executorHandleSet.add(executorHandle);
            // 执行任务。
            return CompletableFuture.supplyAsync(executorTask, executor);
        } catch (Exception e) {
            throw HandlerExceptionHelper.parse(e);
        } finally {
            lock.unlock();
        }
    }

    @Component
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public class ExecutorTask implements Runnable, Supplier<ExecuteResult> {

        private static final String COMMAND_PLACEHOLDER_PARAM_1 = "a1";
        private static final String COMMAND_PLACEHOLDER_PARAM_2 = "a2";
        private static final String COMMAND_PLACEHOLDER_PARAM_3 = "a3";
        private static final String COMMAND_PLACEHOLDER_PARAM_4 = "a4";
        private static final String COMMAND_PLACEHOLDER_PARAM_5 = "a5";
        private static final String COMMAND_PLACEHOLDER_PARAM_6 = "a6";

        private final ExecuteInfo executeInfo;
        private final String executeHandle;

        private final AtomicBoolean conditionPassedRef = new AtomicBoolean();
        private final List<String> executedCommandIds = new ArrayList<>();
        private final List<String> finishedCommandIds = new ArrayList<>();
        private final List<String> failedCommandIds = new ArrayList<>();

        // 该方法不是用于注入的，因此关闭警告。
        @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
        public ExecutorTask(ExecuteInfo executeInfo, String executeHandle) {
            this.executeInfo = executeInfo;
            this.executeHandle = executeHandle;
        }

        @BehaviorAnalyse
        @Override
        public void run() {
            try {
                doRun();
            } catch (Exception e) {
                String message = "执行任务 " + executeInfo.getId() + " 时发生异常, 任务中止, 异常信息如下:";
                LOGGER.warn(message, e);
            } finally {
                removeHandle();
            }
        }

        @BehaviorAnalyse
        @Override
        public ExecuteResult get() {
            try {
                doRun();
                return getResult();
            } catch (Exception e) {
                String message = "执行任务 " + executeInfo.getId() + " 时发生异常, 任务中止, 异常信息如下:";
                LOGGER.warn(message, e);
                throw new CompletionException(e);
            } finally {
                removeHandle();
            }
        }

        private ExecuteResult getResult() {
            return new ExecuteResult(
                    conditionPassedRef.get(),
                    executedCommandIds,
                    finishedCommandIds,
                    failedCommandIds
            );
        }

        private void removeHandle() {
            lock.lock();
            try {
                executorHandleSet.remove(executeHandle);
                condition.signalAll();
            } finally {
                lock.unlock();
            }
        }

        private void doRun() {
            // 展开参数。
            String id = executeInfo.getId();

            LOGGER.info("执行任务 {} 执行开始...", id);

            // 执行条件指令。
            if (!checkCondition()) {
                LOGGER.info("执行任务 {} 条件检查未通过, 任务中止", id);
                return;
            }

            // 执行模块指令。
            executeModules();
        }

        private boolean checkCondition() {
            // 展开参数。
            List<ExecuteInfo.CommandInfo> commandInfos = executeInfo.getConditionCommandInfos();
            // 定义参数映射。
            Map<String, String> argMap;
            // 对于每条条件指令，执行指令，如果所有指令返回 0，则返回 true，否则返回 false。
            for (ExecuteInfo.CommandInfo commandInfo : commandInfos) {
                // 如果通过条件指令，则继续执行。
                if (checkSingleCondition(commandInfo)) {
                    continue;
                }
                // 如果不通过条件执行，通知观察器条件未通过，并返回 false。
                argMap = new HashMap<>();
                argMap.put(COMMAND_PLACEHOLDER_PARAM_3, Constants.OBSERVER_EVENT_CONDITION_NOT_PASSED);
                argMap.put(COMMAND_PLACEHOLDER_PARAM_4, commandInfo.getId());
                fireObservers(argMap);
                // 设置条件未通过标志。
                conditionPassedRef.set(false);
                // 返回 false。
                return false;
            }
            // 如果所有条件指令都通过，通知观察器条件通过，并返回 true。
            argMap = new HashMap<>();
            argMap.put(COMMAND_PLACEHOLDER_PARAM_3, Constants.OBSERVER_EVENT_CONDITION_PASSED);
            fireObservers(argMap);
            // 设置条件通过标志。
            conditionPassedRef.set(true);
            // 返回 true。
            return true;
        }

        private boolean checkSingleCondition(ExecuteInfo.CommandInfo commandInfo) {
            // 展开参数。
            String id = commandInfo.getId();
            // 定义参数映射。
            Map<String, String> argMap;
            // 执行条件指令。
            try {
                // 构造参数映射。
                argMap = new HashMap<>();
                argMap.put(COMMAND_PLACEHOLDER_PARAM_1, executeInfo.getId());
                argMap.put(COMMAND_PLACEHOLDER_PARAM_2, id);
                // 执行条件指令。
                return runProcess(commandInfo, argMap, Constants.DIR_LIBCMD_CONDITION) == 0;
            } catch (Exception e) {
                String message = "检查条件指令时发生异常, 该条件指令将被视为未通过, 执行任务 ID: " +
                        executeInfo.getId() + ", 条件指令 ID: " + id + ", 异常信息如下:";
                LOGGER.warn(message, e);
                return false;
            }
        }

        private void executeModules() {
            // 展开参数。
            List<ExecuteInfo.CommandInfo> commandInfos = executeInfo.getModuleCommandInfos();
            boolean continueOnFailure = executeInfo.isContinueOnFailure();
            // 构造参数映射。
            Map<String, String> argMap = new HashMap<>();
            argMap.put(COMMAND_PLACEHOLDER_PARAM_3, Constants.OBSERVER_EVENT_MODULES_STARTED);
            // 通知观察器模块开始。
            fireObservers(argMap);
            // 对于每条模块指令，执行指令。
            for (ExecuteInfo.CommandInfo commandInfo : commandInfos) {
                boolean success = executeSingleModule(commandInfo);
                // 如果执行失败，根据 continueOnFailure 进行后续处理。
                if (!success && !continueOnFailure) {
                    String message = "执行任务模块执行失败, 且 continueOnFailure 为 false, 任务中止, " +
                            "执行任务 ID: {}, 模块指令 ID: {}";
                    LOGGER.info(message, executeInfo.getId(), commandInfo.getId());
                    break;
                }
            }
            // 构造参数映射。
            String executedCommandIdsArg = String.join(",", executedCommandIds);
            String finishedCommandIdsArg = String.join(",", finishedCommandIds);
            String failedCommandIdsArg = String.join(",", failedCommandIds);
            argMap = new HashMap<>();
            argMap.put(COMMAND_PLACEHOLDER_PARAM_3, Constants.OBSERVER_EVENT_MODULES_FINISHED);
            argMap.put(COMMAND_PLACEHOLDER_PARAM_4, executedCommandIdsArg);
            argMap.put(COMMAND_PLACEHOLDER_PARAM_5, finishedCommandIdsArg);
            argMap.put(COMMAND_PLACEHOLDER_PARAM_6, failedCommandIdsArg);
            // 通知观察器模块结束。
            fireObservers(argMap);
        }

        @SuppressWarnings("IfStatementWithIdenticalBranches")
        private boolean executeSingleModule(ExecuteInfo.CommandInfo commandInfo) {
            // 定义执行结果。
            boolean success;
            // 展开参数。
            String id = commandInfo.getId();
            // 构造参数映射。
            Map<String, String> argMap = new HashMap<>();
            argMap.put(COMMAND_PLACEHOLDER_PARAM_3, Constants.OBSERVER_EVENT_MODULE_STARTED);
            argMap.put(COMMAND_PLACEHOLDER_PARAM_4, id);
            // 通知观察器模块开始。
            fireObservers(argMap);
            // 执行模块指令。
            executedCommandIds.add(id);
            try {
                // 构造参数映射。
                argMap = new HashMap<>();
                argMap.put(COMMAND_PLACEHOLDER_PARAM_1, executeInfo.getId());
                argMap.put(COMMAND_PLACEHOLDER_PARAM_2, id);
                // 执行模块指令。
                success = runProcess(commandInfo, argMap, Constants.DIR_LIBCMD_MODULE) == 0;
            } catch (Exception e) {
                String message = "执行模块指令时发生异常, 该模块指令将被视为执行失败, 执行任务 ID: " +
                        executeInfo.getId() + ", 模块指令 ID: " + id + ", 异常信息如下:";
                LOGGER.warn(message, e);
                success = false;
            }
            // 根据执行结果，进行后续处理。
            if (success) {
                // 将模块指令 ID 加入已完成列表。
                finishedCommandIds.add(id);
                // 构造参数映射。
                argMap = new HashMap<>();
                argMap.put(COMMAND_PLACEHOLDER_PARAM_3, Constants.OBSERVER_EVENT_MODULE_FINISHED);
                argMap.put(COMMAND_PLACEHOLDER_PARAM_4, id);
                // 通知观察器模块结束。
                fireObservers(argMap);
            } else {
                // 将模块指令 ID 加入执行失败列表。
                failedCommandIds.add(id);
                // 构造参数映射。
                argMap = new HashMap<>();
                argMap.put(COMMAND_PLACEHOLDER_PARAM_3, Constants.OBSERVER_EVENT_MODULE_FAILED);
                argMap.put(COMMAND_PLACEHOLDER_PARAM_4, id);
                // 通知观察器模块失败。
                fireObservers(argMap);
            }
            // 返回执行结果。
            return success;
        }

        /**
         * 通知观察器。
         *
         * <p>
         * 需要注意的是，该方法执行时会向入口参数 <code>argMap</code> 增加额外的键值对，分别为：
         * <ul>
         *     <li>a1: 执行任务 ID。</li>
         *     <li>a2: 观察器 ID。</li>
         * </ul>
         * 因此，调用该方法时，不需要在入口参数 <code>argMap</code> 中增加这两个键值对。
         *
         * @param argMap 参数映射。
         */
        private void fireObservers(Map<String, String> argMap) {
            // 展开参数。
            List<ExecuteInfo.CommandInfo> commandInfos = executeInfo.getObserverCommandInfos();
            // 为 argMap 增加额外的键值对。
            argMap.put(COMMAND_PLACEHOLDER_PARAM_1, executeInfo.getId());
            // 对于每条观察器指令，执行指令。
            for (ExecuteInfo.CommandInfo commandInfo : commandInfos) {
                // 为 argMap 增加额外的键值对。
                argMap.put(COMMAND_PLACEHOLDER_PARAM_2, commandInfo.getId());
                fireSingleObserver(commandInfo, argMap);
            }
        }

        private void fireSingleObserver(ExecuteInfo.CommandInfo commandInfo, Map<String, String> argMap) {
            try {
                runProcess(commandInfo, argMap, Constants.DIR_LIBCMD_OBSERVER);
            } catch (Exception e) {
                String message = "执行观察器指令时发生异常, 该观察器指令将被忽略, 执行任务 ID: " +
                        executeInfo.getId() + ", 观察器指令 ID: " + commandInfo.getId() + ", 异常信息如下:";
                LOGGER.warn(message, e);
            }
        }

        private int runProcess(ExecuteInfo.CommandInfo commandInfo, Map<String, String> argMap, File dir) throws Exception {
            // 展开参数。
            String id = commandInfo.getId();
            String command = commandInfo.getCommand();
            // 使用 Spring EL 表达式解析参数。
            command = expressionParser.parseExpression(command, parserContext).getValue(argMap, String.class);
            // 执行指令。
            LOGGER.info("执行指令, ID:{}, 目录: {}, 指令: {}", id, dir.getPath(), command);
            Process process = Runtime.getRuntime().exec(command, null, dir);
            // 读取输出流，并打印日志。
            executor.execute(() -> readOutStream(process));
            // 读取错误流，并打印日志。
            executor.execute(() -> readErrStream(process));
            // 等待进程结束。
            int exitCode = process.waitFor();
            LOGGER.info("进程结束, 退出码: {}", exitCode);
            return exitCode;
        }

        private void readOutStream(Process process) {
            // 读取输出流，并打印日志。
            try (Scanner scanner = new Scanner(process.getInputStream())) {
                while (scanner.hasNextLine()) {
                    LOGGER.info(scanner.nextLine());
                }
            }
        }

        private void readErrStream(Process process) {
            // 读取错误流，并打印日志。
            try (Scanner scanner = new Scanner(process.getErrorStream())) {
                while (scanner.hasNextLine()) {
                    LOGGER.warn(scanner.nextLine());
                }
            }
        }
    }
}
