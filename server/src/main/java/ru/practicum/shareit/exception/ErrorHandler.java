package ru.practicum.shareit.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.NoSuchElementException;

@Slf4j
@RestControllerAdvice
public class ErrorHandler {

    // конфликт данных (дубль email и т.д.)
    @ExceptionHandler(ConflictException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleConflict(ConflictException e) {
        log.warn("Конфликт: {}", e.getMessage());
        return new ErrorResponse(e.getMessage());
    }

    // доступ запрещён (не владелец и т.д.)
    @ExceptionHandler(ForbiddenException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ErrorResponse handleForbidden(ForbiddenException e) {
        log.warn("Доступ запрещён: {}", e.getMessage());
        return new ErrorResponse(e.getMessage());
    }

    // сущность не найдена в БД
    @ExceptionHandler(NoSuchElementException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleNotFound(NoSuchElementException e) {
        log.warn("Не найдено: {}", e.getMessage());
        return new ErrorResponse(e.getMessage());
    }

    // бизнес-логика (вещь недоступна, комментарий без букинга и т.д.)
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleIllegalArgument(IllegalArgumentException e) {
        log.warn("Ошибка бизнес-логики: {}", e.getMessage());
        return new ErrorResponse(e.getMessage());
    }

    // всё остальное
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleGenericException(Exception e) {
        log.error("Необработанная ошибка", e);
        return new ErrorResponse("Внутренняя ошибка сервера");
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleMissingHeader(MissingRequestHeaderException e) {
        log.warn("Отсутствует заголовок: {}", e.getHeaderName());
        return new ErrorResponse("Отсутствует обязательный заголовок: " + e.getHeaderName());
    }
}