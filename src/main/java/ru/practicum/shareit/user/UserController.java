package ru.practicum.shareit.user;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;

@Slf4j
@RestController
@RequestMapping(path = "/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PostMapping
    public ResponseEntity<UserDto> create(@Valid @RequestBody UserDto userDto) {
        log.info("POST /users - создание пользователя");
        UserDto created = userService.create(userDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PatchMapping("/{userId}")
    public ResponseEntity<UserDto> update(@PathVariable Long userId, @RequestBody UserDto userDto) {
        log.info("PATCH /users/{} - обновление пользователя", userId);
        UserDto updated = userService.update(userId, userDto);
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserDto> getById(@PathVariable Long userId) {
        log.info("GET /users/{} - получение пользователя", userId);
        UserDto user = userService.getById(userId);
        return ResponseEntity.ok(user);
    }

    @GetMapping
    public ResponseEntity<List<UserDto>> getAll() {
        log.info("GET /users - получение всех пользователей");
        List<UserDto> users = userService.getAll();
        return ResponseEntity.ok(users);
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> delete(@PathVariable Long userId) {
        log.info("DELETE /users/{} - удаление пользователя", userId);
        userService.delete(userId);
        return ResponseEntity.noContent().build();
    }
}