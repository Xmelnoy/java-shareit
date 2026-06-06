package ru.practicum.shareit.user;

import jakarta.validation.Valid;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.dto.NewUserRequest;
import ru.practicum.shareit.user.dto.UpdateUserRequest;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Slf4j
@Validated
@Data
public class UserController {
    private final UserClient userClient;

    @PostMapping
    public ResponseEntity<Object> createUser(@RequestBody @Valid NewUserRequest request) {
        log.info("POST /users: name={}, email={}", request.getName(), request.getEmail());
        return userClient.addUser(request);
    }

    @PatchMapping("/{userId}")
    public ResponseEntity<Object> updateUser(@PathVariable Long userId,
                                             @Valid @RequestBody UpdateUserRequest request) {
        log.info("PATCH /users/{}", userId);
        return userClient.updateUser(userId, request);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<Object> getUserById(@PathVariable Long userId) {
        log.info("GET /users/{}", userId);
        return userClient.getUser(userId);
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Object> deleteUser(@PathVariable Long userId) {
        log.info("DELETE /users/{}", userId);
        return userClient.deleteUser(userId);
    }

    @GetMapping
    public ResponseEntity<Object> getUsers(@RequestHeader("X-Sharer-User-Id") Long userId,
                                           @RequestParam(name = "from", defaultValue = "0") Integer from,
                                           @RequestParam(name = "size", defaultValue = "10") Integer size) {
        log.info("GET /users: userId={}, from={}, size={}", userId, from, size);
        return userClient.getUsers(userId, from, size);
    }
}
