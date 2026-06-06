package ru.practicum.shareit.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.shareit.client.BaseClient;
import ru.practicum.shareit.user.dto.NewUserRequest;
import ru.practicum.shareit.user.dto.UpdateUserRequest;

import java.util.Map;

@Slf4j
@Service
public class UserClient extends BaseClient {
    private static final String API_PREFIX = "/users";

    public UserClient(@Value("${shareit-server.url}") String serverUrl, RestTemplateBuilder builder) {
        super(
                builder
                        .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl + API_PREFIX))
                        .requestFactory(() -> new HttpComponentsClientHttpRequestFactory())
                        .build()
        );
    }

    public ResponseEntity<Object> addUser(NewUserRequest request) {
        log.debug("addUser: name={}, email={}", request.getName(), request.getEmail());
        return post("", null, request);
    }

    public ResponseEntity<Object> updateUser(Long userId, UpdateUserRequest request) {
        log.debug("updateUser: userId={}", userId);
        return patch("/" + userId, userId, request);
    }

    public ResponseEntity<Object> deleteUser(Long userId) {
        log.debug("deleteUser: userId={}", userId);
        return delete("/" + userId, userId);
    }

    public ResponseEntity<Object> getUser(Long userId) {
        log.debug("getUser: userId={}", userId);
        return get("/" + userId, userId, null);
    }

    public ResponseEntity<Object> getUsers(Long userId, Integer from, Integer size) {
        log.debug("getUsers: userId={}, from={}, size={}", userId, from, size);
        Map<String, Object> parameters = Map.of(
                "from", from,
                "size", size
        );
        return get("?from={from}&size={size}", userId, parameters);
    }
}