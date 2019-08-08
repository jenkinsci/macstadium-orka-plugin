package io.jenkins.plugins.orka.client;

public class OrkaError {
    private String message;

    public String getMessage() {
        return this.message;
    }

    public void setMessage(String message) {
        this.message = message;;
    }

    @Override
    public String toString() {
        return this.message;
    }
}