package ru.mazhanchiki.severstal.exception;

public class TendersNotFoundException extends Exception {
    private final String link;
    public TendersNotFoundException(String message, String link) {
        super(message);
        this.link = link;
    }

    public String getLink() {
        return link;
    }
}
