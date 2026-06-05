package ru.practicum.shareit.booking.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class NewBookingRequest {

    @NotNull(message = "Начало бронирования не может быть пустым")
    LocalDateTime start;

    @NotNull(message = "Конец бронирования не может быть пустым")
    LocalDateTime end;

    @NotNull(message = "Id предмета не может быть пустым")
    Long itemId;
}
