package ru.mazhanchiki.severstal.exception;

public class ServiceUnavailableException extends Exception {
    private String service;

    public ServiceUnavailableException(String message, String service) {
        super(message);
        this.service = service;
    }

    @Override
    public String getMessage() {
        return String.format("%s: %s", service, super.getMessage());
    }
}
