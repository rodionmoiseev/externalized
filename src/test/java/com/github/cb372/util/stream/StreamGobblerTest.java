package com.github.cb372.util.stream;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;


/**
 * Author: chris
 * Created: 4/5/13
 */
public class StreamGobblerTest {

    Charset utf8 = Charset.forName("UTF-8");

    @Test
    public void handlesEmptyStream() throws IOException {
        InputStream stream = new ByteArrayInputStream(new byte[0]);
        MockListener listener = new MockListener();
        new StreamGobbler(stream, utf8, listener).gobble();

        assertThat(listener.calls.size(), is(0));
    }

    @Test
    public void gobblesStreamAndCallsListenerCallbacks() throws IOException {
        String text = "abc\ndef";
        InputStream stream = new ByteArrayInputStream(text.getBytes(utf8));
        MockListener listener = new MockListener();
        new StreamGobbler(stream, utf8, listener).gobble();

        assertThat(listener.calls.get(0), is("a"));
        assertThat(listener.calls.get(1), is("b"));
        assertThat(listener.calls.get(2), is("c"));
        assertThat(listener.calls.get(3), is("\n"));
        assertThat(listener.calls.get(4), is("abc"));
        assertThat(listener.calls.get(5), is("d"));
        assertThat(listener.calls.get(6), is("e"));
        assertThat(listener.calls.get(7), is("f"));
        assertThat(listener.calls.get(8), is("def"));
    }

    @Test
    public void handlesJapaneseCharsCorrectly() throws IOException {
        String text = "あいう\nかきく";
        InputStream stream = new ByteArrayInputStream(text.getBytes(utf8));
        MockListener listener = new MockListener();
        new StreamGobbler(stream, utf8, listener).gobble();

        assertThat(listener.calls.get(0), is("あ"));
        assertThat(listener.calls.get(1), is("い"));
        assertThat(listener.calls.get(2), is("う"));
        assertThat(listener.calls.get(3), is("\n"));
        assertThat(listener.calls.get(4), is("あいう"));
        assertThat(listener.calls.get(5), is("か"));
        assertThat(listener.calls.get(6), is("き"));
        assertThat(listener.calls.get(7), is("く"));
        assertThat(listener.calls.get(8), is("かきく"));
    }

    @Test
    public void handlesCRLFLineEndingsCorrectly() throws IOException {
        String text = "abc\r\ndef";
        InputStream stream = new ByteArrayInputStream(text.getBytes(utf8));
        MockListener listener = new MockListener();
        new StreamGobbler(stream, utf8, listener).gobble();

        assertThat(listener.calls.get(0), is("a"));
        assertThat(listener.calls.get(1), is("b"));
        assertThat(listener.calls.get(2), is("c"));
        assertThat(listener.calls.get(3), is("\r"));
        assertThat(listener.calls.get(4), is("\n"));
        assertThat(listener.calls.get(5), is("abc"));
        assertThat(listener.calls.get(6), is("d"));
        assertThat(listener.calls.get(7), is("e"));
        assertThat(listener.calls.get(8), is("f"));
        assertThat(listener.calls.get(9), is("def"));
    }

    @Test
    public void handlesEmptyLinesCorrectly() throws IOException {
        String text = "a\n\n\nb\n\n";
        InputStream stream = new ByteArrayInputStream(text.getBytes(utf8));
        MockListener listener = new MockListener();
        new StreamGobbler(stream, utf8, listener).gobble();

        assertThat(listener.calls.get(0), is("a"));
        assertThat(listener.calls.get(1), is("\n"));
        assertThat(listener.calls.get(2), is("a"));
        assertThat(listener.calls.get(3), is("\n"));
        assertThat(listener.calls.get(4), is(""));
        assertThat(listener.calls.get(5), is("\n"));
        assertThat(listener.calls.get(6), is(""));
        assertThat(listener.calls.get(7), is("b"));
        assertThat(listener.calls.get(8), is("\n"));
        assertThat(listener.calls.get(9), is("b"));
        assertThat(listener.calls.get(10), is("\n"));
        assertThat(listener.calls.get(11), is(""));
    }

    static class MockListener implements StreamListener {
        private List<String> calls = new ArrayList<String>();

        @Override
        public void onChar(char c) {
            calls.add(String.valueOf(c));
        }

        @Override
        public void onLine(String line) {
            calls.add(line);
        }
    }
}