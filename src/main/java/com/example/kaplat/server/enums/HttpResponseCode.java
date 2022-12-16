package com.example.kaplat.server.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum HttpResponseCode {
    OK_RESPONSE(200),
    CONFLICT_RESPONSE(409);

    private final int responseCode;
}
