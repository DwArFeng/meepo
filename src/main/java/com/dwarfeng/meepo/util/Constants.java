package com.dwarfeng.meepo.util;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * 常量类。
 *
 * @author DwArFeng
 * @since 1.0.0
 */
public final class Constants {

    public static final File DIR_LIBCMD_CONDITION = new File("libcmd" + File.separator + "condition");
    public static final File DIR_LIBCMD_MODULE = new File("libcmd" + File.separator + "module");
    public static final File DIR_LIBCMD_OBSERVER = new File("libcmd" + File.separator + "observer");

    public static final File FILE_EXECUTORS_CONFIG = new File(
            "conf" + File.separator + "meepo" + File.separator + "daemon" + File.separator + "executors.yaml"
    );

    public static final String RESOURCE_EXECUTORS_CONFIG = "classpath:meepo/daemon/executors.yaml";

    public static final String OBSERVER_EVENT_CONDITION_PASSED = "CONDITION_PASSED";
    public static final String OBSERVER_EVENT_CONDITION_NOT_PASSED = "CONDITION_NOT_PASSED";
    public static final String OBSERVER_EVENT_MODULES_STARTED = "MODULES_STARTED";
    public static final String OBSERVER_EVENT_MODULES_FINISHED = "MODULES_FINISHED";
    public static final String OBSERVER_EVENT_MODULE_STARTED = "MODULE_STARTED";
    public static final String OBSERVER_EVENT_MODULE_FINISHED = "MODULE_FINISHED";
    public static final String OBSERVER_EVENT_MODULE_FAILED = "MODULE_FAILED";

    public static final int POOF_RESPONSE_CODE_SUCCESS = 0;
    public static final int POOF_RESPONSE_CODE_ADDRESS_BLOCKED = 400;
    public static final int POOF_RESPONSE_CODE_EXCEPTION = 500;
    public static final int POOF_RESPONSE_CODE_CONDITION_NOT_PASSED = 600;
    public static final int POOF_RESPONSE_CODE_FAILED = 601;
    public static final int POOF_RESPONSE_CODE_UNKNOWN = 900;

    /**
     * 换行符。
     *
     * <p>
     * 统一换行符为 \n，以免出现跨平台问题。
     */
    public static final String LINE_SEPARATOR = "\n";

    /**
     * 字符集。
     *
     * <p>
     * 统一字符集为 UTF-8，以免出现乱码问题。
     */
    public static final Charset CHARSET = StandardCharsets.UTF_8;

    public static final int ARRIVAL_DEFAULT_PORT = 8089;

    public static final int POOF_EXCEPTIONALLY_EXIT_CODE = -1;

    private Constants() {
        throw new IllegalStateException("禁止实例化");
    }
}
