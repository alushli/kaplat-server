package com.example.kaplat.server;

import com.example.kaplat.server.dto.AddArgumentsRequestDto;
import com.example.kaplat.server.dto.ResponseDto;
import com.example.kaplat.server.enums.ErrorMessageController;
import com.example.kaplat.server.enums.ErrorMessageEnum;
import com.example.kaplat.server.enums.HttpResponseCode;
import com.example.kaplat.server.enums.OperationEnum;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.Duration;
import java.time.Instant;
import java.util.*;

@RestController
@RequestMapping("/stack")
public class StackServerController {
    @Autowired
    private ErrorMessageController errorMessageController;

    @Autowired
    private StackController stackController;

    @Autowired
    private ServerController serverController;

    @Autowired
    private RequestCounterController requestCounterController;

    private final Logger logger = LogManager.getLogger("stack-logger");
    private Instant startRequest, endRequest;

    @GetMapping("/size")
    public ResponseEntity<ResponseDto> getStackSize() {
        startRequest = Instant.now();
        ResponseDto result = new ResponseDto();
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();

        try {
            requestCounterController.increaseCounter();
            serverController.setSuccessesResult(result);
            result.setResult(Optional.of(this.stackController.getStackSize()));
            endRequest = Instant.now();
            logger.info("Stack size is {}", this.stackController.getStackSize());
            logger.debug("Stack content (first == top): [{}]", this.stackController.getReverseStackAsList()
                    .toString().replace("[","").replace("]",""));
            return ResponseEntity.status(HttpResponseCode.OK_RESPONSE.getResponseCode()).body(result);
        } catch (Exception e) {
            logger.error("Server encountered an error ! message: {}", result.getErrorMessage().get());
            return ResponseEntity.status(HttpResponseCode.CONFLICT_RESPONSE.getResponseCode()).body(result);
        }
        finally {
            serverController.printLogForRequest(request, Duration.between(startRequest, endRequest).toMillis());
        }
    }

    @PutMapping("/arguments")
    public ResponseEntity<ResponseDto> addArgumentsToStack(@RequestBody AddArgumentsRequestDto arguments) {
        startRequest = Instant.now();
        ResponseDto result = new ResponseDto();
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();

        try {
            requestCounterController.increaseCounter();
            for (int numberToAdd : arguments.getArguments()) {
                this.stackController.pushToStack(numberToAdd);
            }
            serverController.setSuccessesResult(result);
            result.setResult(Optional.of(this.stackController.getStackSize()));
            endRequest = Instant.now();
            logger.info("Adding total of {} argument(s) to the stack | Stack size: {}", arguments.getArguments().size(), this.stackController.getStackSize());
            logger.debug("Adding arguments: {} | Stack size before {} | stack size after {}", arguments.getArguments().toString()
                    .replace("[","").replace("]","").replace(" ",""),
                    this.stackController.getStackSize() - arguments.getArguments().size(), this.stackController.getStackSize());
            return ResponseEntity.status(HttpResponseCode.OK_RESPONSE.getResponseCode()).body(result);
        } catch (Exception e) {
            logger.error("Server encountered an error ! message: {}", result.getErrorMessage().get());
            return ResponseEntity.status(HttpResponseCode.CONFLICT_RESPONSE.getResponseCode()).body(result);
        }
        finally {
            serverController.printLogForRequest(request, Duration.between(startRequest, endRequest).toMillis());
        }
    }

    @GetMapping("/operate")
    public ResponseEntity<ResponseDto> performOperation(@RequestParam(name = "operation") String operation) {
        startRequest = Instant.now();
        ResponseDto result = new ResponseDto();
        OperationEnum operationEnum;
        HttpResponseCode responseCode = null;
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();

        try {
            requestCounterController.increaseCounter();
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
                    List<Integer> removedNumbers = new ArrayList<>();
                    responseCode = this.twoArgumentsOperatorWithStack(operationEnum, operation, result, removedNumbers);
                    logger.info("Performing operation {}. Result is {} | stack size: {}", operation, result.getResult().get(), this.stackController.getStackSize());
                    logger.debug("Performing operation: {}({},{}) = {}", operation, removedNumbers.get(0), removedNumbers.get(1) , result.getResult().get());
                    break;
                case Abs:
                case Fact:
                    List<Integer> removedNumber = new ArrayList<>();
                    responseCode = this.oneArgumentOperatorWithStack(operationEnum, operation, result, removedNumber);
                    logger.info("Performing operation {}. Result is {} | stack size: {}", operation, result.getResult().get(), this.stackController.getStackSize());
                    logger.debug("Performing operation: {}({}) = {}", operation, removedNumber.get(0), result.getResult().get());
                    break;
            }
            endRequest = Instant.now();
            return ResponseEntity.status(responseCode.getResponseCode()).body(result);
        } catch (IllegalArgumentException e) {
            result.setResult(null);
            result.setErrorMessage(Optional.of(errorMessageController.createErrorMessage(ErrorMessageEnum.NO_SUCH_OPERATION, operation,0 , 0)));
            logger.error("Server encountered an error ! message: {}", result.getErrorMessage());
            return ResponseEntity.status(HttpResponseCode.CONFLICT_RESPONSE.getResponseCode()).body(result);
        } catch (Exception e) {
            logger.error("Server encountered an error ! message: {}", result.getErrorMessage().get());
            return ResponseEntity.status(HttpResponseCode.CONFLICT_RESPONSE.getResponseCode()).body(result);
        }
        finally {
            serverController.printLogForRequest(request, Duration.between(startRequest, endRequest).toMillis());
        }
    }

    @DeleteMapping("/arguments")
    public ResponseEntity<ResponseDto> deleteArguments(@RequestParam(name = "count") int totalArgumentsToPop) {
        startRequest = Instant.now();
        ResponseDto result = new ResponseDto();
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();

        try {
            requestCounterController.increaseCounter();
            if (this.stackController.getStackSize() < totalArgumentsToPop) {
                result.setErrorMessage(Optional.of(errorMessageController.createErrorMessage(ErrorMessageEnum.ERROR_OPERATION_INVOKED, null, totalArgumentsToPop, this.stackController.getStackSize())));
                return ResponseEntity.status(HttpResponseCode.CONFLICT_RESPONSE.getResponseCode()).body(result);
            }
            this.stackController.popAmountFromStack(totalArgumentsToPop);
            serverController.setSuccessesResult(result);
            result.setResult(Optional.of(this.stackController.getStackSize()));
            endRequest = Instant.now();
            logger.info("Removing total {} argument(s) from the stack | Stack size: {}", totalArgumentsToPop, this.stackController.getStackSize());
            return ResponseEntity.status(HttpResponseCode.OK_RESPONSE.getResponseCode()).body(result);
        } catch (Exception e) {
            logger.error("Server encountered an error ! message: {}", result.getErrorMessage().get());
            return ResponseEntity.status(HttpResponseCode.CONFLICT_RESPONSE.getResponseCode()).body(result);
        }
        finally {
            serverController.printLogForRequest(request, Duration.between(startRequest, endRequest).toMillis());
        }
    }

    private HttpResponseCode twoArgumentsOperatorWithStack(OperationEnum operation, String operationFromUser, ResponseDto result, List<Integer> removedNumbers) throws Exception {
        if (this.stackController.getStackSize() < 2) {
            result.setResult(null);
            result.setErrorMessage(Optional.of(errorMessageController.createErrorMessage(ErrorMessageEnum.NOT_ENOUGH_ARGUMENTS_WITH_COUNTER, operationFromUser, 2, this.stackController.getStackSize())));
            throw new Exception();
        } else {
            removedNumbers.add(this.stackController.popFromStack());
            removedNumbers.add(this.stackController.popFromStack());
            return serverController.twoArgumentsOperatorExecute(removedNumbers.get(0), removedNumbers.get(1), operation, result);
        }
    }

    private HttpResponseCode oneArgumentOperatorWithStack(OperationEnum operation, String operationFromUser, ResponseDto result, List<Integer> removedNumbers) throws Exception {
        if (this.stackController.getStackSize() == 0) {
            result.setResult(null);
            result.setErrorMessage(Optional.of(errorMessageController.createErrorMessage(ErrorMessageEnum.NOT_ENOUGH_ARGUMENTS_WITH_COUNTER, operationFromUser,1 , this.stackController.getStackSize())));
            throw new Exception();
        } else {
            removedNumbers.add(this.stackController.popFromStack());
            return serverController.oneArgumentOperatorExecute(removedNumbers.get(0), operation, result);
        }
    }
}
