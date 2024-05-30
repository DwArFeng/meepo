package com.dwarfeng.meepo.handler.daemon;

import com.dwarfeng.meepo.execption.ExecuteInfoNotExistsException;
import com.dwarfeng.subgrade.stack.exception.HandlerException;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * 操作处理器验证器。
 *
 * <p>
 * 为操作处理器提供公共的验证方法。
 *
 * @author DwArFeng
 * @since 1.0.0
 */
@Component
public class HandlerValidator {

    private final ExecuteInfoHandler executeInfoHandler;

    public HandlerValidator(ExecuteInfoHandler executeInfoHandler) {
        this.executeInfoHandler = executeInfoHandler;
    }

    public void makeSureExecuteInfoExists(String id) throws HandlerException {
        if (Objects.isNull(executeInfoHandler.getExecuteInfo(id))) {
            throw new ExecuteInfoNotExistsException(id);
        }
    }
}
