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

    private static final long serialVersionUID = 2276399178581642995L;

    @JSONField(name = "address_blocked_flag", ordinal = 1)
    private boolean addressBlockedFlag;

    @JSONField(name = "exception_flag", ordinal = 2)
    private boolean exceptionFlag;

    @JSONField(name = "exception_message", ordinal = 3)
    private String exceptionMessage;

    @JSONField(name = "execute_info", ordinal = 4)
    private ExecuteResult executeResult;

    public ArrivalResponse() {
    }

    public ArrivalResponse(
            boolean addressBlockedFlag, boolean exceptionFlag, String exceptionMessage, ExecuteResult executeResult
    ) {
        this.addressBlockedFlag = addressBlockedFlag;
        this.exceptionFlag = exceptionFlag;
        this.exceptionMessage = exceptionMessage;
        this.executeResult = executeResult;
    }

    public boolean isAddressBlockedFlag() {
        return addressBlockedFlag;
    }

    public void setAddressBlockedFlag(boolean addressBlockedFlag) {
        this.addressBlockedFlag = addressBlockedFlag;
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
                "addressBlockedFlag=" + addressBlockedFlag +
                ", exceptionFlag=" + exceptionFlag +
                ", exceptionMessage='" + exceptionMessage + '\'' +
                ", executeResult=" + executeResult +
                '}';
    }
}
