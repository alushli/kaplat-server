package com.example.kaplat.server.ApiControllers;

import com.example.kaplat.server.ServerController;
import com.example.kaplat.server.dto.IndependentCalculationRequestDto;
import com.example.kaplat.server.dto.ResponseDto;
import com.example.kaplat.server.enums.ErrorMessageController;
import com.example.kaplat.server.enums.ErrorMessageEnum;
import com.example.kaplat.server.enums.HttpResponseCode;
import com.example.kaplat.server.enums.OperationEnum;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Optional;

@RestController
@RequestMapping("/independent")
public class IndependentCalculateSeverController {
    @Autowired
    private ErrorMessageController errorMessageController;

    @Autowired
    private ServerController serverController;

    private final Logger logger = LogManager.getLogger("independent-logger");

    @PostMapping("/calculate")
    public ResponseEntity<ResponseDto> independentCalculation(@RequestBody IndependentCalculationRequestDto arguments) {
        ResponseDto result = new ResponseDto();
        OperationEnum operation;
        HttpResponseCode responseCode = null;

        try {
            operation = serverController.getOperator(arguments.getOperation());
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
                    logger.info("Performing operation {}. Result is {}", arguments.getOperation(), result.getResult().get());
                    logger.debug("Performing operation: {}({},{}) = {}", arguments.getOperation(), arguments.getArguments().get(0), arguments.getArguments().get(1), result.getResult().get());
                    break;
                case Abs:
                case Fact:
                    responseCode = this.oneArgumentOperator(arguments, operation, result);
                    logger.info("Performing operation {}. Result is {}", arguments.getArguments(), result.getResult().get());
                    logger.debug("Performing operation: {}({}) = {}", operation, arguments.getArguments().get(0), result.getResult());
                    break;
            }
            return ResponseEntity.status(responseCode.getResponseCode()).body(result);
        } catch (IllegalArgumentException e) {
            result.setResult(null);
            result.setErrorMessage(Optional.of(errorMessageController.createErrorMessage(ErrorMessageEnum.NO_SUCH_OPERATION, arguments.getOperation(),0 , 0)));
            logger.error("Server encountered an error ! message: {}", result.getErrorMessage().get());
            return ResponseEntity.status(HttpResponseCode.CONFLICT_RESPONSE.getResponseCode()).body(result);
        } catch (Exception e) {
            logger.error("Server encountered an error ! message: {}", result.getErrorMessage().get());
            return ResponseEntity.status(HttpResponseCode.CONFLICT_RESPONSE.getResponseCode()).body(result);
        }
    }

    private HttpResponseCode twoArgumentsOperator(IndependentCalculationRequestDto arguments, OperationEnum operation, ResponseDto result) throws Exception {
        if (arguments.getArguments().size() < 2) {
            result.setResult(null);
            result.setErrorMessage(Optional.of(errorMessageController.createErrorMessage(ErrorMessageEnum.NOT_ENOUGH_ARGUMENTS, arguments.getOperation(), 0, 0)));
            return HttpResponseCode.CONFLICT_RESPONSE;
        } else if (arguments.getArguments().size() > 2) {
            result.setResult(null);
            result.setErrorMessage(Optional.of(errorMessageController.createErrorMessage(ErrorMessageEnum.TOO_MANY_ARGUMENTS, arguments.getOperation(), 0, 0)));
            return HttpResponseCode.CONFLICT_RESPONSE;
        } else {
            return serverController.twoArgumentsOperatorExecute(arguments.getArguments().get(0), arguments.getArguments().get(1), operation, result);
        }
    }

    private HttpResponseCode oneArgumentOperator(IndependentCalculationRequestDto arguments, OperationEnum operation, ResponseDto result) throws Exception {
        if (arguments.getArguments().size() == 0) {
            result.setResult(null);
            result.setErrorMessage(Optional.of(errorMessageController.createErrorMessage(ErrorMessageEnum.NOT_ENOUGH_ARGUMENTS, arguments.getOperation(),0 , 0)));
            return HttpResponseCode.CONFLICT_RESPONSE;
        } else if (arguments.getArguments().size() > 1) {
            result.setResult(null);
            result.setErrorMessage(Optional.of(errorMessageController.createErrorMessage(ErrorMessageEnum.TOO_MANY_ARGUMENTS, arguments.getOperation(),0 , 0)));
            return HttpResponseCode.CONFLICT_RESPONSE;
        } else {
            return serverController.oneArgumentOperatorExecute(arguments.getArguments().get(0), operation, result);
        }
    }
}
