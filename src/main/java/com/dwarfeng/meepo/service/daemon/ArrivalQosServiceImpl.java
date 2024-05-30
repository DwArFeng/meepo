package com.dwarfeng.meepo.service.daemon;

import com.dwarfeng.meepo.handler.daemon.ArrivalHandler;
import com.dwarfeng.subgrade.sdk.exception.ServiceExceptionHelper;
import com.dwarfeng.subgrade.stack.exception.HandlerException;
import com.dwarfeng.subgrade.stack.exception.ServiceException;
import com.dwarfeng.subgrade.stack.exception.ServiceExceptionMapper;
import com.dwarfeng.subgrade.stack.log.LogLevel;
import org.springframework.stereotype.Service;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Service
public class ArrivalQosServiceImpl implements ArrivalQosService {

    private final ArrivalHandler arrivalHandler;

    private final ServiceExceptionMapper sem;

    private final Lock lock = new ReentrantLock();

    public ArrivalQosServiceImpl(
            ArrivalHandler arrivalHandler, ServiceExceptionMapper sem
    ) {
        this.arrivalHandler = arrivalHandler;
        this.sem = sem;
    }

    @Override
    public boolean isStarted() throws ServiceException {
        lock.lock();
        try {
            return arrivalHandler.isStarted();
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
            arrivalHandler.start();
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
            arrivalHandler.stop();
        } catch (HandlerException e) {
            throw ServiceExceptionHelper.logParse("停止执行服务时发生异常", LogLevel.WARN, e, sem);
        } finally {
            lock.unlock();
        }
    }
}
