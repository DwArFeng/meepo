package com.dwarfeng.meepo.bean.dto;

import com.dwarfeng.subgrade.stack.bean.dto.Dto;

import java.util.List;

/**
 * 执行信息。
 *
 * @author DwArFeng
 * @since 1.0.0
 */
public class ExecuteInfo implements Dto {

    private static final long serialVersionUID = 3350687705599658318L;

    private String id;
    private List<CommandInfo> conditionCommandInfos;
    private List<CommandInfo> moduleCommandInfos;
    private List<CommandInfo> observerCommandInfos;
    private boolean continueOnFailure;

    public ExecuteInfo() {
    }

    public ExecuteInfo(
            String id, List<CommandInfo> conditionCommandInfos, List<CommandInfo> moduleCommandInfos,
            List<CommandInfo> observerCommandInfos, boolean continueOnFailure
    ) {
        this.id = id;
        this.conditionCommandInfos = conditionCommandInfos;
        this.moduleCommandInfos = moduleCommandInfos;
        this.observerCommandInfos = observerCommandInfos;
        this.continueOnFailure = continueOnFailure;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<CommandInfo> getConditionCommandInfos() {
        return conditionCommandInfos;
    }

    public void setConditionCommandInfos(List<CommandInfo> conditionCommandInfos) {
        this.conditionCommandInfos = conditionCommandInfos;
    }

    public List<CommandInfo> getModuleCommandInfos() {
        return moduleCommandInfos;
    }

    public void setModuleCommandInfos(List<CommandInfo> moduleCommandInfos) {
        this.moduleCommandInfos = moduleCommandInfos;
    }

    public List<CommandInfo> getObserverCommandInfos() {
        return observerCommandInfos;
    }

    public void setObserverCommandInfos(List<CommandInfo> observerCommandInfos) {
        this.observerCommandInfos = observerCommandInfos;
    }

    public boolean isContinueOnFailure() {
        return continueOnFailure;
    }

    public void setContinueOnFailure(boolean continueOnFailure) {
        this.continueOnFailure = continueOnFailure;
    }

    @Override
    public String toString() {
        return "ExecuteInfo{" +
                "id='" + id + '\'' +
                ", conditionCommandInfos=" + conditionCommandInfos +
                ", moduleCommandInfos=" + moduleCommandInfos +
                ", observerCommandInfos=" + observerCommandInfos +
                ", continueOnFailure=" + continueOnFailure +
                '}';
    }

    public static class CommandInfo implements Dto {

        private static final long serialVersionUID = 2482071773785335644L;

        private String id;
        private String command;

        public CommandInfo() {
        }

        public CommandInfo(String id, String command) {
            this.id = id;
            this.command = command;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getCommand() {
            return command;
        }

        public void setCommand(String command) {
            this.command = command;
        }

        @Override
        public String toString() {
            return "CommandInfo{" +
                    "id='" + id + '\'' +
                    ", command='" + command + '\'' +
                    '}';
        }
    }
}
