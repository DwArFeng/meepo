package com.dwarfeng.meepo.configuration.daemon;

import com.dwarfeng.meepo.execption.ExecuteHandlerStoppedException;
import com.dwarfeng.meepo.execption.ExecuteInfoNotExistsException;
import com.dwarfeng.meepo.util.ServiceExceptionCodes;
import com.dwarfeng.subgrade.impl.exception.MapServiceExceptionMapper;
import com.dwarfeng.subgrade.sdk.exception.ServiceExceptionHelper;
import com.dwarfeng.subgrade.stack.exception.ServiceException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
public class ServiceExceptionMapperConfiguration {

    @SuppressWarnings("DuplicatedCode")
    @Bean
    public MapServiceExceptionMapper mapServiceExceptionMapper() {
        Map<Class<? extends Exception>, ServiceException.Code> destination = ServiceExceptionHelper.putDefaultDestination(null);
        destination.put(ExecuteHandlerStoppedException.class, ServiceExceptionCodes.EXECUTOR_HANDLER_STOPPED);
        destination.put(ExecuteInfoNotExistsException.class, ServiceExceptionCodes.EXECUTE_INFO_NOT_EXISTS);
        return new MapServiceExceptionMapper(destination, com.dwarfeng.subgrade.sdk.exception.ServiceExceptionCodes.UNDEFINED);
    }
}
