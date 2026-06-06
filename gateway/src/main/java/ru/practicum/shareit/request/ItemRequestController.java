package ru.practicum.shareit.request;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.NewItemRequestReq;

@RestController
@RequestMapping(path = "/requests")
@RequiredArgsConstructor
@Slf4j
@Validated
public class ItemRequestController {
    private final ItemRequestClient itemRequestClient;

    @PostMapping
    public ResponseEntity<Object> addRequest(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                     @Valid @RequestBody NewItemRequestReq request) {
        log.info("POST /requests: userId={}", userId);
        return itemRequestClient.addRequest(userId, request);
    }

    @GetMapping
    public ResponseEntity<Object> getUserRequests(@RequestHeader("X-Sharer-User-Id") Long userId) {
        log.info("GET /requests: userId={}", userId);
        return itemRequestClient.getUserRequests(userId);
    }

    @GetMapping("/all")
    public ResponseEntity<Object> getRequestAll(@RequestHeader("X-Sharer-User-Id") Long userId) {
        log.info("GET /requests/all: userId={}", userId);
        return itemRequestClient.getAllRequests(userId);
    }

    @GetMapping("/{requestId}")
    public ResponseEntity<Object> getRequestById(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                         @PathVariable("requestId") Long requestId) {
        log.info("GET /requests/{}: userId={}", requestId, userId);
        return itemRequestClient.getRequest(userId, requestId);
    }
}
