package org.example.exception;

public class TwoFactorRequiredException extends RuntimeException {
    private final String partialToken;

    public TwoFactorRequiredException(String message, String partialToken) {
        super(message);
        this.partialToken = partialToken;
    }

    public String getPartialToken() {
        return partialToken;
    }
}
