package fr.braindead.websocket.client;

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.URI;

import static org.junit.Assert.*;

/**
 * Unit test for WebSocketClient
 */
public class WebSocketClientTest {

    private WebSocketClient client;

    @Before
    public void setup() throws IOException, InterruptedException {
        client = new WebSocketClient(URI.create("ws://localhost:9050")) {
            @Override
            public void onOpen() {

            }

            @Override
            public void onMessage(String msg) {

            }

            @Override
            public void onClose(int code, String reason) {

            }

            @Override
            public void onError(Exception e) {

            }
        };
    }

    @Test
    public void testConnect() {
//        assertTrue(client.isOpen());
    }

    @Test
    public void testClose() throws IOException, InterruptedException {
//        client.close();
//        Thread.sleep(200);
        assertFalse(client.isOpen());
    }
}
