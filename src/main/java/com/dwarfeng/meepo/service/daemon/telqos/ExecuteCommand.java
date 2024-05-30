package com.dwarfeng.meepo.service.daemon.telqos;

import com.dwarfeng.meepo.bean.dto.ExecuteInfo;
import com.dwarfeng.meepo.service.daemon.ExecuteQosService;
import com.dwarfeng.springtelqos.node.config.TelqosCommand;
import com.dwarfeng.springtelqos.sdk.command.CliCommand;
import com.dwarfeng.springtelqos.stack.command.Context;
import com.dwarfeng.springtelqos.stack.exception.TelqosException;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.ParseException;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ScheduledFuture;

@TelqosCommand
public class ExecuteCommand extends CliCommand {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExecuteCommand.class);

    private static final String COMMAND_OPTION_START = "start";
    private static final String COMMAND_OPTION_STOP = "stop";
    private static final String COMMAND_OPTION_STATUS = "status";
    private static final String COMMAND_OPTION_EXECUTE = "execute";
    private static final String COMMAND_OPTION_LIST = "list";
    private static final String COMMAND_OPTION_RELOAD = "reload";

    private static final String[] COMMAND_OPTION_ARRAY = new String[]{
            COMMAND_OPTION_START,
            COMMAND_OPTION_STOP,
            COMMAND_OPTION_STATUS,
            COMMAND_OPTION_EXECUTE,
            COMMAND_OPTION_LIST,
            COMMAND_OPTION_RELOAD
    };

    private static final String IDENTITY = "execute";
    private static final String DESCRIPTION = "执行处理器操作/查看";

    private static final String CMD_LINE_SYNTAX_START = IDENTITY + " " +
            CommandUtil.concatOptionPrefix(COMMAND_OPTION_START);
    private static final String CMD_LINE_SYNTAX_STOP = IDENTITY + " " +
            CommandUtil.concatOptionPrefix(COMMAND_OPTION_STOP);
    private static final String CMD_LINE_SYNTAX_STATUS = IDENTITY + " " +
            CommandUtil.concatOptionPrefix(COMMAND_OPTION_STATUS);
    private static final String CMD_LINE_SYNTAX_EXECUTE = IDENTITY + " " +
            CommandUtil.concatOptionPrefix(COMMAND_OPTION_EXECUTE) + " id";
    private static final String CMD_LINE_SYNTAX_LIST = IDENTITY + " " +
            CommandUtil.concatOptionPrefix(COMMAND_OPTION_LIST);
    private static final String CMD_LINE_SYNTAX_RELOAD = IDENTITY + " " +
            CommandUtil.concatOptionPrefix(COMMAND_OPTION_RELOAD);

    private static final String[] CMD_LINE_ARRAY = new String[]{
            CMD_LINE_SYNTAX_START,
            CMD_LINE_SYNTAX_STOP,
            CMD_LINE_SYNTAX_STATUS,
            CMD_LINE_SYNTAX_EXECUTE,
            CMD_LINE_SYNTAX_LIST,
            CMD_LINE_SYNTAX_RELOAD
    };

    private static final String CMD_LINE_SYNTAX = CommandUtil.syntax(CMD_LINE_ARRAY);

    private final ExecuteQosService executeQosService;

    private final ThreadPoolTaskScheduler scheduler;

    public ExecuteCommand(ExecuteQosService executeQosService, ThreadPoolTaskScheduler scheduler) {
        super(IDENTITY, DESCRIPTION, CMD_LINE_SYNTAX);
        this.executeQosService = executeQosService;
        this.scheduler = scheduler;
    }

    @Override
    protected List<Option> buildOptions() {
        List<Option> list = new ArrayList<>();
        list.add(Option.builder(COMMAND_OPTION_START).desc("启动执行处理器").build());
        list.add(Option.builder(COMMAND_OPTION_STOP).desc("停止执行处理器").build());
        list.add(Option.builder(COMMAND_OPTION_STATUS).desc("查看执行处理器状态").build());
        list.add(Option.builder(COMMAND_OPTION_EXECUTE).desc("执行指定的任务").hasArg().type(String.class).build());
        list.add(Option.builder(COMMAND_OPTION_LIST).desc("列出执行信息").build());
        list.add(Option.builder(COMMAND_OPTION_RELOAD).desc("重新加载执行信息").build());
        return list;
    }

    @Override
    protected void executeWithCmd(Context context, CommandLine cmd) throws TelqosException {
        try {
            Pair<String, Integer> pair = CommandUtil.analyseCommand(cmd, COMMAND_OPTION_ARRAY);
            if (pair.getRight() != 1) {
                context.sendMessage(CommandUtil.optionMismatchMessage(COMMAND_OPTION_ARRAY));
                context.sendMessage(CMD_LINE_SYNTAX);
                return;
            }
            switch (pair.getLeft()) {
                case COMMAND_OPTION_START:
                    executeQosService.start();
                    context.sendMessage("执行处理器已启动!");
                    break;
                case COMMAND_OPTION_STOP:
                    stop(context);
                    break;
                case COMMAND_OPTION_STATUS:
                    printStatus(context);
                    break;
                case COMMAND_OPTION_EXECUTE:
                    execute(context, cmd);
                    break;
                case COMMAND_OPTION_LIST:
                    printList(context);
                    break;
                case COMMAND_OPTION_RELOAD:
                    executeQosService.reload();
                    context.sendMessage("执行信息已重新加载!");
                    break;
            }
        } catch (Exception e) {
            throw new TelqosException(e);
        }
    }

    @SuppressWarnings("DuplicatedCode")
    private void stop(Context context) throws Exception {
        ScheduledFuture<?> future = scheduler.scheduleWithFixedDelay(
                () -> {
                    try {
                        context.sendMessage("仍有执行中的执行任务, 请耐心等待");
                    } catch (Exception e) {
                        LOGGER.warn("发送消息时发生异常, 异常信息如下:", e);
                    }
                },
                new Date(System.currentTimeMillis() + 1000),
                1000
        );
        executeQosService.stop();
        future.cancel(true);
        context.sendMessage("执行处理器已停止!");
    }

    private void printStatus(Context context) throws Exception {
        boolean startedFlag = executeQosService.isStarted();
        context.sendMessage(String.format("started: %b.", startedFlag));
    }

    private void execute(Context context, CommandLine cmd) throws Exception {
        String id;
        try {
            id = (String) cmd.getParsedOptionValue(COMMAND_OPTION_EXECUTE);
        } catch (ParseException e) {
            LOGGER.warn("解析命令选项时发生异常, 异常信息如下:", e);
            context.sendMessage("命令行格式错误，正确的格式为: " + CMD_LINE_SYNTAX_EXECUTE);
            context.sendMessage("请留意选项 " + CMD_LINE_SYNTAX_EXECUTE + " 后接参数的类型应该是字符串 ");
            return;
        }
        executeQosService.execute(id);
        context.sendMessage("任务已执行!");
    }

    private void printList(Context context) throws Exception {
        List<ExecuteInfo> executeInfos = executeQosService.getExecuteInfos();
        context.sendMessage("Executor info list, total: " + executeInfos.size());
        for (int i = 0; i < executeInfos.size(); i++) {
            ExecuteInfo executeInfo = executeInfos.get(i);
            context.sendMessage(String.format("  %d/%d: %s", i, executeInfos.size(), executeInfo.toString()));
        }
    }
}
