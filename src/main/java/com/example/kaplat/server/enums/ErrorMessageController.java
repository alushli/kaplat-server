package com.example.kaplat.server.enums;

import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Component;

@Component
@NoArgsConstructor
@Setter
public class ErrorMessageController {
    public String createErrorMessage(ErrorMessageEnum errorMessageEnum, String operationName, int requiresArgumentsCount, int totalArgumentsCount) {
        switch (errorMessageEnum) {
            case DIVISION_BY_0:
                return String.format("Error while performing operation Divide: division by 0");
            case FACTORIAL:
                return String.format("Error while performing operation Factorial: not supported for the negative number");
            case NO_SUCH_OPERATION:
                return String.format("Error: unknown operation: %s", operationName);
            case NOT_ENOUGH_ARGUMENTS:
                return String.format("Error: Not enough arguments to perform the operation %s", operationName);
            case TOO_MANY_ARGUMENTS:
                return String.format("Error: Too many arguments to perform the operation %s", operationName);
            case NOT_ENOUGH_ARGUMENTS_WITH_COUNTER:
                return String.format("Error: cannot implement operation %s. It requires %d arguments and the stack has only %d arguments", operationName, requiresArgumentsCount, totalArgumentsCount);
            case ERROR_OPERATION_INVOKED:
                return String.format("Error: cannot remove %d from the stack. It has only %d arguments", requiresArgumentsCount, totalArgumentsCount);
            default: return null;
        }
    }
}