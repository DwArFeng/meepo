package com.dwarfeng.meepo.service.daemon.telqos;

import com.dwarfeng.meepo.service.daemon.ArrivalQosService;
import com.dwarfeng.springtelqos.sdk.command.CliCommand;
import com.dwarfeng.springtelqos.sdk.configuration.TelqosCommand;
import com.dwarfeng.springtelqos.sdk.util.CliCommandUtil;
import com.dwarfeng.springtelqos.stack.command.CommandDescriptor;
import com.dwarfeng.springtelqos.stack.command.CommandExecutor;
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

    @SuppressWarnings({"SpellCheckingInspection", "GrazieInspectionRunner", "RedundantSuppression"})
    private static final String IDENTITY = "arrival";

    private static final Logger LOGGER = LoggerFactory.getLogger(ArrivalCommand.class);

    // region 指令选项

    private static final String COMMAND_OPTION_START = "start";
    private static final String COMMAND_OPTION_STOP = "stop";
    private static final String COMMAND_OPTION_STATUS = "status";

    private static final String[] COMMAND_OPTION_ARRAY = new String[]{
            COMMAND_OPTION_START,
            COMMAND_OPTION_STOP,
            COMMAND_OPTION_STATUS
    };

    // endregion

    private final ArrivalQosService arrivalQosService;

    private final ThreadPoolTaskScheduler scheduler;

    public ArrivalCommand(ArrivalQosService arrivalQosService, ThreadPoolTaskScheduler scheduler) {
        super(IDENTITY);
        this.arrivalQosService = arrivalQosService;
        this.scheduler = scheduler;
    }

    @Override
    protected DescriptionProvider provideDescriptionProvider() {
        return context -> "Arrival 处理器操作/查看";
    }

    @Override
    protected CliSyntaxProvider provideCliSyntaxProvider() {
        return this::cliSyntaxProvider;
    }

    private String cliSyntaxProvider(CommandDescriptor.Context context) throws Exception {
        String identity = context.getRuntimeIdentity();
        String[] patterns = new String[]{
                identity + " " + CliCommandUtil.concatOptionPrefix(COMMAND_OPTION_START),
                identity + " " + CliCommandUtil.concatOptionPrefix(COMMAND_OPTION_STOP),
                identity + " " + CliCommandUtil.concatOptionPrefix(COMMAND_OPTION_STATUS)
        };
        return CliCommandUtil.cliSyntax(patterns);
    }

    @Override
    protected List<Option> provideOptions() {
        List<Option> list = new ArrayList<>();
        list.add(Option.builder(COMMAND_OPTION_START).optionalArg(true).hasArg(false).desc("启动 arrival 处理器").build());
        list.add(Option.builder(COMMAND_OPTION_STOP).optionalArg(true).hasArg(false).desc("停止 arrival 处理器").build());
        list.add(Option.builder(COMMAND_OPTION_STATUS).optionalArg(true).hasArg(false).desc("查看 arrival 处理器状态").build());
        return list;
    }

    @Override
    protected void executeWithCmd(CommandExecutor.Context context, CommandLine cmd) throws Exception {
        Pair<String, Integer> pair = CliCommandUtil.analyseCommand(cmd, COMMAND_OPTION_ARRAY);
        if (pair.getRight() != 1) {
            context.sendMessage(CliCommandUtil.optionMismatchMessage(COMMAND_OPTION_ARRAY));
            context.sendMessage(context.getCommandManual(context.getRuntimeIdentity()));
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
            default:
                throw new IllegalStateException("不应该执行到此处, 请联系开发人员");
        }
    }

    @SuppressWarnings("DuplicatedCode")
    private void stop(CommandExecutor.Context context) throws Exception {
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

    private void printStatus(CommandExecutor.Context context) throws Exception {
        boolean startedFlag = arrivalQosService.isStarted();
        context.sendMessage(String.format("started: %b.", startedFlag));
    }
}
