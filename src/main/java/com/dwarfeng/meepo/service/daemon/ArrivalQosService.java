package com.dwarfeng.meepo.service.daemon;

import com.dwarfeng.subgrade.stack.exception.ServiceException;
import com.dwarfeng.subgrade.stack.service.Service;

/**
 * Arrival QOS 服务。
 *
 * @author DwArFeng
 * @since 1.0.0
 */
public interface ArrivalQosService extends Service {

    /**
     * Arrival 服务是否启动。
     *
     * @return Arrival 服务是否启动。
     * @throws ServiceException 服务异常。
     */
    boolean isStarted() throws ServiceException;

    /**
     * Arrival 服务启动。
     *
     * @throws ServiceException 服务异常。
     */
    void start() throws ServiceException;

    /**
     * Arrival 服务停止。
     *
     * @throws ServiceException 服务异常。
     */
    void stop() throws ServiceException;
}
