package com.dwarfeng.meepo.service.daemon.telqos;

import com.dwarfeng.meepo.service.daemon.ArrivalQosService;
import com.dwarfeng.springtelqos.node.config.TelqosCommand;
import com.dwarfeng.springtelqos.sdk.command.CliCommand;
import com.dwarfeng.springtelqos.stack.command.Context;
import com.dwarfeng.springtelqos.stack.exception.TelqosException;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ScheduledFuture;

@TelqosCommand
public class ArrivalCommand extends CliCommand {

    private static final Logger LOGGER = LoggerFactory.getLogger(ArrivalCommand.class);

    private static final String COMMAND_OPTION_START = "start";
    private static final String COMMAND_OPTION_STOP = "stop";
    private static final String COMMAND_OPTION_STATUS = "status";

    private static final String[] COMMAND_OPTION_ARRAY = new String[]{
            COMMAND_OPTION_START,
            COMMAND_OPTION_STOP,
            COMMAND_OPTION_STATUS,
    };

    private static final String IDENTITY = "arrival";
    private static final String DESCRIPTION = "Arrival 处理器操作/查看";

    private static final String CMD_LINE_SYNTAX_START = IDENTITY + " " +
            CommandUtil.concatOptionPrefix(COMMAND_OPTION_START);
    private static final String CMD_LINE_SYNTAX_STOP = IDENTITY + " " +
            CommandUtil.concatOptionPrefix(COMMAND_OPTION_STOP);
    private static final String CMD_LINE_SYNTAX_STATUS = IDENTITY + " " +
            CommandUtil.concatOptionPrefix(COMMAND_OPTION_STATUS);

    private static final String[] CMD_LINE_ARRAY = new String[]{
            CMD_LINE_SYNTAX_START,
            CMD_LINE_SYNTAX_STOP,
            CMD_LINE_SYNTAX_STATUS,
    };

    private static final String CMD_LINE_SYNTAX = CommandUtil.syntax(CMD_LINE_ARRAY);

    private final ArrivalQosService arrivalQosService;

    private final ThreadPoolTaskScheduler scheduler;

    public ArrivalCommand(ArrivalQosService arrivalQosService, ThreadPoolTaskScheduler scheduler) {
        super(IDENTITY, DESCRIPTION, CMD_LINE_SYNTAX);
        this.arrivalQosService = arrivalQosService;
        this.scheduler = scheduler;
    }

    @Override
    protected List<Option> buildOptions() {
        List<Option> list = new ArrayList<>();
        list.add(Option.builder(COMMAND_OPTION_START).desc("启动 arrival 处理器").build());
        list.add(Option.builder(COMMAND_OPTION_STOP).desc("停止 arrival 处理器").build());
        list.add(Option.builder(COMMAND_OPTION_STATUS).desc("查看 arrival 处理器状态").build());
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
                    arrivalQosService.start();
                    context.sendMessage("执行处理器已启动!");
                    break;
                case COMMAND_OPTION_STOP:
                    stop(context);
                    break;
                case COMMAND_OPTION_STATUS:
                    printStatus(context);
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
        arrivalQosService.stop();
        future.cancel(true);
        context.sendMessage("执行处理器已停止!");
    }

    private void printStatus(Context context) throws Exception {
        boolean startedFlag = arrivalQosService.isStarted();
        context.sendMessage(String.format("started: %b.", startedFlag));
    }
}
