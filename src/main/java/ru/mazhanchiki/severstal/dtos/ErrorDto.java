package ru.mazhanchiki.severstal.dtos;

public class ErrorDto {
    private String error;

    public ErrorDto(String error) {
        this.error = error;
    }

    public static ErrorDto New(String error) {
        return new ErrorDto(error);
    }
}
