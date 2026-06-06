package ru.practicum.shareit.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.lang.Nullable;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Slf4j
public class BaseClient {
    protected final RestTemplate rest;

    public BaseClient(RestTemplate rest) {
        this.rest = rest;
    }

    protected ResponseEntity<Object> get(String path, Long userId, @Nullable Map<String, Object> parameters) {
        return makeAndSendRequest(HttpMethod.GET, path, userId, parameters, null);
    }

    protected <T> ResponseEntity<Object> post(String path, Long userId, T body) {
        return makeAndSendRequest(HttpMethod.POST, path, userId, null, body);
    }

    protected <T> ResponseEntity<Object> put(String path, Long userId, T body) {
        return makeAndSendRequest(HttpMethod.PUT, path, userId, null, body);
    }

    protected <T> ResponseEntity<Object> patch(String path, Long userId, T body) {
        return makeAndSendRequest(HttpMethod.PATCH, path, userId, null, body);
    }

    protected ResponseEntity<Object> delete(String path, Long userId) {
        return makeAndSendRequest(HttpMethod.DELETE, path, userId, null, null);
    }

    private <T> ResponseEntity<Object> makeAndSendRequest(HttpMethod method, String path, Long userId,
                                                          @Nullable Map<String, Object> parameters, @Nullable T body) {
        HttpEntity<T> requestEntity = new HttpEntity<>(body, createHeaders(userId));

        log.debug("Sending {} request to {} with userId={}", method, path, userId);
        if (body != null) {
            log.debug("Request body: {}", body);
        }

        ResponseEntity<Object> response;
        try {
            if (parameters != null && !parameters.isEmpty()) {
                response = rest.exchange(path, method, requestEntity, Object.class, parameters);
            } else {
                response = rest.exchange(path, method, requestEntity, Object.class);
            }
            log.debug("Received response with status: {}", response.getStatusCode());
        } catch (HttpStatusCodeException e) {
            log.warn("Request failed with status: {}, body: {}", e.getStatusCode(), e.getResponseBodyAsString());
            return ResponseEntity.status(e.getStatusCode())
                    .body(e.getResponseBodyAsString());
        }
        return prepareGatewayResponse(response);
    }

    private HttpHeaders createHeaders(Long userId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        if (userId != null) {
            headers.set("X-Sharer-User-Id", String.valueOf(userId));
        }
        return headers;
    }

    private static ResponseEntity<Object> prepareGatewayResponse(ResponseEntity<Object> response) {
        if (response.getStatusCode().is2xxSuccessful()) {
            return response;
        }

        ResponseEntity.BodyBuilder responseBuilder = ResponseEntity.status(response.getStatusCode());

        if (response.hasBody()) {
            return responseBuilder.body(response.getBody());
        }

        return responseBuilder.build();
    }
}