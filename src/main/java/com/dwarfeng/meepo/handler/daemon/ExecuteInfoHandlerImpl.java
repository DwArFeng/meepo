package com.dwarfeng.meepo.handler.daemon;

import com.dwarfeng.dutil.basic.io.IOUtil;
import com.dwarfeng.dutil.basic.io.StringOutputStream;
import com.dwarfeng.meepo.bean.dto.ExecuteInfo;
import com.dwarfeng.meepo.util.Constants;
import com.dwarfeng.meepo.util.IdentifierUtil;
import com.dwarfeng.meepo.util.MeepoListConstructor;
import com.dwarfeng.subgrade.stack.exception.HandlerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;

import javax.annotation.PostConstruct;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Component
public class ExecuteInfoHandlerImpl implements ExecuteInfoHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExecuteInfoHandlerImpl.class);

    private final ApplicationContext ctx;

    private final Lock lock = new ReentrantLock();

    private final Map<String, ExecuteInfo> executeInfoMap = new LinkedHashMap<>();

    public ExecuteInfoHandlerImpl(ApplicationContext ctx) {
        this.ctx = ctx;
    }

    @PostConstruct
    public void init() throws Exception {
        lock.lock();
        try {
            LOGGER.info("初始化执行信息处理器...");
            internalReload();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public List<ExecuteInfo> getExecuteInfos() {
        lock.lock();
        try {
            return Collections.unmodifiableList(new ArrayList<>(executeInfoMap.values()));
        } finally {
            lock.unlock();
        }
    }

    @Override
    public ExecuteInfo getExecuteInfo(String id) {
        lock.lock();
        try {
            return executeInfoMap.get(id);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void reload() throws HandlerException {
        lock.lock();
        try {
            LOGGER.info("重新加载执行信息...");
            internalReload();
        } catch (Exception e) {
            throw new HandlerException(e);
        } finally {
            lock.unlock();
        }
    }

    private void internalReload() throws Exception {
        // 清除原有的执行信息。
        LOGGER.info("清除原有的执行信息...");
        executeInfoMap.clear();
        // 读取配置文件内容。
        LOGGER.info("读取配置文件内容...");
        String content;
        try (
                InputStream in = getConfigInputStream();
                StringOutputStream out = new StringOutputStream(StandardCharsets.UTF_8)
        ) {
            IOUtil.trans(in, out, 4096);
            out.flush();
            content = out.toString();
        }
        LOGGER.debug("配置文件内容: {}", content);
        Yaml yaml = new Yaml(new MeepoListConstructor(ExecuteInfo.class, new LoaderOptions()));
        // 此处类型转换安全性由编写配置文件的人员保证。
        // 如果配置文件格式不正确，则无法正确解析，解析方法将尽可能地保证安全。
        List<ExecuteInfo> executeInfos;
        try {
            executeInfos = yaml.load(content);
        } catch (Exception e) {
            LOGGER.warn("解析配置文件时发生异常, 将使用空列表代替, 异常信息如下:", e);
            executeInfos = new ArrayList<>();
        }
        root:
        for (ExecuteInfo executeInfo : executeInfos) {
            // 定义指令 ID 集合，用于校验。
            Set<String> existsCommandIdSet = new HashSet<>();
            // 参数校验。
            String id = executeInfo.getId();
            if (!IdentifierUtil.isValidIdentifier(id)) {
                LOGGER.warn("忽略 ID 非法的驱动信息配置 1 条, ID: {}", id);
                continue;
            }
            if (executeInfoMap.containsKey(id)) {
                LOGGER.warn("配置文件中已存在 ID 为 {} 的执行信息配置, 将忽略此条执行信息", id);
                continue;
            }
            // 参数调整。
            if (Objects.isNull(executeInfo.getConditionCommandInfos())) {
                executeInfo.setConditionCommandInfos(Collections.emptyList());
            }
            if (Objects.isNull(executeInfo.getModuleCommandInfos())) {
                executeInfo.setModuleCommandInfos(Collections.emptyList());
            }
            if (Objects.isNull(executeInfo.getObserverCommandInfos())) {
                executeInfo.setObserverCommandInfos(Collections.emptyList());
            }
            // 参数校验。
            List<ExecuteInfo.CommandInfo> conditionCommandInfos = executeInfo.getConditionCommandInfos();
            for (ExecuteInfo.CommandInfo conditionCommandInfo : conditionCommandInfos) {
                String conditionCommandInfoId = conditionCommandInfo.getId();
                if (!IdentifierUtil.isValidIdentifier(conditionCommandInfoId)) {
                    LOGGER.warn(
                            "忽略条件指令信息 ID 非法的条件命令配置 1 条, ID: {}, 条件指令信息 ID: {}",
                            id, conditionCommandInfoId
                    );
                    continue root;
                }
                if (existsCommandIdSet.contains(conditionCommandInfoId)) {
                    LOGGER.warn(
                            "忽略条件指令信息 ID 重复的条件命令配置 1 条, ID: {}, 条件指令信息 ID: {}",
                            id, conditionCommandInfoId
                    );
                    continue root;
                }
                existsCommandIdSet.add(conditionCommandInfoId);
            }
            List<ExecuteInfo.CommandInfo> moduleCommandInfos = executeInfo.getModuleCommandInfos();
            for (ExecuteInfo.CommandInfo moduleCommandInfo : moduleCommandInfos) {
                String moduleCommandInfoId = moduleCommandInfo.getId();
                if (!IdentifierUtil.isValidIdentifier(moduleCommandInfoId)) {
                    LOGGER.warn(
                            "忽略模块指令信息 ID 非法的模块命令配置 1 条, ID: {}, 模块指令信息 ID: {}",
                            id, moduleCommandInfoId
                    );
                    continue root;
                }
                if (existsCommandIdSet.contains(moduleCommandInfoId)) {
                    LOGGER.warn(
                            "忽略模块指令信息 ID 重复的模块命令配置 1 条, ID: {}, 模块指令信息 ID: {}",
                            id, moduleCommandInfoId
                    );
                    continue root;
                }
                existsCommandIdSet.add(moduleCommandInfoId);
            }
            List<ExecuteInfo.CommandInfo> observerCommandInfos = executeInfo.getObserverCommandInfos();
            for (ExecuteInfo.CommandInfo observerCommandInfo : observerCommandInfos) {
                String observerCommandInfoId = observerCommandInfo.getId();
                if (!IdentifierUtil.isValidIdentifier(observerCommandInfoId)) {
                    LOGGER.warn(
                            "忽略观察者指令信息 ID 非法的观察者命令配置 1 条, ID: {}, 观察者指令信息 ID: {}",
                            id, observerCommandInfoId
                    );
                    continue root;
                }
                if (existsCommandIdSet.contains(observerCommandInfoId)) {
                    LOGGER.warn(
                            "忽略观察者指令信息 ID 重复的观察者命令配置 1 条, ID: {}, 观察者指令信息 ID: {}",
                            id, observerCommandInfoId
                    );
                    continue root;
                }
                existsCommandIdSet.add(observerCommandInfoId);
            }
            // 添加执行信息。
            LOGGER.info("成功解析执行信息配置: {}", executeInfo);
            executeInfoMap.put(id, executeInfo);
        }
    }

    private InputStream getConfigInputStream() throws Exception {
        // 如果配置文件存在，则返回配置文件的输入流。
        if (Constants.FILE_EXECUTORS_CONFIG.exists()) {
            return Files.newInputStream(Constants.FILE_EXECUTORS_CONFIG.toPath());
        }
        // 如果配置文件不存在，则返回内置配置文件的输入流。
        LOGGER.warn("驱动信息配置文件不存在, 使用内置配置文件...");
        return ctx.getResource(Constants.RESOURCE_EXECUTORS_CONFIG).getInputStream();
    }
}
