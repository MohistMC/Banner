package com.mohistmc.banner.stackdeobf.http;

// Created by booky10 in StackDeobfuscator (18:16 29.03.23)

import java.net.http.HttpResponse;

public class FailedHttpRequestException extends RuntimeException {

    private final HttpResponse<?> response;

    public FailedHttpRequestException(HttpResponse<?> response) {
        this(null, response);
    }

    public FailedHttpRequestException(String message, HttpResponse<?> response) {
        super((message == null ? "" : message + ": ") + response.toString());
        this.response = response;
    }

    public HttpResponse<?> getResponse() {
        return this.response;
    }
}
