package com.dwarfeng.meepo.handler.poof;

import com.dwarfeng.meepo.util.Constants;
import com.dwarfeng.subgrade.sdk.exception.HandlerExceptionHelper;
import com.dwarfeng.subgrade.stack.exception.HandlerException;
import org.springframework.stereotype.Component;

import java.io.*;
import java.net.Socket;
import java.util.Objects;

@Component
public class PoofHandlerImpl implements PoofHandler {

    private static final int SOCKET_IN_BUFFER_SIZE = 4096;

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

            // 读取服务端的返回值。
            String executionCodeString = readContent(socketIn);

            // 将返回值转换为整数并返回。
            return Integer.parseInt(executionCodeString);
        } finally {
            if (Objects.nonNull(socket)) {
                socket.close();
            }
        }
    }

    @SuppressWarnings("DuplicatedCode")
    private String readContent(InputStream socketIn) throws Exception {
        Reader reader = new InputStreamReader(socketIn, Constants.CHARSET);
        StringBuilder stringBuilder = new StringBuilder();
        char[] buffer = new char[SOCKET_IN_BUFFER_SIZE];
        int len;
        while ((len = reader.read(buffer)) != -1) {
            stringBuilder.append(buffer, 0, len);
            if (stringBuilder.indexOf(Constants.LINE_SEPARATOR) != -1) {
                break;
            }
        }
        return stringBuilder.toString().trim();
    }

    private void writeContent(OutputStream socketOut, String content) throws Exception {
        Writer writer = new OutputStreamWriter(socketOut, Constants.CHARSET);
        writer.write(content + Constants.LINE_SEPARATOR);
        writer.flush();
    }
}
