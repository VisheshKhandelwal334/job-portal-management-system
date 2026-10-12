package com.jobportal.exception;

/** Single checked exception type used across the DAO / UI layers. */
public class PortalException extends Exception {
    private static final long serialVersionUID = 1L;

    public PortalException(String message) {
        super(message);
    }

    public PortalException(String message, Throwable cause) {
        super(message, cause);
    }
}
