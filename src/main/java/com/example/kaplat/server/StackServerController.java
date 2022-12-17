package com.example.kaplat.server;

import com.example.kaplat.server.dto.AddArgumentsRequestDto;
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
@RequestMapping("/stack")
public class StackServerController {
    @Autowired
    private ErrorMessageController errorMessageController;

    @Autowired
    private StackController stackController;

    @Autowired
    private ServerController serverController;

    @GetMapping("/size")
    public ResponseEntity<ResponseDto> getStackSize() {
        ResponseDto result = new ResponseDto();
        serverController.setSuccessesResult(result);
        result.setResult(Optional.of(this.stackController.getStackSize()));
        return ResponseEntity.status(HttpResponseCode.OK_RESPONSE.getResponseCode()).body(result);
    }

    @PutMapping("/arguments")
    public ResponseEntity<ResponseDto> addArgumentsToStack(@RequestBody AddArgumentsRequestDto arguments) {
        ResponseDto result = new ResponseDto();
        for (int numberToAdd : arguments.getArguments()) {
            this.stackController.pushToStack(numberToAdd);
        }
        serverController.setSuccessesResult(result);
        result.setResult(Optional.of(this.stackController.getStackSize()));
        return ResponseEntity.status(HttpResponseCode.OK_RESPONSE.getResponseCode()).body(result);
    }

    @GetMapping("/operate")
    public ResponseEntity<ResponseDto> performOperation(@RequestParam(name = "operation") String operation) {
        ResponseDto result = new ResponseDto();
        OperationEnum operationEnum;
        HttpResponseCode responseCode = null;

        try {
            operationEnum = serverController.getOperator(operation);
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

    @DeleteMapping("/arguments")
    public ResponseEntity<ResponseDto> deleteArguments(@RequestParam(name = "count") int totalArgumentsToPop) {
        ResponseDto result = new ResponseDto();
        if (this.stackController.getStackSize() < totalArgumentsToPop) {
            result.setErrorMessage(Optional.of(errorMessageController.createErrorMessage(ErrorMessageEnum.ERROR_OPERATION_INVOKED, null,totalArgumentsToPop , this.stackController.getStackSize())));
            return ResponseEntity.status(HttpResponseCode.CONFLICT_RESPONSE.getResponseCode()).body(result);
        }
        this.stackController.popAmountFromStack(totalArgumentsToPop);
        serverController.setSuccessesResult(result);
        result.setResult(Optional.of(this.stackController.getStackSize()));
        return ResponseEntity.status(HttpResponseCode.OK_RESPONSE.getResponseCode()).body(result);
    }

    private HttpResponseCode twoArgumentsOperatorWithStack(OperationEnum operation, String operationFromUser, ResponseDto result) {
        if (this.stackController.getStackSize() < 2) {
            result.setResult(null);
            result.setErrorMessage(Optional.of(errorMessageController.createErrorMessage(ErrorMessageEnum.NOT_ENOUGH_ARGUMENTS_WITH_COUNTER, operationFromUser, 2, this.stackController.getStackSize())));
            return HttpResponseCode.CONFLICT_RESPONSE;
        } else {
            return serverController.twoArgumentsOperatorExecute(this.stackController.popFromStack(), this.stackController.popFromStack(), operation, result);
        }
    }

    private HttpResponseCode oneArgumentOperatorWithStack(OperationEnum operation, String operationFromUser, ResponseDto result) {
        if (this.stackController.getStackSize() == 0) {
            result.setResult(null);
            result.setErrorMessage(Optional.of(errorMessageController.createErrorMessage(ErrorMessageEnum.NOT_ENOUGH_ARGUMENTS_WITH_COUNTER, operationFromUser,1 , this.stackController.getStackSize())));
            return HttpResponseCode.CONFLICT_RESPONSE;
        } else {
            return serverController.oneArgumentOperatorExecute(this.stackController.popFromStack(), operation, result);
        }
    }
}
