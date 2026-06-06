package ru.practicum.shareit.item;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.shareit.client.BaseClient;
import ru.practicum.shareit.item.comment.dto.NewCommentRequest;
import ru.practicum.shareit.item.dto.NewItemRequest;
import ru.practicum.shareit.item.dto.UpdateItemRequest;

import java.util.Map;

@Slf4j
@Service
public class ItemClient extends BaseClient {
    private static final String API_PREFIX = "/items";

    public ItemClient(@Value("${shareit-server.url}") String serverUrl, RestTemplateBuilder builder) {
        super(
                builder
                        .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl + API_PREFIX))
                        .requestFactory(() -> new HttpComponentsClientHttpRequestFactory())
                        .build()
        );
    }

    public ResponseEntity<Object> addItem(Long userId, NewItemRequest request) {
        log.debug("addItem: userId={}, name={}", userId, request.getName());
        return post("", userId, request);
    }

    public ResponseEntity<Object> updateItem(Long userId, Long itemId, UpdateItemRequest request) {
        log.debug("updateItem: userId={}, itemId={}", userId, itemId);
        return patch("/" + itemId, userId, request);
    }

    public ResponseEntity<Object> deleteItem(Long userId, Long itemId) {
        log.debug("deleteItem: userId={}, itemId={}", userId, itemId);
        return delete("/" + itemId, userId);
    }

    public ResponseEntity<Object> getItem(Long userId, Long itemId) {
        log.debug("getItem: userId={}, itemId={}", userId, itemId);
        return get("/" + itemId, userId, null);
    }

    public ResponseEntity<Object> getItems(Long userId, Integer from, Integer size) {
        log.debug("getItems: userId={}, from={}, size={}", userId, from, size);
        Map<String, Object> parameters = Map.of(
                "from", from,
                "size", size
        );
        return get("?from={from}&size={size}", userId, parameters);
    }

    public ResponseEntity<Object> searchItems(Long userId, String text, Integer from, Integer size) {
        log.debug("searchItems: userId={}, text={}, from={}, size={}", userId, text, from, size);
        Map<String, Object> parameters = Map.of(
                "text", text,
                "from", from,
                "size", size
        );
        return get("/search?text={text}&from={from}&size={size}", userId, parameters);
    }

    public ResponseEntity<Object> addComment(Long userId, Long itemId, NewCommentRequest request) {
        log.debug("addComment: userId={}, itemId={}", userId, itemId);
        return post("/" + itemId + "/comment", userId, request);
    }
}