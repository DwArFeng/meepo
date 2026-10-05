package com.dwarfeng.meepo.service.daemon.telqos;

import com.dwarfeng.meepo.bean.dto.ExecuteInfo;
import com.dwarfeng.meepo.service.daemon.ExecuteQosService;
import com.dwarfeng.springtelqos.sdk.command.CliCommand;
import com.dwarfeng.springtelqos.sdk.configuration.TelqosCommand;
import com.dwarfeng.springtelqos.sdk.util.CliCommandUtil;
import com.dwarfeng.springtelqos.stack.command.CommandDescriptor;
import com.dwarfeng.springtelqos.stack.command.CommandExecutor;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.lang3.StringUtils;
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

    @SuppressWarnings({"SpellCheckingInspection", "GrazieInspectionRunner", "RedundantSuppression"})
    private static final String IDENTITY = "execute";

    private static final Logger LOGGER = LoggerFactory.getLogger(ExecuteCommand.class);

    // region 指令选项

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

    private static final String COMMAND_SUB_OPTION_ID = "id";

    // endregion

    private final ExecuteQosService executeQosService;

    private final ThreadPoolTaskScheduler scheduler;

    public ExecuteCommand(ExecuteQosService executeQosService, ThreadPoolTaskScheduler scheduler) {
        super(IDENTITY);
        this.executeQosService = executeQosService;
        this.scheduler = scheduler;
    }

    @Override
    protected DescriptionProvider provideDescriptionProvider() {
        return context -> "执行处理器操作/查看";
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
                identity + " " + CliCommandUtil.concatOptionPrefix(COMMAND_OPTION_STATUS),
                identity + " " + CliCommandUtil.concatOptionPrefix(COMMAND_OPTION_EXECUTE) + " [" +
                        CliCommandUtil.concatOptionPrefix(COMMAND_SUB_OPTION_ID) + " id]",
                identity + " " + CliCommandUtil.concatOptionPrefix(COMMAND_OPTION_LIST),
                identity + " " + CliCommandUtil.concatOptionPrefix(COMMAND_OPTION_RELOAD)
        };
        return CliCommandUtil.cliSyntax(patterns);
    }

    @Override
    protected List<Option> provideOptions() {
        List<Option> list = new ArrayList<>();
        list.add(Option.builder(COMMAND_OPTION_START).optionalArg(true).hasArg(false).desc("启动执行处理器").build());
        list.add(Option.builder(COMMAND_OPTION_STOP).optionalArg(true).hasArg(false).desc("停止执行处理器").build());
        list.add(Option.builder(COMMAND_OPTION_STATUS).optionalArg(true).hasArg(false).desc("查看执行处理器状态").build());
        list.add(Option.builder(COMMAND_OPTION_EXECUTE).optionalArg(true).hasArg(false).desc("执行指定的任务").build());
        list.add(Option.builder(COMMAND_OPTION_LIST).optionalArg(true).hasArg(false).desc("列出执行信息").build());
        list.add(Option.builder(COMMAND_OPTION_RELOAD).optionalArg(true).hasArg(false).desc("重新加载执行信息").build());
        list.add(Option.builder(COMMAND_SUB_OPTION_ID).hasArg(true).type(String.class).desc("待执行任务的 ID").build());
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
                handleExecute(context, cmd);
                break;
            case COMMAND_OPTION_LIST:
                printList(context);
                break;
            case COMMAND_OPTION_RELOAD:
                executeQosService.reload();
                context.sendMessage("执行信息已重新加载!");
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
        executeQosService.stop();
        future.cancel(true);
        context.sendMessage("执行处理器已停止!");
    }

    private void printStatus(CommandExecutor.Context context) throws Exception {
        boolean startedFlag = executeQosService.isStarted();
        context.sendMessage(String.format("started: %b.", startedFlag));
    }

    private void handleExecute(CommandExecutor.Context context, CommandLine cmd) throws Exception {
        String id = parseId(context, cmd);
        executeQosService.execute(id);
        context.sendMessage("任务已执行!");
    }

    private String parseId(CommandExecutor.Context context, CommandLine cmd) throws Exception {
        String id;
        // 优先从子选项中获取任务 ID。
        if (cmd.hasOption(COMMAND_SUB_OPTION_ID)) {
            id = (String) cmd.getParsedOptionValue(COMMAND_SUB_OPTION_ID);
        } else {
            id = null;
        }
        // 子选项缺失或取值为空时，以交互方式获取任务 ID。
        if (StringUtils.isBlank(id)) {
            context.sendMessage("请输入需要执行的任务 ID:");
            id = context.receiveMessage();
        }
        // 对最终获取的任务 ID 进行空值校验。
        if (StringUtils.isBlank(id)) {
            throw new IllegalArgumentException("执行任务 ID 不能为空");
        }
        return id;
    }

    private void printList(CommandExecutor.Context context) throws Exception {
        List<ExecuteInfo> executeInfos = executeQosService.getExecuteInfos();
        context.sendMessage("Executor info list, total: " + executeInfos.size());
        for (int i = 0; i < executeInfos.size(); i++) {
            ExecuteInfo executeInfo = executeInfos.get(i);
            context.sendMessage(String.format("  %d/%d: %s", i, executeInfos.size(), executeInfo.toString()));
        }
    }
}
