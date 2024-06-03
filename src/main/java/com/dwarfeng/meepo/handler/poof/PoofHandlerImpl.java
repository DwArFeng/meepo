package com.dwarfeng.meepo.handler.poof;

import com.alibaba.fastjson.JSON;
import com.dwarfeng.meepo.bean.dto.ArrivalResponse;
import com.dwarfeng.meepo.bean.dto.ExecuteResult;
import com.dwarfeng.meepo.util.Constants;
import com.dwarfeng.subgrade.sdk.exception.HandlerExceptionHelper;
import com.dwarfeng.subgrade.stack.exception.HandlerException;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.Socket;
import java.util.Objects;

@Component
public class PoofHandlerImpl implements PoofHandler {

    @Override
    public int poof(String host, int port, String id) throws HandlerException {
        try {
            return doPoof(host, port, id);
        } catch (Exception e) {
            throw HandlerExceptionHelper.parse(e);
        }
    }

    private int doPoof(String host, int port, String id) throws Exception {
        Socket socket = null;
        try {
            // 创建一个新的 Socket 对象。
            socket = new Socket(host, port);

            // 获取 Socket 的输入流和输出流。
            InputStream socketIn = socket.getInputStream();
            OutputStream socketOut = socket.getOutputStream();

            // 将 id 发送至服务端。
            writeContent(socketOut, id);

            // 将服务端的返回响应序列化为 ArrivalResponse 对象。
            ArrivalResponse arrivalResponse = JSON.parseObject(socketIn, ArrivalResponse.class);

            // 将 arrivalResponse 解析为 Poof 响应码并返回。
            return parseExitCode(arrivalResponse);
        } finally {
            if (Objects.nonNull(socket)) {
                socket.close();
            }
        }
    }

    private void writeContent(OutputStream socketOut, String content) throws Exception {
        Writer writer = new OutputStreamWriter(socketOut, Constants.CHARSET);
        writer.write(content + Constants.LINE_SEPARATOR);
        writer.flush();
    }

    private int parseExitCode(ArrivalResponse arrivalResponse) {
        // 如果 address_blocked_flag 为 true，返回 POOF_RESPONSE_CODE_ADDRESS_NOT_PASSED。
        if (arrivalResponse.isAddressBlockedFlag()) {
            // 返回地址未通过码。
            return Constants.POOF_RESPONSE_CODE_ADDRESS_BLOCKED;
        }
        // 如果 exceptionFlag 为 true，返回 POOF_RESPONSE_CODE_EXCEPTION。
        if (arrivalResponse.isExceptionFlag()) {
            // 返回异常码。
            return Constants.POOF_RESPONSE_CODE_EXCEPTION;
        }
        // 获取 executeResult。
        ExecuteResult executeResult = arrivalResponse.getExecuteResult();
        // 如果 executeResult 为 null，返回 POOF_RESPONSE_CODE_UNKNOWN。
        if (Objects.isNull(executeResult)) {
            // 返回未知码。
            return Constants.POOF_RESPONSE_CODE_UNKNOWN;
        }
        // 进一步解析 executeResult：
        // 如果 !conditionPassed，返回 POOF_RESPONSE_CODE_CONDITION_NOT_PASSED。
        if (!executeResult.isConditionPassed()) {
            // 返回条件未通过码。
            return Constants.POOF_RESPONSE_CODE_CONDITION_NOT_PASSED;
        }
        // 如果 executeResult 中的所有指令均执行成功，返回 POOF_RESPONSE_CODE_SUCCESS。
        if (executeResult.getFailedCommandIds().isEmpty()) {
            // 返回成功码。
            return Constants.POOF_RESPONSE_CODE_SUCCESS;
        }
        // 如果 executeResult 中至少有一条指令执行失败，返回 POOF_RESPONSE_CODE_FAILED。
        else {
            // 返回失败码。
            return Constants.POOF_RESPONSE_CODE_FAILED;
        }
    }
}
