package com.example.kaplat.server.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.ArrayList;

@AllArgsConstructor
@Getter
public class IndependentCalculationRequestDto {
    private ArrayList<Integer> arguments;
    private String operation;
}
