package ru.practicum.shareit.item;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemController {
    private final ItemService itemService;

    @PostMapping
    public ResponseEntity<ItemDto> create(@RequestHeader("X-Sharer-User-Id") Long userId,
                                          @Valid @RequestBody ItemDto itemDto) {
        log.info("POST /items - создание вещи, userId={}", userId);
        ItemDto created = itemService.create(userId, itemDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PatchMapping("/{itemId}")
    public ResponseEntity<ItemDto> update(@RequestHeader("X-Sharer-User-Id") Long userId,
                                          @PathVariable Long itemId,
                                          @RequestBody ItemDto itemDto) {
        log.info("PATCH /items/{} - обновление вещи, userId={}", itemId, userId);
        ItemDto updated = itemService.update(userId, itemId, itemDto);
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<ItemDto> getById(@PathVariable Long itemId) {
        log.info("GET /items/{} - получение вещи", itemId);
        ItemDto item = itemService.getById(itemId);
        return ResponseEntity.ok(item);
    }

    @GetMapping
    public ResponseEntity<List<ItemDto>> getAllByOwner(@RequestHeader("X-Sharer-User-Id") Long userId) {
        log.info("GET /items - получение всех вещей владельца, userId={}", userId);
        List<ItemDto> items = itemService.getAllByOwner(userId);
        return ResponseEntity.ok(items);
    }

    @GetMapping("/search")
    public ResponseEntity<List<ItemDto>> search(@RequestParam String text) {
        log.info("GET /items/search?text={} - поиск вещей", text);
        List<ItemDto> items = itemService.search(text);
        return ResponseEntity.ok(items);
    }
}