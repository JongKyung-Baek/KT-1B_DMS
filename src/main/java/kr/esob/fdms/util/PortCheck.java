package kr.esob.fdms.util;

import java.io.IOException;
import java.net.Socket;

public class PortCheck {

    private static final String HOST = "localhost";
    private static final int PORT = 39229; // 웹소켓이 실행 중인 포트 번호를 입력해주세요.

    public static boolean isPortListening() {
        Socket socket = null;
        try {
            socket = new Socket(HOST, PORT);
            return true;
        } catch (IOException e) {
            return false;
        } finally {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {
                    // handle exception
                }
            }
        }
    }
}