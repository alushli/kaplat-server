package com.example.kaplat.server;

import com.example.kaplat.server.dto.ResponseDto;
import com.example.kaplat.server.enums.ErrorMessageController;
import com.example.kaplat.server.enums.ErrorMessageEnum;
import com.example.kaplat.server.enums.HttpResponseCode;
import com.example.kaplat.server.enums.OperationEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
public class ServerController {
    @Autowired
    private ErrorMessageController errorMessageController;

    public HttpResponseCode twoArgumentsOperatorExecute(int argument1, int argument2, OperationEnum operation, ResponseDto result) throws Exception {
        switch (operation) {
            case Plus:
                result.setResult(Optional.of(argument1 + argument2));
                setSuccessesResult(result);
                break;
            case Minus:
                result.setResult(Optional.of(argument1 - argument2));
                setSuccessesResult(result);
                break;
            case Times:
                result.setResult(Optional.of(argument1 * argument2));
                setSuccessesResult(result);
                break;
            case Divide:
                if (argument2 == 0) {
                    result.setResult(null);
                    result.setErrorMessage(Optional.of(errorMessageController.createErrorMessage(ErrorMessageEnum.DIVISION_BY_0, operation.name(), 0, 0)));
                    throw new Exception();
                } else {
                    result.setResult(Optional.of(argument1 / argument2));
                    setSuccessesResult(result);
                }
                break;
            case Pow:
                result.setResult(Optional.of((int) Math.pow(argument1, argument2)));
                setSuccessesResult(result);
                break;
        }
        return HttpResponseCode.OK_RESPONSE;
    }

    public HttpResponseCode oneArgumentOperatorExecute(int argument, OperationEnum operation, ResponseDto result) throws Exception {
        switch (operation) {
            case Abs:
                result.setResult(Optional.of(Math.abs(argument)));
                setSuccessesResult(result);
                break;
            case Fact:
                if (argument < 0) {
                    result.setResult(null);
                    result.setErrorMessage(Optional.of(errorMessageController.createErrorMessage(ErrorMessageEnum.FACTORIAL, operation.name(), 0, 0)));
                    throw new Exception();
                } else {
                    result.setResult(Optional.of(fact(argument)));
                    setSuccessesResult(result);
                }
                break;
        }
        return HttpResponseCode.OK_RESPONSE;
    }

    public void setSuccessesResult(ResponseDto result){
        result.setErrorMessage(null);
    }

    private int fact(int number) {
        int fact = 1;
        for (int i = 2; i <= number; i++) {
            fact = fact * i;
        }
        return fact;
    }

    public OperationEnum getOperator(String operator) {
        OperationEnum operationEnumsArray[] = OperationEnum.values();
        for (OperationEnum operationEnum: operationEnumsArray) {
            if (operationEnum.name().equalsIgnoreCase(operator.toLowerCase()))
                return operationEnum;
        }
        return null;
    }
}