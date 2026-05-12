package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.storage.UserStorage;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserStorage userStorage;

    @Override
    public UserDto create(UserDto userDto) {
        log.info("Создание пользователя: {}", userDto);
        if (userStorage.existsByEmail(userDto.getEmail())) {
            log.warn("Попытка создать пользователя с уже существующим email: {}", userDto.getEmail());
            throw new ConflictException("Пользователь с email " + userDto.getEmail() + " уже существует");
        }
        User user = UserMapper.toUser(userDto);
        User created = userStorage.create(user);
        log.debug("Создан пользователь: {}", created);
        return UserMapper.toUserDto(created);
    }

    @Override
    public UserDto update(Long userId, UserDto userDto) {
        log.info("Обновление пользователя с id {}: {}", userId, userDto);
        User existing = userStorage.findById(userId)
                .orElseThrow(() -> {
                    log.error("Пользователь с id {} не найден", userId);
                    return new NotFoundException("Пользователь с id " + userId + " не найден");
                });

        if (userDto.getName() != null) {
            existing.setName(userDto.getName());
        }
        if (userDto.getEmail() != null) {
            if (!userDto.getEmail().equals(existing.getEmail()) && userStorage.existsByEmail(userDto.getEmail())) {
                log.warn("Попытка обновить email на уже существующий: {}", userDto.getEmail());
                throw new ConflictException("Email " + userDto.getEmail() + " уже используется");
            }
            existing.setEmail(userDto.getEmail());
        }

        User updated = userStorage.update(existing);
        log.debug("Обновлён пользователь: {}", updated);
        return UserMapper.toUserDto(updated);
    }

    @Override
    public UserDto getById(Long id) {
        log.info("Получение пользователя с id {}", id);
        User user = userStorage.findById(id)
                .orElseThrow(() -> {
                    log.error("Пользователь с id {} не найден", id);
                    return new NotFoundException("Пользователь с id " + id + " не найден");
                });
        return UserMapper.toUserDto(user);
    }

    @Override
    public List<UserDto> getAll() {
        log.info("Получение всех пользователей");
        return userStorage.findAll().stream()
                .map(UserMapper::toUserDto)
                .collect(Collectors.toList());
    }

    @Override
    public void delete(Long id) {
        log.info("Удаление пользователя с id {}", id);
        userStorage.delete(id);
    }
}