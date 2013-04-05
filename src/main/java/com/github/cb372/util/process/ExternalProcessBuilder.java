package com.github.cb372.util.process;

import com.github.cb372.util.stream.StreamGobblerThreadBuilder;

import java.io.File;
import java.io.IOException;

/**
 * Author: chris
 * Created: 4/5/13
 */
public final class ExternalProcessBuilder {
    private final ProcessBuilder processBuilder;
    private StreamGobblerThreadBuilder stdoutGobblerThreadBuilder = new StreamGobblerThreadBuilder();
    private StreamGobblerThreadBuilder stderrGobblerThreadBuilder = new StreamGobblerThreadBuilder();

    protected ExternalProcessBuilder(ProcessBuilder processBuilder) {
        this.processBuilder = processBuilder;
    }

    /**
     * Set the working directory for the process
     * @param dir working directory
     * @return builder
     */
    public ExternalProcessBuilder withWorkingDirectory(File dir) {
        processBuilder.directory(dir);
        return this;
    }

    /**
     * Delete all environment variables from the process's environment.
     * By default the process will inherit the parent's environment.
     * @return builder
     */
    public ExternalProcessBuilder clearEnvironment() {
        processBuilder.environment().clear();
        return this;
    }

    /**
     * Set an environment variable in the process's environment.
     * If it is already set, it will be overwritten.
     * @param key key
     * @param value value
     * @return builder
     */
    public ExternalProcessBuilder withEnvVar(String key, String value) {
        processBuilder.environment().put(key, value);
        return this;
    }

    /**
     * Redirect the process's stderr to stdout.
     * Note: If this is called, any calls to {@link #processStdErr(StreamGobblerThreadBuilder) processStdErr} will be ignored.
     *
     * @return builder
     */
    public ExternalProcessBuilder redirectingErrorStream() {
        processBuilder.redirectErrorStream(true);
        return this;
    }

    /**
     * Set options for how to handle the process's stdout.
     * By default the stream will be silently consumed and discarded.
     * @param streamGobblerThreadBuilder
     * @return builder
     */
    public ExternalProcessBuilder processStdOut(StreamGobblerThreadBuilder streamGobblerThreadBuilder) {
        this.stdoutGobblerThreadBuilder = streamGobblerThreadBuilder;
        return this;
    }

    /**
     * Set options for how to handle the process's stderr.
     * By default the stream will be silently consumed and discarded.
     * @param streamGobblerThreadBuilder
     * @return
     */
    public ExternalProcessBuilder processStdErr(StreamGobblerThreadBuilder streamGobblerThreadBuilder) {
        this.stderrGobblerThreadBuilder = streamGobblerThreadBuilder;
        return this;
    }

    /**
     * Start the process.
     * @return the started process
     * @throws IOException if the process failed to start
     */
    public ExternalProcess start() throws IOException {
        // Start the process
        Process process = processBuilder.start();

        // Start the output stream processing threads
        Thread stdoutGobblerThread = stdoutGobblerThreadBuilder.build(process.getInputStream());
        stdoutGobblerThread.start();
        if (!processBuilder.redirectErrorStream()) {
            Thread stderrGobblerThread = stderrGobblerThreadBuilder.build(process.getErrorStream());
            stderrGobblerThread.start();
        }

        // return the process
        return new JavaLangProcessWrapper(process);
    }

}

