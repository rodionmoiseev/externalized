package com.github.cb372.util.stream;

import com.github.cb372.util.stream.listener.binary.ByteStreamListener;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * Author: chris
 * Created: 9/29/13
 */
public class ByteStreamProcessor implements StreamProcessor {
    private final InputStream stream;
    private final List<ByteStreamListener> listeners;
    private final byte[] buffer;

    public ByteStreamProcessor(InputStream stream, List<ByteStreamListener> listeners, int bufferSize) {
        this.stream = stream;
        this.listeners = listeners;
        this.buffer = new byte[bufferSize];
    }

    @Override
    public void run() throws IOException {
        int bytesRead;
        while ((bytesRead = stream.read(buffer)) != -1) {
            onBytes(buffer, bytesRead);
        }
        onEOS();
    }

    private void onBytes(byte[] buffer, int bytesRead) {
        for (ByteStreamListener listener : listeners) {
            listener.onBytes(buffer, 0, bytesRead);
        }
    }

    private void onEOS() {
        for (ByteStreamListener listener : listeners) {
            listener.onEndOfStream();
        }
    }

}
