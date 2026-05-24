package ru.practicum.shareit.request;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;

import java.util.List;

@RestController
@RequestMapping(path = "/requests")
@RequiredArgsConstructor
public class ItemRequestController {
    private final ItemRequestService requestService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ItemRequestResponseDto create(@RequestHeader("X-Sharer-User-Id") Long userId,
                                         @Valid @RequestBody ItemRequestCreateDto createDto) {
        return requestService.create(userId, createDto);
    }

    @GetMapping
    public List<ItemRequestResponseDto> getUserRequests(@RequestHeader("X-Sharer-User-Id") Long userId) {
        return requestService.getUserRequests(userId);
    }

    @GetMapping("/all")
    public List<ItemRequestResponseDto> getAllRequests(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                       @RequestParam(defaultValue = "0") Integer from,
                                                       @RequestParam(defaultValue = "10") Integer size) {
        return requestService.getAllRequests(userId, from, size);
    }

    @GetMapping("/{requestId}")
    public ItemRequestResponseDto getRequestById(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                 @PathVariable Long requestId) {
        return requestService.getRequestById(userId, requestId);
    }
}