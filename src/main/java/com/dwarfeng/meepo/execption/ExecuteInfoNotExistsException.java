package com.dwarfeng.meepo.execption;

import com.dwarfeng.subgrade.stack.exception.HandlerException;

/**
 * 执行信息不存在异常。
 *
 * @author DwArFeng
 * @since 1.0.0
 */
public class ExecuteInfoNotExistsException extends HandlerException {

    private static final long serialVersionUID = 852663253012833692L;

    private final String id;

    public ExecuteInfoNotExistsException(String id) {
        this.id = id;
    }

    public ExecuteInfoNotExistsException(Throwable cause, String id) {
        super(cause);
        this.id = id;
    }

    @Override
    public String getMessage() {
        return "执行信息不存在: " + id;
    }
}
