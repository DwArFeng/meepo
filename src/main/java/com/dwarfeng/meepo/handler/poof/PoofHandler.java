package com.dwarfeng.meepo.handler.poof;

import com.dwarfeng.subgrade.stack.exception.HandlerException;
import com.dwarfeng.subgrade.stack.handler.Handler;

/**
 * Poof 发送处理器。
 *
 * @author DwArFeng
 * @since 1.0.0
 */
public interface PoofHandler extends Handler {

    /**
     * 向指定的主机和端口发送一个 Poof 消息。
     *
     * @param host 指定的主机。
     * @param port 指定的端口。
     * @param id   指定的 ID。
     * @return Poof 消息的返回值。
     * @throws HandlerException 处理器异常。
     */
    int poof(String host, int port, String id) throws HandlerException;
}
