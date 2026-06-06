package ru.practicum.shareit.item.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class NewItemRequest {

    @NotNull(message = "Имя не может быть null")
    @NotBlank(message = "Имя не может быть пустым")
    @Size(min = 1, max = 100, message = "Имя должно быть от 1 до 100 символов")
    private String name;

    @NotNull(message = "Описание не может быть null")
    @NotBlank(message = "Описание не может быть пустым")
    @Size(min = 1, max = 2000, message = "Описание должно быть от 1 до 2000 символов")
    private String description;

    @NotNull(message = "Поле 'available' не может быть пустым")
    private Boolean available;

    private Long requestId;
}
