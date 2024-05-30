package com.dwarfeng.meepo.launcher.daemon;

import com.dwarfeng.meepo.handler.daemon.LauncherSettingHandler;
import com.dwarfeng.meepo.service.daemon.ArrivalQosService;
import com.dwarfeng.meepo.service.daemon.ExecuteQosService;
import com.dwarfeng.springterminator.sdk.util.ApplicationUtil;
import com.dwarfeng.subgrade.stack.exception.ServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import java.util.Date;

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
                "classpath:spring/daemon/application-context*.xml",
                "file:opt/daemon/opt*.xml",
                "file:optext/daemon/opt*.xml"
        }, ctx -> {
            // 根据启动器设置处理器的设置，选择性启动执行服务。
            mayStartExecute(ctx);
            // 根据启动器设置处理器的设置，选择性启动 arrival 服务。
            mayStartArrival(ctx);
        });
    }

    private static void mayStartExecute(ApplicationContext ctx) {
        // 获取启动器设置处理器，用于获取启动器设置，并按照设置选择性执行功能。
        LauncherSettingHandler launcherSettingHandler = ctx.getBean(LauncherSettingHandler.class);

        // 获取程序中的 ThreadPoolTaskScheduler，用于处理计划任务。
        ThreadPoolTaskScheduler scheduler = ctx.getBean(ThreadPoolTaskScheduler.class);

        // 获取执行 QOS 服务。
        ExecuteQosService executeQosService = ctx.getBean(ExecuteQosService.class);

        // 判断执行处理器是否启动执行服务，并按条件执行不同的操作。
        long startExecuteDelay = launcherSettingHandler.getStartExecuteDelay();
        if (startExecuteDelay == 0) {
            LOGGER.info("立即启动执行服务...");
            try {
                executeQosService.start();
            } catch (ServiceException e) {
                LOGGER.error("无法启动执行服务, 异常信息如下: ", e);
            }
        } else if (startExecuteDelay > 0) {
            LOGGER.info("{} 毫秒后启动执行服务...", startExecuteDelay);
            scheduler.schedule(
                    () -> {
                        LOGGER.info("启动执行服务...");
                        try {
                            executeQosService.start();
                        } catch (ServiceException e) {
                            LOGGER.error("无法启动执行服务, 异常信息如下: ", e);
                        }
                    },
                    new Date(System.currentTimeMillis() + startExecuteDelay)
            );
        }
    }

    private static void mayStartArrival(ApplicationContext ctx) {
        // 获取启动器设置处理器，用于获取启动器设置，并按照设置选择性 arrival 功能。
        LauncherSettingHandler launcherSettingHandler = ctx.getBean(LauncherSettingHandler.class);

        // 获取程序中的 ThreadPoolTaskScheduler，用于处理计划任务。
        ThreadPoolTaskScheduler scheduler = ctx.getBean(ThreadPoolTaskScheduler.class);

        // 获取 arrival  QOS 服务。
        ArrivalQosService arrivalQosService = ctx.getBean(ArrivalQosService.class);

        // 判断 arrival 处理器是否启动 arrival 服务，并按条件 arrival 不同的操作。
        long startArrivalDelay = launcherSettingHandler.getStartArrivalDelay();
        if (startArrivalDelay == 0) {
            LOGGER.info("立即启动 arrival 服务...");
            try {
                arrivalQosService.start();
            } catch (ServiceException e) {
                LOGGER.error("无法启动 arrival 服务, 异常信息如下: ", e);
            }
        } else if (startArrivalDelay > 0) {
            LOGGER.info("{} 毫秒后启动 arrival 服务...", startArrivalDelay);
            scheduler.schedule(
                    () -> {
                        LOGGER.info("启动 arrival 服务...");
                        try {
                            arrivalQosService.start();
                        } catch (ServiceException e) {
                            LOGGER.error("无法启动 arrival 服务, 异常信息如下: ", e);
                        }
                    },
                    new Date(System.currentTimeMillis() + startArrivalDelay)
            );
        }
    }
}
