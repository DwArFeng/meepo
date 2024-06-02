package com.dwarfeng.meepo.bean.dto;

import com.alibaba.fastjson.annotation.JSONField;
import com.dwarfeng.subgrade.stack.bean.dto.Dto;

/**
 * Arrival 响应。
 *
 * @author DwArFeng
 * @since 1.1.0
 */
public class ArrivalResponse implements Dto {

    private static final long serialVersionUID = -6228964813557932034L;
    
    @JSONField(name = "exception_flag", ordinal = 1)
    private boolean exceptionFlag;

    @JSONField(name = "exception_message", ordinal = 2)
    private String exceptionMessage;

    @JSONField(name = "execute_info", ordinal = 3)
    private ExecuteResult executeResult;

    public ArrivalResponse() {
    }

    public ArrivalResponse(boolean exceptionFlag, String exceptionMessage, ExecuteResult executeResult) {
        this.exceptionFlag = exceptionFlag;
        this.exceptionMessage = exceptionMessage;
        this.executeResult = executeResult;
    }

    public boolean isExceptionFlag() {
        return exceptionFlag;
    }

    public void setExceptionFlag(boolean exceptionFlag) {
        this.exceptionFlag = exceptionFlag;
    }

    public String getExceptionMessage() {
        return exceptionMessage;
    }

    public void setExceptionMessage(String exceptionMessage) {
        this.exceptionMessage = exceptionMessage;
    }

    public ExecuteResult getExecuteResult() {
        return executeResult;
    }

    public void setExecuteResult(ExecuteResult executeResult) {
        this.executeResult = executeResult;
    }

    @Override
    public String toString() {
        return "ArrivalResponse{" +
                "exceptionFlag=" + exceptionFlag +
                ", exceptionMessage='" + exceptionMessage + '\'' +
                ", executeResult=" + executeResult +
                '}';
    }
}
