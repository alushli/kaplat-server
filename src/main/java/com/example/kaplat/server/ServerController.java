package com.example.kaplat.server;

import com.example.kaplat.server.dto.AddArgumentsRequestDto;
import com.example.kaplat.server.dto.IndependentCalculationRequestDto;
import com.example.kaplat.server.dto.ResponseDto;
import com.example.kaplat.server.enums.ErrorMessageController;
import com.example.kaplat.server.enums.ErrorMessageEnum;
import com.example.kaplat.server.enums.HttpResponseCode;
import com.example.kaplat.server.enums.OperationEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
public class ServerController {

    @Autowired
    private StackController stackController;

    @Autowired
    private ErrorMessageController errorMessageController;

    @PostMapping("/independent/calculate")
    public ResponseEntity<ResponseDto> independentCalculation(@RequestBody IndependentCalculationRequestDto arguments) {
        ResponseDto result = new ResponseDto();
        OperationEnum operation;
        HttpResponseCode responseCode = null;

        try {
            operation = getOperator(arguments.getOperation());
            if (operation == null) {
                throw new IllegalArgumentException();
            }
            switch (operation) {
                case Plus:
                case Minus:
                case Times:
                case Divide:
                case Pow:
                    responseCode = this.twoArgumentsOperator(arguments, operation, result);
                    break;
                case Abs:
                case Fact:
                    responseCode = this.oneArgumentOperator(arguments, operation, result);
                    break;
            }
            return ResponseEntity.status(responseCode.getResponseCode()).body(result);
        } catch (IllegalArgumentException e) {
            result.setResult(null);
            result.setErrorMessage(Optional.of(errorMessageController.createErrorMessage(ErrorMessageEnum.NO_SUCH_OPERATION, arguments.getOperation(),0 , 0)));
            return ResponseEntity.status(HttpResponseCode.CONFLICT_RESPONSE.getResponseCode()).body(result);
        } catch (Exception e) {
            System.out.println("problem");
            return ResponseEntity.status(HttpResponseCode.CONFLICT_RESPONSE.getResponseCode()).body(result);
        }
    }

    private HttpResponseCode twoArgumentsOperator(IndependentCalculationRequestDto arguments, OperationEnum operation, ResponseDto result) {
        if (arguments.getArguments().size() < 2) {
            result.setResult(null);
            result.setErrorMessage(Optional.of(errorMessageController.createErrorMessage(ErrorMessageEnum.NOT_ENOUGH_ARGUMENTS, arguments.getOperation(), 0, 0)));
            return HttpResponseCode.CONFLICT_RESPONSE;
        } else if (arguments.getArguments().size() > 2) {
            result.setResult(null);
            result.setErrorMessage(Optional.of(errorMessageController.createErrorMessage(ErrorMessageEnum.TOO_MANY_ARGUMENTS, arguments.getOperation(), 0, 0)));
            return HttpResponseCode.CONFLICT_RESPONSE;
        } else {
            return twoArgumentsOperatorExecute(arguments.getArguments().get(0), arguments.getArguments().get(1), operation, result);
        }
    }

    private HttpResponseCode twoArgumentsOperatorExecute(int argument1, int argument2, OperationEnum operation, ResponseDto result) {
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
                    return HttpResponseCode.CONFLICT_RESPONSE;
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

    private HttpResponseCode oneArgumentOperator(IndependentCalculationRequestDto arguments, OperationEnum operation, ResponseDto result) {
        if (arguments.getArguments().size() == 0) {
            result.setResult(null);
            result.setErrorMessage(Optional.of(errorMessageController.createErrorMessage(ErrorMessageEnum.NOT_ENOUGH_ARGUMENTS, arguments.getOperation(),0 , 0)));
            return HttpResponseCode.CONFLICT_RESPONSE;
        } else if (arguments.getArguments().size() > 1) {
            result.setResult(null);
            result.setErrorMessage(Optional.of(errorMessageController.createErrorMessage(ErrorMessageEnum.TOO_MANY_ARGUMENTS, arguments.getOperation(),0 , 0)));
            return HttpResponseCode.CONFLICT_RESPONSE;
        } else {
            return oneArgumentOperatorExecute(arguments.getArguments().get(0), operation, result);
        }
    }

    private HttpResponseCode oneArgumentOperatorExecute(int argument, OperationEnum operation, ResponseDto result) {
        switch (operation) {
            case Abs:
                result.setResult(Optional.of(Math.abs(argument)));
                setSuccessesResult(result);
                break;
            case Fact:
                if (argument < 0) {
                    result.setResult(null);
                    result.setErrorMessage(Optional.of(errorMessageController.createErrorMessage(ErrorMessageEnum.FACTORIAL, operation.name(), 0, 0)));
                    return HttpResponseCode.CONFLICT_RESPONSE;
                } else {
                    result.setResult(Optional.of(fact(argument)));
                    setSuccessesResult(result);
                }
                break;
        }
        return HttpResponseCode.OK_RESPONSE;
    }

    private void setSuccessesResult(ResponseDto result){
        result.setErrorMessage(null);
    }

    private int fact(int number) {
        int fact = 1;
        for (int i = 2; i <= number; i++) {
            fact = fact * i;
        }
        return fact;
    }

    private OperationEnum getOperator(String operator) {
        OperationEnum operationEnumsArray[] = OperationEnum.values();
        for (OperationEnum operationEnum: operationEnumsArray) {
            if (operationEnum.name().equalsIgnoreCase(operator.toLowerCase()))
                return operationEnum;
        }
        return null;
    }

    @GetMapping("/stack/size")
    public ResponseEntity<ResponseDto> getStackSize() {
        ResponseDto result = new ResponseDto();
        setSuccessesResult(result);
        result.setResult(Optional.of(this.stackController.getStackSize()));
        return ResponseEntity.status(HttpResponseCode.OK_RESPONSE.getResponseCode()).body(result);
    }

    @PutMapping("/stack/arguments")
    public ResponseEntity<ResponseDto> addArgumentsToStack(@RequestBody AddArgumentsRequestDto arguments) {
        ResponseDto result = new ResponseDto();
        for (int numberToAdd : arguments.getArguments()) {
            this.stackController.pushToStack(numberToAdd);
        }
        setSuccessesResult(result);
        result.setResult(Optional.of(this.stackController.getStackSize()));
        return ResponseEntity.status(HttpResponseCode.OK_RESPONSE.getResponseCode()).body(result);
    }

    @GetMapping("/stack/operate")
    public ResponseEntity<ResponseDto> performOperation(@RequestParam(name = "operation") String operation) {
        ResponseDto result = new ResponseDto();
        OperationEnum operationEnum;
        HttpResponseCode responseCode = null;

        try {
            operationEnum = getOperator(operation);
            if (operationEnum == null) {
                throw new IllegalArgumentException();
            }
            switch (operationEnum) {
                case Plus:
                case Minus:
                case Times:
                case Divide:
                case Pow:
                    responseCode = this.twoArgumentsOperatorWithStack(operationEnum, operation, result);
                    break;
                case Abs:
                case Fact:
                    responseCode = this.oneArgumentOperatorWithStack(operationEnum, operation, result);
                    break;
            }
            return ResponseEntity.status(responseCode.getResponseCode()).body(result);
        } catch (IllegalArgumentException e) {
            result.setResult(null);
            result.setErrorMessage(Optional.of(errorMessageController.createErrorMessage(ErrorMessageEnum.NO_SUCH_OPERATION, operation,0 , 0)));
            return ResponseEntity.status(HttpResponseCode.CONFLICT_RESPONSE.getResponseCode()).body(result);
        } catch (Exception e) {
            System.out.println("problem");
            return ResponseEntity.status(HttpResponseCode.CONFLICT_RESPONSE.getResponseCode()).body(result);
        }
    }

    private HttpResponseCode twoArgumentsOperatorWithStack(OperationEnum operation, String operationFromUser, ResponseDto result) {
        if (this.stackController.getStackSize() < 2) {
            result.setResult(null);
            result.setErrorMessage(Optional.of(errorMessageController.createErrorMessage(ErrorMessageEnum.NOT_ENOUGH_ARGUMENTS_WITH_COUNTER, operationFromUser, 2, this.stackController.getStackSize())));
            return HttpResponseCode.CONFLICT_RESPONSE;
        } else {
            return twoArgumentsOperatorExecute(this.stackController.popFromStack(), this.stackController.popFromStack(), operation, result);
        }
    }

    private HttpResponseCode oneArgumentOperatorWithStack(OperationEnum operation, String operationFromUser, ResponseDto result) {
        if (this.stackController.getStackSize() == 0) {
            result.setResult(null);
            result.setErrorMessage(Optional.of(errorMessageController.createErrorMessage(ErrorMessageEnum.NOT_ENOUGH_ARGUMENTS_WITH_COUNTER, operationFromUser,1 , this.stackController.getStackSize())));
            return HttpResponseCode.CONFLICT_RESPONSE;
        } else {
            return oneArgumentOperatorExecute(this.stackController.popFromStack(), operation, result);
        }
    }

    @DeleteMapping("/stack/arguments")
    public ResponseEntity<ResponseDto> deleteArguments(@RequestParam(name = "count") int totalArgumentsToPop) {
        ResponseDto result = new ResponseDto();
        if (this.stackController.getStackSize() < totalArgumentsToPop) {
            result.setErrorMessage(Optional.of(errorMessageController.createErrorMessage(ErrorMessageEnum.ERROR_OPERATION_INVOKED, null,totalArgumentsToPop , this.stackController.getStackSize())));
            return ResponseEntity.status(HttpResponseCode.CONFLICT_RESPONSE.getResponseCode()).body(result);
        }
        this.stackController.popAmountFromStack(totalArgumentsToPop);
        setSuccessesResult(result);
        result.setResult(Optional.of(this.stackController.getStackSize()));
        return ResponseEntity.status(HttpResponseCode.OK_RESPONSE.getResponseCode()).body(result);
    }
}