package ru.practicum.shareit.exception.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.ValidationException;

import java.util.List;

@Slf4j
@RestControllerAdvice
public class ErrorHandler {

    @ExceptionHandler({ValidationException.class, BadRequestException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleValidation(final RuntimeException exception) {
        log.error("Ошибка валидации данных. Error: {}", exception.getMessage());
        return new ErrorResponse(exception.getMessage(), "Ошибка валидации данных");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleValidationExceptions(MethodArgumentNotValidException exception) {
        List<String> errors = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + " : " + error.getDefaultMessage()
                        + " Получены данные: " + error.getRejectedValue())
                .toList();

        String errorMessage = String.join("; ", errors);
        log.warn("Ошибка валидации: {}", errorMessage);

        return new ErrorResponse(errorMessage, "Ошибка валидации данных");
    }

    @ExceptionHandler(Throwable.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleException(final Throwable exception) {
        log.error("Внутренняя ошибка сервера. Error: {}", exception.getMessage());
        return new ErrorResponse(exception.getMessage(), "Внутренняя ошибка сервера");
    }
}