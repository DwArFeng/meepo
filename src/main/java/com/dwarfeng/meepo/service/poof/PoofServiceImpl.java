package com.dwarfeng.meepo.service.poof;

import com.dwarfeng.meepo.handler.poof.PoofHandler;
import com.dwarfeng.subgrade.sdk.exception.ServiceExceptionHelper;
import com.dwarfeng.subgrade.stack.exception.ServiceException;
import com.dwarfeng.subgrade.stack.exception.ServiceExceptionMapper;
import com.dwarfeng.subgrade.stack.log.LogLevel;
import org.springframework.stereotype.Service;

@Service
public class PoofServiceImpl implements PoofService {

    private final PoofHandler poofHandler;

    private final ServiceExceptionMapper sem;

    public PoofServiceImpl(PoofHandler poofHandler, ServiceExceptionMapper sem) {
        this.poofHandler = poofHandler;
        this.sem = sem;
    }

    @Override
    public int poof(String host, int port, String id) throws ServiceException {
        try {
            return poofHandler.poof(host, port, id);
        } catch (Exception e) {
            throw ServiceExceptionHelper.logParse("向指定的主机和端口发送 Poof 消息时发生异常", LogLevel.WARN, e, sem);
        }
    }
}
