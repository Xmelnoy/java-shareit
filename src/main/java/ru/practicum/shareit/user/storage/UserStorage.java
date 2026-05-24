package ru.practicum.shareit.user.storage;

import ru.practicum.shareit.user.model.User;

import java.util.List;
import java.util.Optional;

public interface UserStorage {
    User create(User user);

    User update(User user);

    Optional<User> findById(Long id);

    List<User> findAll();

    void delete(Long id);

    boolean existsByEmail(String email);
}