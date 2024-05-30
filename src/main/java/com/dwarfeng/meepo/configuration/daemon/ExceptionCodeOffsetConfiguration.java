package com.dwarfeng.meepo.configuration.daemon;

import com.dwarfeng.subgrade.sdk.exception.ServiceExceptionCodes;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

@Configuration
public class ExceptionCodeOffsetConfiguration {

    @Value("${meepo.daemon.exception_code_offset}")
    private int exceptionCodeOffset;
    @Value("${meepo.daemon.exception_code_offset.subgrade}")
    private int subgradeExceptionCodeOffset;

    @PostConstruct
    public void init() {
        ServiceExceptionCodes.setExceptionCodeOffset(exceptionCodeOffset);
        ServiceExceptionCodes.setExceptionCodeOffset(subgradeExceptionCodeOffset);
    }
}
