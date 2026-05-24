package ru.practicum.shareit.item.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class CommentDto {
    private Long id;
    @NotBlank
    private String text;
    private String authorName;
    private LocalDateTime created;
}