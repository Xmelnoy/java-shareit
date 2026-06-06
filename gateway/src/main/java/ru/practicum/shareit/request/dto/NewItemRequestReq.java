package ru.practicum.shareit.request.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class NewItemRequestReq {
    @NotNull(message = "Описание не может быть пустым")
    @Size(min = 1, max = 2000, message = "Описание должно быть от 1 до 2000 символов")
    private String description;

    private LocalDateTime created = LocalDateTime.now();
}
