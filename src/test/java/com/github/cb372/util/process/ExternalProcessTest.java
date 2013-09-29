package com.github.cb372.util.process;


import com.github.cb372.util.stream.listener.text.TextOutputCollectingListener;
import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.concurrent.TimeUnit;

import static com.github.cb372.util.process.StreamProcessing.consume;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertThat;

/**
 * Author: chris
 * Created: 4/5/13
 */
public class ExternalProcessTest {

    @Test
    public void processReturnsCorrectExitCode() throws IOException, InterruptedException {
        ExternalProcess process = Command.parse("src/test/resources/myscript.sh").start();
        assertThat(process.waitFor(), equalTo(123));
    }

    @Test
    public void processOutputIsPassedToListeners() throws IOException, InterruptedException {
        TextOutputCollectingListener stdoutListener = new TextOutputCollectingListener();
        TextOutputCollectingListener stderrListener = new TextOutputCollectingListener();

        ExternalProcess process = Command.parse("src/test/resources/myscript.sh")
                .processStdOut(consume().asText().withListener(stdoutListener))
                .processStdErr(consume().asText().withListener(stderrListener))
                .start();

        process.waitFor();
        assertThat(stdoutListener.awaitOutputCollection(1, TimeUnit.SECONDS), is(true));
        assertThat(stderrListener.awaitOutputCollection(1, TimeUnit.SECONDS), is(true));

        assertThat(stdoutListener.getTextOutput().get(0), equalTo("hello"));
        assertThat(stdoutListener.getTextOutput().get(1), equalTo("world"));
        assertThat(stderrListener.getTextOutput().get(0), equalTo("oh noes"));
    }

    @Test
    public void canRedirectStderrToStdout() throws IOException, InterruptedException {
        TextOutputCollectingListener stdoutListener = new TextOutputCollectingListener();

        ExternalProcess process = Command.parse("src/test/resources/myscript.sh")
                .processStdOut(consume().asText().withListener(stdoutListener))
                .redirectingErrorStream()
                .start();

        process.waitFor();
        assertThat(stdoutListener.awaitOutputCollection(1, TimeUnit.SECONDS), is(true));

        assertThat(stdoutListener.getTextOutput().get(0), equalTo("hello"));
        assertThat(stdoutListener.getTextOutput().get(1), equalTo("world"));
        assertThat(stdoutListener.getTextOutput().get(2), equalTo("oh noes"));
    }

    @Test
    public void canSetEnvironmentVariables() throws IOException, InterruptedException {
        TextOutputCollectingListener stdoutListener = new TextOutputCollectingListener();

        ExternalProcess process = Command.parse("src/test/resources/echo-foo.sh")
                .withEnvVar("FOO", "bar")
                .processStdOut(consume().asText().withListener(stdoutListener))
                .start();
        process.waitFor();

        assertThat(stdoutListener.awaitOutputCollection(1, TimeUnit.SECONDS), is(true));
        assertThat(stdoutListener.getTextOutput().get(0), equalTo("bar"));
    }

    @Test
    public void collectsProcessOutputIfToldTo() throws IOException, InterruptedException {
        TextCollectingExternalProcess process = Command.parse("src/test/resources/myscript.sh")
                .collectStdOut()
                .start();
        process.waitFor();

        assertThat(process.getTextOutput().size(), equalTo(2));
        assertThat(process.getTextOutput().get(0), equalTo("hello"));
        assertThat(process.getTextOutput().get(1), equalTo("world"));
    }

    @Test
    public void doesNotCollectProcessOutputUnlessToldTo() throws IOException, InterruptedException {
        TextCollectingExternalProcess process = Command.parse("src/test/resources/myscript.sh")
                .start();
        process.waitFor();

        assertThat(process.getTextOutput().isEmpty(), is(true));
    }

    @Test
    public void canHandleBinaryStdinAndStdout() throws IOException, InterruptedException {
        BinaryOutputCollectingExternalProcess process = Command.parse("src/test/resources/roundtrip-binary.sh")
                .processStdOut(consume().asBinary())
                .collectStdOut()
                .start();

        IOUtils.copy(getResourceAsStream("yohkan.jpg"), process.getStdIn());
        process.getStdIn().close();
        process.waitFor();

        byte[] result = process.getBinaryOutput();
        assertThat(result.length, equalTo(17660));
        assertThat(result, equalTo(getResourceAsByteArray("yohkan.jpg")));

    }

    private InputStream getResourceAsStream(String path) {
        return getClass().getClassLoader().getResourceAsStream(path);
    }

    private byte[] getResourceAsByteArray(String path) throws IOException {
        InputStream stream = getResourceAsStream(path);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        IOUtils.copy(stream, baos);
        return baos.toByteArray();
    }
}
