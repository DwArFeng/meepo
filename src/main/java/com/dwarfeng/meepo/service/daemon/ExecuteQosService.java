package com.dwarfeng.meepo.service.daemon;

import com.dwarfeng.meepo.bean.dto.ExecuteInfo;
import com.dwarfeng.subgrade.stack.exception.ServiceException;
import com.dwarfeng.subgrade.stack.service.Service;

import java.util.List;

/**
 * 执行 QOS 服务。
 *
 * @author DwArFeng
 * @since 1.0.0
 */
public interface ExecuteQosService extends Service {

    /**
     * 执行服务是否启动。
     *
     * @return 执行服务是否启动。
     * @throws ServiceException 服务异常。
     */
    boolean isStarted() throws ServiceException;

    /**
     * 执行服务启动。
     *
     * @throws ServiceException 服务异常。
     */
    void start() throws ServiceException;

    /**
     * 执行服务停止。
     *
     * @throws ServiceException 服务异常。
     */
    void stop() throws ServiceException;

    /**
     * 执行指定的任务。
     *
     * @param id 指定的任务的 ID。
     * @throws ServiceException 服务异常。
     */
    void execute(String id) throws ServiceException;

    /**
     * 获取执行信息。
     *
     * @return 执行信息组成的列表。
     * @throws ServiceException 服务异常。
     */
    List<ExecuteInfo> getExecuteInfos() throws ServiceException;

    /**
     * 重新加载执行信息。
     *
     * @throws ServiceException 服务异常。
     */
    void reload() throws ServiceException;
}
