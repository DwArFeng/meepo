package com.dwarfeng.meepo.execption;

import com.dwarfeng.subgrade.stack.exception.HandlerException;

/**
 * 执行处理器已经停止异常。
 *
 * @author DwArFeng
 * @since 1.0.0
 */
public class ExecuteHandlerStoppedException extends HandlerException {

    private static final long serialVersionUID = -1011458457238464332L;

    public ExecuteHandlerStoppedException() {
    }

    public ExecuteHandlerStoppedException(Throwable cause) {
        super(cause);
    }

    @Override
    public String getMessage() {
        return "执行处理器已经停止";
    }
}
