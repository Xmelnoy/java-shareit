package ru.practicum.shareit.item.comment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class NewCommentRequest {

    @NotNull(message = "Текст не может быть null")
    @NotBlank(message = "Текст не может быть пустым")
    @Size(min = 1, max = 5000, message = "Текст комментария должен быть от 1 до 5000 символов")
    private String text;
}
