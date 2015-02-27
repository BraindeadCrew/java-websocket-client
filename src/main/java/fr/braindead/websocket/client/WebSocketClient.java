package fr.braindead.websocket.client;

import io.undertow.websockets.core.*;
import org.xnio.*;

import java.io.IOException;
import java.net.URI;

/**
 * Created by leiko on 27/02/15.
 *
 */
public abstract class WebSocketClient implements WebSocketClientHandlers {

    private WebSocketChannel channel;
    private FutureNotifier futureNotifier = new FutureNotifier(this);

    /**
     *
     * @param uri web socket server uri
     * @throws IOException
     * @throws InterruptedException
     */
    public WebSocketClient(URI uri) throws IOException, InterruptedException {
        Xnio xnio = Xnio.getInstance(io.undertow.websockets.client.WebSocketClient.class.getClassLoader());
        XnioWorker worker = xnio.createWorker(OptionMap.builder()
                .set(Options.WORKER_IO_THREADS, 2)
                .set(Options.CONNECTION_HIGH_WATER, 1000000)
                .set(Options.CONNECTION_LOW_WATER, 1000000)
                .set(Options.WORKER_TASK_CORE_THREADS, 30)
                .set(Options.WORKER_TASK_MAX_THREADS, 30)
                .set(Options.TCP_NODELAY, true)
                .set(Options.CORK, true)
                .getMap());
        ByteBufferSlicePool buffer = new ByteBufferSlicePool(BufferAllocator.BYTE_BUFFER_ALLOCATOR, 1024, 1024);
        IoFuture<WebSocketChannel> futureClient = io.undertow.websockets.client.WebSocketClient
                .connect(worker, buffer, OptionMap.EMPTY, uri, WebSocketVersion.V13);
        futureClient.addNotifier(futureNotifier, null);
    }

    /**
     *
     * @param worker XnioWorker
     * @param uri web socket server uri
     * @throws IOException
     * @throws InterruptedException
     */
    public WebSocketClient(XnioWorker worker, URI uri) throws IOException, InterruptedException {
        ByteBufferSlicePool buffer = new ByteBufferSlicePool(BufferAllocator.BYTE_BUFFER_ALLOCATOR, 1024, 1024);
        IoFuture<WebSocketChannel> futureClient = io.undertow.websockets.client.WebSocketClient
                .connect(worker, buffer, OptionMap.EMPTY, uri, WebSocketVersion.V13);
        futureClient.addNotifier(futureNotifier, null);
    }

    /**
     *
     * @param worker XnioWorker
     * @param buffer ByteBufferSlicePool
     * @param uri web socket server uri
     * @throws IOException
     * @throws InterruptedException
     */
    public WebSocketClient(XnioWorker worker, ByteBufferSlicePool buffer, URI uri) throws IOException, InterruptedException {
        IoFuture<WebSocketChannel> futureClient = io.undertow.websockets.client.WebSocketClient
                .connect(worker, buffer, OptionMap.EMPTY, uri, WebSocketVersion.V13);
        futureClient.addNotifier(futureNotifier, null);
    }

    /**
     * Close connection with remote web socket server
     * @throws IOException
     */
    public void close() throws IOException {
        if (this.channel != null) {
            this.channel.sendClose();
        }
    }

    /**
     *
     * @return true if currently connected to remote server
     */
    public boolean isOpen() {
        return this.channel != null && this.channel.isOpen();
    }

    /**
     * Code readability helper
     */
    private class FutureNotifier extends IoFuture.HandlingNotifier<WebSocketChannel, Object> {

        private WebSocketClient client;

        public FutureNotifier(WebSocketClient client) {
            this.client = client;
        }

        @Override
        public void handleFailed(IOException exception, Object attachment) {
            this.client.onError(exception);
        }

        @Override
        public void handleDone(WebSocketChannel channel, Object attachment) {
            this.client.channel = channel;
            this.client.onOpen();

            channel.getReceiveSetter().set(new AbstractReceiveListener() {
                @Override
                protected void onFullTextMessage(WebSocketChannel ws, BufferedTextMessage message) throws IOException {
                    client.onMessage(message.getData());
                }

                @Override
                protected void onError(WebSocketChannel ws, Throwable error) {
                    super.onError(ws, error);
                    client.onError(new Exception(error));
                }
            });

            channel.resumeReceives();
            channel.addCloseTask(ws -> client.onClose(ws.getCloseCode(), ws.getCloseReason()));
        }
    }
}
