package ru.practicum.shareit.item;

import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemResponseDto;

import java.util.List;

public interface ItemService {
    ItemDto create(Long ownerId, ItemDto itemDto);

    ItemDto update(Long ownerId, Long itemId, ItemDto itemDto);

    ItemResponseDto getById(Long userId, Long itemId);

    List<ItemResponseDto> getAllByOwner(Long ownerId);

    List<ItemDto> search(String text);

    CommentDto addComment(Long userId, Long itemId, CommentDto commentDto);
}