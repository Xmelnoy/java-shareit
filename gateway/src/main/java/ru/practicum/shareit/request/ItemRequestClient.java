package ru.practicum.shareit.request;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.shareit.client.BaseClient;
import ru.practicum.shareit.request.dto.NewItemRequestReq;

@Slf4j
@Service
public class ItemRequestClient extends BaseClient {
    private static final String API_PREFIX = "/requests";

    public ItemRequestClient(@Value("${shareit-server.url}") String serverUrl, RestTemplateBuilder builder) {
        super(
                builder
                        .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl + API_PREFIX))
                        .requestFactory(() -> new HttpComponentsClientHttpRequestFactory())
                        .build()
        );
    }

    public ResponseEntity<Object> addRequest(Long userId, NewItemRequestReq request) {
        log.debug("addRequest: userId={}", userId);
        return post("", userId, request);
    }

    public ResponseEntity<Object> getUserRequests(Long userId) {
        log.debug("getUserRequests: userId={}", userId);
        return get("", userId, null);
    }

    public ResponseEntity<Object> getAllRequests(Long userId) {
        log.debug("getAllRequests: userId={}", userId);
        return get("/all", userId, null);
    }

    public ResponseEntity<Object> getRequest(Long userId, Long requestId) {
        log.debug("getRequest: userId={}, requestId={}", userId, requestId);
        return get("/" + requestId, userId, null);
    }
}