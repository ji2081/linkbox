package com.ji2081.linkbox.exception;

import com.ji2081.linkbox.dto.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.ArrayList;
import java.util.List;

@RestControllerAdvice   // 모든 컨트롤러에서 터진 예외를 여기서 받음
public class GlobalExceptionHandler {
    @ExceptionHandler(BookmarkNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)   // 404
    public ErrorResponse handleNotFound(BookmarkNotFoundException e) {
        return new ErrorResponse("BOOKMARK_NOT_FOUND", e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)   // 400
    public ErrorResponse handleValidation(MethodArgumentNotValidException e) {
        List<String> messages = new ArrayList<>();
        for (FieldError error : e.getBindingResult().getFieldErrors()) {
            messages.add(error.getField() + ": " + error.getDefaultMessage());
        }
        return new ErrorResponse("INVALID_INPUT", String.join(", ", messages));
    }
}
