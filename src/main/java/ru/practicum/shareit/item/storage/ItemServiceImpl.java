package ru.practicum.shareit.item.storage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.storage.UserStorage;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final ItemStorage itemStorage;
    private final UserStorage userStorage;

    @Override
    public ItemDto create(Long userId, ItemDto itemDto) {
        log.info("Создание вещи для пользователя с id {}: {}", userId, itemDto);
        User owner = userStorage.findById(userId)
                .orElseThrow(() -> {
                    log.error("Пользователь с id {} не найден", userId);
                    return new NotFoundException("Пользователь с id " + userId + " не найден");
                });

        Item item = ItemMapper.toItem(itemDto);
        item.setOwner(owner);

        Item created = itemStorage.create(item);
        log.debug("Создана вещь: {}", created);
        return ItemMapper.toItemDto(created);
    }

    @Override
    public ItemDto update(Long userId, Long itemId, ItemDto itemDto) {
        log.info("Обновление вещи с id {} от пользователя с id {}: {}", itemId, userId, itemDto);
        User owner = userStorage.findById(userId)
                .orElseThrow(() -> {
                    log.error("Пользователь с id {} не найден", userId);
                    return new NotFoundException("Пользователь с id " + userId + " не найден");
                });

        Item existing = itemStorage.findById(itemId)
                .orElseThrow(() -> {
                    log.error("Вещь с id {} не найдена", itemId);
                    return new NotFoundException("Вещь с id " + itemId + " не найдена");
                });

        if (!existing.getOwner().getId().equals(owner.getId())) {
            log.warn("Пользователь с id {} пытается изменить вещь другого владельца", userId);
            throw new NotFoundException("Пользователь не является владельцем вещи");
        }

        if (itemDto.getName() != null) {
            existing.setName(itemDto.getName());
        }
        if (itemDto.getDescription() != null) {
            existing.setDescription(itemDto.getDescription());
        }
        if (itemDto.getAvailable() != null) {
            existing.setAvailable(itemDto.getAvailable());
        }

        Item updated = itemStorage.update(existing);
        log.debug("Обновлена вещь: {}", updated);
        return ItemMapper.toItemDto(updated);
    }

    @Override
    public ItemDto getById(Long itemId) {
        log.info("Получение вещи с id {}", itemId);
        Item item = itemStorage.findById(itemId)
                .orElseThrow(() -> {
                    log.error("Вещь с id {} не найдена", itemId);
                    return new NotFoundException("Вещь с id " + itemId + " не найдена");
                });
        return ItemMapper.toItemDto(item);
    }

    @Override
    public List<ItemDto> getAllByOwner(Long userId) {
        log.info("Получение всех вещей владельца с id {}", userId);
        userStorage.findById(userId)
                .orElseThrow(() -> {
                    log.error("Пользователь с id {} не найден", userId);
                    return new NotFoundException("Пользователь с id " + userId + " не найден");
                });
        return itemStorage.findAllByOwner(userId).stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemDto> search(String text) {
        log.info("Поиск вещей по тексту: {}", text);
        return itemStorage.searchAvailable(text).stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }
}