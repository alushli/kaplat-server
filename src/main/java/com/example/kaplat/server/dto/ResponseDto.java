package com.example.kaplat.server.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.Optional;

@Getter
@Setter
public class ResponseDto {
    private Optional<Integer> result;
    @JsonProperty("error-message")
    private Optional<String> errorMessage;
}
