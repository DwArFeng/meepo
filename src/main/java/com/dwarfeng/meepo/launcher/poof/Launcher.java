package com.dwarfeng.meepo.launcher.poof;

import com.dwarfeng.dutil.basic.mea.TimeMeasurer;
import com.dwarfeng.meepo.service.poof.PoofService;
import com.dwarfeng.meepo.util.Constants;
import com.dwarfeng.springterminator.sdk.util.ApplicationUtil;
import com.dwarfeng.springterminator.stack.handler.TerminateHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 程序启动器。
 *
 * @author DwArFeng
 * @since 1.0.0
 */
public class Launcher {

    private static final Logger LOGGER = LoggerFactory.getLogger(Launcher.class);

    public static void main(String[] args) {
        ApplicationUtil.launch(new String[]{
                "classpath:spring/poof/application-context*.xml",
                "file:opt/poof/opt*.xml",
                "file:optext/poof/opt*.xml"
        }, ctx -> {
            // 获取 TerminateHandler 对象。
            TerminateHandler terminator = ctx.getBean(TerminateHandler.class);

            // 检查 args 的数量应该大于等于 2。
            if (args.length < 2) {
                LOGGER.error("参数数量不足 2, 无法启动程序");
                exit(terminator, Constants.POOF_EXCEPTIONALLY_EXIT_CODE);
            }

            // 基于 args[0]，获取 host 和 port。
            // 基本格式为 host[:port]，如果没有 port，默认为 8089。
            String hostAndPort = args[0];
            String[] split = hostAndPort.split(":");
            String host = split[0];
            int port = split.length == 1 ? Constants.ARRIVAL_DEFAULT_PORT : Integer.parseInt(split[1]);

            // 基于 args[1]，获取 id。
            String id = args[1];

            // 计时。
            TimeMeasurer tm = new TimeMeasurer();
            tm.start();

            // 调用 PoofService 的 poof 方法。
            int exitCode = Constants.POOF_EXCEPTIONALLY_EXIT_CODE;
            PoofService poofService = ctx.getBean(PoofService.class);
            try {
                exitCode = poofService.poof(host, port, id);
            } catch (Exception e) {
                LOGGER.error("执行 poof 指令时发生异常, 异常信息如下: ", e);
                exit(terminator, Constants.POOF_EXCEPTIONALLY_EXIT_CODE);
            }

            // 计时结束，并输出用时。
            tm.stop();
            LOGGER.info("Poof 指令执行完毕, 用时 {}ms", tm.getTimeMs());

            // 退出程序。
            exit(terminator, exitCode);
        });
    }

    /**
     * 退出程序。
     *
     * <p>
     * 该方法封装了 {@link TerminateHandler#exit(int)} 的调用。
     * 终止处理器在退出程序的过程中可能抛出异常，此处统一进行捕获并记录日志，避免终止流程本身的异常影响启动器的执行。
     *
     * @param terminator 终止处理器。
     * @param exitCode   程序退出码。
     */
    private static void exit(TerminateHandler terminator, int exitCode) {
        try {
            terminator.exit(exitCode);
        } catch (Exception e) {
            LOGGER.error("退出程序时发生异常, 异常信息如下: ", e);
        }
    }
}
