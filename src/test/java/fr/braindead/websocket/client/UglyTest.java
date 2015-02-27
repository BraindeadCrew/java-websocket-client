package fr.braindead.websocket.client;

import java.io.IOException;
import java.net.URI;

/**
 * Created by leiko on 27/02/15.
 *
 */
public class UglyTest {

    public static void main(String[] args) throws IOException, InterruptedException {
        WebSocketClient client = new WebSocketClient(URI.create("ws://localhost:9050")) {
            @Override
            public void onOpen() {
                System.out.println("open");
            }

            @Override
            public void onMessage(String msg) {
                System.out.println("message: " + msg);
            }

            @Override
            public void onClose(int code, String reason) {
                System.out.println("close (code: " + code + ", reason: " + reason + ")");
            }

            @Override
            public void onError(Exception e) {
                System.out.println("err: " + e.getMessage());
                System.out.println("Client isOpen?: "+this.isOpen());
            }
        };
    }
}
