package com.dwarfeng.meepo.handler.daemon;

import com.dwarfeng.meepo.bean.dto.ExecuteInfo;
import com.dwarfeng.subgrade.stack.exception.HandlerException;
import com.dwarfeng.subgrade.stack.handler.Handler;

import java.util.List;

/**
 * 执行信息处理器。
 *
 * @author DwArFeng
 * @since 1.0.0
 */
public interface ExecuteInfoHandler extends Handler {

    /**
     * 获取执行信息。
     *
     * @return 执行信息组成的列表。
     * @throws HandlerException 处理器异常。
     */
    List<ExecuteInfo> getExecuteInfos() throws HandlerException;

    /**
     * 获取指定的 ID 对应的执行信息。
     *
     * @param id 指定的 ID。
     * @return 指定的 ID 对应的执行信息。
     * @throws HandlerException 处理器异常。
     */
    ExecuteInfo getExecuteInfo(String id) throws HandlerException;

    /**
     * 重新加载执行信息。
     *
     * @throws HandlerException 处理器异常。
     */
    void reload() throws HandlerException;
}
