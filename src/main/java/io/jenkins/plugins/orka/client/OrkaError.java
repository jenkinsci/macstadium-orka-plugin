package io.jenkins.plugins.orka.client;

public class OrkaError {
    private String message;

    public String getMessage() {
        return this.message;
    }

    @Override
    public String toString() {
        return this.message;
    }
}