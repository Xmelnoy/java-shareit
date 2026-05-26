package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;
import ru.practicum.shareit.request.mapper.ItemRequestMapper;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ItemRequestServiceImpl implements ItemRequestService {
    private final ItemRequestRepository requestRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    @Override
    public ItemRequestResponseDto create(Long userId, ItemRequestCreateDto createDto) {
        User requestor = getUserOrThrow(userId);
        ItemRequest request = ItemRequestMapper.toEntity(createDto.getDescription(), requestor);
        ItemRequest saved = requestRepository.save(request);
        log.info("Создан запрос с id={} для пользователя с id={}", saved.getId(), userId);
        return ItemRequestMapper.toDto(saved, List.of());
    }

    @Override
    public List<ItemRequestResponseDto> getUserRequests(Long userId) {
        getUserOrThrow(userId);
        List<ItemRequest> requests = requestRepository.findByRequestorIdOrderByCreatedDesc(userId);
        return requests.stream()
                .map(req -> ItemRequestMapper.toDto(req, getItemsForRequest(req.getId())))
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemRequestResponseDto> getAllRequests(Long userId, Integer from, Integer size) {
        getUserOrThrow(userId);
        int page = from / size;
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "created"));
        return requestRepository.findByRequestorIdNot(userId, pageable).stream()
                .map(req -> ItemRequestMapper.toDto(req, getItemsForRequest(req.getId())))
                .collect(Collectors.toList());
    }

    @Override
    public ItemRequestResponseDto getRequestById(Long userId, Long requestId) {
        getUserOrThrow(userId);
        ItemRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Запрос с id=" + requestId + " не найден."));
        List<ItemDto> items = getItemsForRequest(requestId);
        return ItemRequestMapper.toDto(request, items);
    }

    private User getUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + userId + " не найден."));
    }

    private List<ItemDto> getItemsForRequest(Long requestId) {
        return itemRepository.findByRequestId(requestId).stream()
                .map(ItemMapper::toDto)
                .collect(Collectors.toList());
    }
}