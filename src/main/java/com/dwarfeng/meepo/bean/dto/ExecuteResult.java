package com.dwarfeng.meepo.bean.dto;

import com.dwarfeng.subgrade.stack.bean.dto.Dto;

import java.util.List;

/**
 * 执行结果。
 *
 * @author DwArFeng
 * @since 1.0.0
 */
public class ExecuteResult implements Dto {

    private static final long serialVersionUID = 42602392138379527L;

    private boolean conditionPassed;
    private List<String> executedCommandIds;
    private List<String> finishedCommandIds;
    private List<String> failedCommandIds;

    public ExecuteResult() {
    }

    public ExecuteResult(
            boolean conditionPassed, List<String> executedCommandIds, List<String> finishedCommandIds,
            List<String> failedCommandIds
    ) {
        this.conditionPassed = conditionPassed;

        this.executedCommandIds = executedCommandIds;
        this.finishedCommandIds = finishedCommandIds;
        this.failedCommandIds = failedCommandIds;
    }

    public boolean isConditionPassed() {
        return conditionPassed;
    }

    public void setConditionPassed(boolean conditionPassed) {
        this.conditionPassed = conditionPassed;
    }

    public List<String> getExecutedCommandIds() {
        return executedCommandIds;
    }

    public void setExecutedCommandIds(List<String> executedCommandIds) {
        this.executedCommandIds = executedCommandIds;
    }

    public List<String> getFinishedCommandIds() {
        return finishedCommandIds;
    }

    public void setFinishedCommandIds(List<String> finishedCommandIds) {
        this.finishedCommandIds = finishedCommandIds;
    }

    public List<String> getFailedCommandIds() {
        return failedCommandIds;
    }

    public void setFailedCommandIds(List<String> failedCommandIds) {
        this.failedCommandIds = failedCommandIds;
    }

    @Override
    public String toString() {
        return "ExecuteResult{" +
                "conditionPassed=" + conditionPassed +
                ", executedCommandIds=" + executedCommandIds +
                ", finishedCommandIds=" + finishedCommandIds +
                ", failedCommandIds=" + failedCommandIds +
                '}';
    }
}
