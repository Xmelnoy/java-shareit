package ru.practicum.shareit.request;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.NewItemRequestReq;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Тесты сервиса запросов вещей")
class ItemRequestServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ItemRequestRepository requestRepository;

    @Mock
    private ItemRequestMapper requestMapper;

    @InjectMocks
    private ItemRequestServiceImpl requestService;

    private User requester;
    private NewItemRequestReq newRequest;
    private ItemRequest itemRequest;
    private ItemRequestDto requestDto;

    @BeforeEach
    void setUp() {
        requester = new User();
        requester.setUserId(1L);
        requester.setName("Заказчик");
        requester.setEmail("requester@test.com");

        newRequest = new NewItemRequestReq();
        newRequest.setDescription("Нужна дрель");

        itemRequest = new ItemRequest();
        itemRequest.setId(1L);
        itemRequest.setDescription("Нужна дрель");
        itemRequest.setCreated(LocalDateTime.now());
        itemRequest.setRequestor(requester);

        requestDto = new ItemRequestDto();
        requestDto.setId(1L);
        requestDto.setDescription("Нужна дрель");
        requestDto.setCreated(LocalDateTime.now());
    }


    @Test
    @DisplayName("addRequest - должен создавать запрос")
    void addRequest_shouldSaveAndReturnRequest() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(requester));
        when(requestMapper.toItemEntity(any(NewItemRequestReq.class))).thenReturn(itemRequest);
        when(requestRepository.save(any(ItemRequest.class))).thenReturn(itemRequest);
        when(requestMapper.toRequestDto(any(ItemRequest.class))).thenReturn(requestDto);

        ItemRequestDto result = requestService.addRequest(1L, newRequest);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getDescription()).isEqualTo("Нужна дрель");
        verify(requestRepository).save(any(ItemRequest.class));
    }

    @Test
    @DisplayName("addRequest - должен выбрасывать NotFoundException, если пользователь не найден")
    void addRequest_shouldThrowNotFoundException_whenUserNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> requestService.addRequest(99L, newRequest))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("не найден");
    }

    @Test
    @DisplayName("getUserRequests - должен возвращать список запросов пользователя")
    void getUserRequests_shouldReturnList() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(requester));
        when(requestRepository.findAllByRequestorId(1L)).thenReturn(List.of(itemRequest));
        when(requestMapper.toRequestDto(any(ItemRequest.class))).thenReturn(requestDto);

        List<ItemRequestDto> result = requestService.getUserRequests(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("getUserRequests - должен выбрасывать NotFoundException, если пользователь не найден")
    void getUserRequests_shouldThrowNotFoundException_whenUserNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> requestService.getUserRequests(99L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("не найден");
    }

    @Test
    @DisplayName("getAllRequests - должен возвращать все запросы")
    void getAllRequests_shouldReturnAll() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(requester));
        when(requestRepository.findAll(any(Sort.class)))
                .thenReturn(List.of(itemRequest));
        when(requestMapper.toRequestDto(any(ItemRequest.class))).thenReturn(requestDto);

        List<ItemRequestDto> result = requestService.getAllRequests(1L);

        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("getAllRequests - должен выбрасывать NotFoundException, если пользователь не найден")
    void getAllRequests_shouldThrowNotFoundException_whenUserNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> requestService.getAllRequests(99L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("не найден");
    }


    @Test
    @DisplayName("getRequest - должен возвращать запрос по ID")
    void getRequest_shouldReturnRequest() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(requester));
        when(requestRepository.findById(10L)).thenReturn(Optional.of(itemRequest));
        when(requestMapper.toRequestDto(any(ItemRequest.class))).thenReturn(requestDto);

        ItemRequestDto result = requestService.getRequest(1L, 10L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getDescription()).isEqualTo("Нужна дрель");
    }

    @Test
    @DisplayName("getRequest - должен выбрасывать NotFoundException, если пользователь не найден")
    void getRequest_shouldThrowNotFoundException_whenUserNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> requestService.getRequest(99L, 10L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("не найден");
    }

    @Test
    @DisplayName("getRequest - должен выбрасывать NotFoundException, если запрос не найден")
    void getRequest_shouldThrowNotFoundException_whenRequestNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(requester));
        when(requestRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> requestService.getRequest(1L, 99L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Не существует");
    }
}