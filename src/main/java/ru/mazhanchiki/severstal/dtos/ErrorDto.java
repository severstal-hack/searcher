package ru.mazhanchiki.severstal.dtos;

import lombok.Getter;

@Getter
public class ErrorDto {
    private String error;

    public ErrorDto(String error) {
        this.error = error;
    }

    public static ErrorDto New(String error) {
        return new ErrorDto(error);
    }

}

