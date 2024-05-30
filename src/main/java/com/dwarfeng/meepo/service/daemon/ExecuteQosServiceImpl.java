package com.dwarfeng.meepo.service.daemon;

import com.dwarfeng.meepo.bean.dto.ExecuteInfo;
import com.dwarfeng.meepo.handler.daemon.ExecuteHandler;
import com.dwarfeng.meepo.handler.daemon.ExecuteInfoHandler;
import com.dwarfeng.subgrade.sdk.exception.ServiceExceptionHelper;
import com.dwarfeng.subgrade.stack.exception.HandlerException;
import com.dwarfeng.subgrade.stack.exception.ServiceException;
import com.dwarfeng.subgrade.stack.exception.ServiceExceptionMapper;
import com.dwarfeng.subgrade.stack.log.LogLevel;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Service
public class ExecuteQosServiceImpl implements ExecuteQosService {

    private final ExecuteHandler executeHandler;
    private final ExecuteInfoHandler executeInfoHandler;

    private final ServiceExceptionMapper sem;

    private final Lock lock = new ReentrantLock();

    public ExecuteQosServiceImpl(
            ExecuteHandler executeHandler, ExecuteInfoHandler executeInfoHandler, ServiceExceptionMapper sem
    ) {
        this.executeHandler = executeHandler;
        this.executeInfoHandler = executeInfoHandler;
        this.sem = sem;
    }

    @Override
    public boolean isStarted() throws ServiceException {
        lock.lock();
        try {
            return executeHandler.isStarted();
        } catch (HandlerException e) {
            throw ServiceExceptionHelper.logParse("判断执行服务是否启动时发生异常", LogLevel.WARN, e, sem);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void start() throws ServiceException {
        lock.lock();
        try {
            executeHandler.start();
        } catch (HandlerException e) {
            throw ServiceExceptionHelper.logParse("启动执行服务时发生异常", LogLevel.WARN, e, sem);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void stop() throws ServiceException {
        lock.lock();
        try {
            executeHandler.stop();
        } catch (HandlerException e) {
            throw ServiceExceptionHelper.logParse("停止执行服务时发生异常", LogLevel.WARN, e, sem);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void execute(String id) throws ServiceException {
        lock.lock();
        try {
            executeHandler.execute(id);
        } catch (HandlerException e) {
            throw ServiceExceptionHelper.logParse("执行指定任务时发生异常", LogLevel.WARN, e, sem);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public List<ExecuteInfo> getExecuteInfos() throws ServiceException {
        lock.lock();
        try {
            return executeInfoHandler.getExecuteInfos();
        } catch (HandlerException e) {
            throw ServiceExceptionHelper.logParse("获取执行信息时发生异常", LogLevel.WARN, e, sem);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void reload() throws ServiceException {
        lock.lock();
        try {
            boolean started = executeHandler.isStarted();
            executeHandler.stop();
            executeInfoHandler.reload();
            if (started) {
                executeHandler.start();
            }
        } catch (HandlerException e) {
            throw ServiceExceptionHelper.logParse("重新加载执行信息时发生异常", LogLevel.WARN, e, sem);
        } finally {
            lock.unlock();
        }
    }
}
