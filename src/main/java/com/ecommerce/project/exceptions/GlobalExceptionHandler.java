package com.ecommerce.project.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;


@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse> methodArgumentNotValidException(MethodArgumentNotValidException exception) {

        ApiResponse response = new ApiResponse();

        exception.getBindingResult().getAllErrors().forEach(error -> {

            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();

            response.setStatus("error");

            response.setMessage(errorMessage);

            response.setData(null);

        });

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);

    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse> resourceNotFoundException(ResourceNotFoundException exception) {

        ApiResponse response = new ApiResponse();

        response.setMessage(exception.getMessage());

        response.setStatus("error");

        return new ResponseEntity<ApiResponse>(response, HttpStatus.NOT_FOUND);

    }

    @ExceptionHandler(APIException.class)
    public ResponseEntity<ApiResponse> apiException(APIException exception) {

        ApiResponse response = new ApiResponse();

        response.setMessage(exception.getMessage());

        response.setStatus("error");

        return new ResponseEntity<ApiResponse>(response, HttpStatus.BAD_REQUEST);

    }
}
