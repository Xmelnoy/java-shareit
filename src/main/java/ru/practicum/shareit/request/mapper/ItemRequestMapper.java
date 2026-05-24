package ru.practicum.shareit.request.mapper;

import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

public class ItemRequestMapper {
    public static ItemRequest toEntity(String description, User requestor) {
        return ItemRequest.builder()
                .description(description)
                .requestor(requestor)
                .created(LocalDateTime.now())
                .build();
    }

    public static ItemRequestResponseDto toDto(ItemRequest request, List<ItemDto> items) {
        return ItemRequestResponseDto.builder()
                .id(request.getId())
                .description(request.getDescription())
                .created(request.getCreated())
                .items(items)
                .build();
    }
}