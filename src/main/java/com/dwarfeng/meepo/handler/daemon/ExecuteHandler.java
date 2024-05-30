package com.dwarfeng.meepo.handler.daemon;

import com.dwarfeng.meepo.bean.dto.ExecuteResult;
import com.dwarfeng.subgrade.stack.exception.HandlerException;
import com.dwarfeng.subgrade.stack.handler.StartableHandler;

import java.util.concurrent.CompletableFuture;

/**
 * 执行处理器。
 *
 * @author DwArFeng
 * @since 1.0.0
 */
public interface ExecuteHandler extends StartableHandler {

    /**
     * 执行指定的任务。
     *
     * @param id 指定的任务的 ID。
     * @throws HandlerException 处理器异常。
     */
    void execute(String id) throws HandlerException;

    /**
     * 异步执行指定的任务。
     *
     * @param id 指定的任务的 ID。
     * @return 异步执行的结果。
     * @throws HandlerException 处理器异常。
     */
    CompletableFuture<ExecuteResult> executeAsync(String id) throws HandlerException;
}
