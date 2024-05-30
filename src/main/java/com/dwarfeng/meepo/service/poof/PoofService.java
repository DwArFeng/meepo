package com.dwarfeng.meepo.service.poof;

import com.dwarfeng.subgrade.stack.exception.ServiceException;
import com.dwarfeng.subgrade.stack.service.Service;

/**
 * Poof 服务。
 *
 * @author DwArFeng
 * @since 1.0.0
 */
public interface PoofService extends Service {

    /**
     * 向指定的主机和端口发送一个 Poof 消息。
     *
     * @param host 指定的主机。
     * @param port 指定的端口。
     * @param id   指定的 ID。
     * @return Poof 消息的返回值。
     * @throws ServiceException 服务异常。
     */
    int poof(String host, int port, String id) throws ServiceException;
}
