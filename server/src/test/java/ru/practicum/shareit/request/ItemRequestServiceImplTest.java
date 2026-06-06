package ru.practicum.shareit.request;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ItemRequestServiceImplTest {

    @Mock
    private ItemRequestRepository requestRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ItemRepository itemRepository;

    @InjectMocks
    private ItemRequestServiceImpl requestService;

    private User user(Long id) {
        return User.builder().id(id).name("User" + id).email("u" + id + "@mail.com").build();
    }

    private ItemRequest request(Long id, User requestor, LocalDateTime created) {
        return ItemRequest.builder()
                .id(id)
                .description("desc" + id)
                .requestor(requestor)
                .created(created)
                .build();
    }

    @Test
    void create_setsCreatedAndReturnsDto() {
        User requestor = user(1L);
        ItemRequestCreateDto createDto = new ItemRequestCreateDto();
        createDto.setDescription("need a drill");
        when(userRepository.findById(1L)).thenReturn(Optional.of(requestor));
        when(requestRepository.save(any(ItemRequest.class))).thenAnswer(inv -> {
            ItemRequest r = inv.getArgument(0);
            r.setId(10L);
            return r;
        });

        ItemRequestResponseDto result = requestService.create(1L, createDto);

        assertThat(result.getId()).isEqualTo(10L);
        assertThat(result.getDescription()).isEqualTo("need a drill");
        assertThat(result.getCreated()).isNotNull();
        assertThat(result.getItems()).isEmpty();

        ArgumentCaptor<ItemRequest> captor = ArgumentCaptor.forClass(ItemRequest.class);
        verify(requestRepository).save(captor.capture());
        assertThat(captor.getValue().getCreated()).isNotNull();
        assertThat(captor.getValue().getRequestor()).isEqualTo(requestor);
    }

    @Test
    void create_userNotFound_throwsNotFound() {
        ItemRequestCreateDto createDto = new ItemRequestCreateDto();
        createDto.setDescription("x");
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> requestService.create(1L, createDto))
                .isInstanceOf(NotFoundException.class);
        verify(requestRepository, never()).save(any());
    }

    @Test
    void getUserRequests_returnsListWithItems() {
        User requestor = user(1L);
        ItemRequest req = request(5L, requestor, LocalDateTime.now());
        when(userRepository.findById(1L)).thenReturn(Optional.of(requestor));
        when(requestRepository.findByRequestorIdOrderByCreatedDesc(1L)).thenReturn(List.of(req));
        Item item = Item.builder().id(100L).name("Drill").description("d")
                .available(true).owner(requestor).request(req).build();
        when(itemRepository.findByRequestId(5L)).thenReturn(List.of(item));

        List<ItemRequestResponseDto> result = requestService.getUserRequests(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(5L);
        assertThat(result.get(0).getItems()).hasSize(1);
        assertThat(result.get(0).getItems().get(0).getRequestId()).isEqualTo(5L);
    }

    @Test
    void getUserRequests_userNotFound_throwsNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> requestService.getUserRequests(1L))
                .isInstanceOf(NotFoundException.class);
        verify(requestRepository, never()).findByRequestorIdOrderByCreatedDesc(anyLong());
    }

    @Test
    void getAllRequests_paginatesAndExcludesSelf() {
        User requestor = user(1L);
        ItemRequest req = request(7L, user(2L), LocalDateTime.now());
        when(userRepository.findById(1L)).thenReturn(Optional.of(requestor));
        Page<ItemRequest> page = new PageImpl<>(List.of(req));
        when(requestRepository.findByRequestorIdNot(eq(1L), any(Pageable.class))).thenReturn(page);
        when(itemRepository.findByRequestId(7L)).thenReturn(List.of());

        List<ItemRequestResponseDto> result = requestService.getAllRequests(1L, 0, 10);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(7L);

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(requestRepository).findByRequestorIdNot(eq(1L), captor.capture());
        assertThat(captor.getValue().getPageNumber()).isEqualTo(0);
        assertThat(captor.getValue().getPageSize()).isEqualTo(10);
    }

    @Test
    void getAllRequests_computesPageFromOffset() {
        User requestor = user(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(requestor));
        when(requestRepository.findByRequestorIdNot(eq(1L), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));

        requestService.getAllRequests(1L, 20, 10);

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(requestRepository).findByRequestorIdNot(eq(1L), captor.capture());
        assertThat(captor.getValue().getPageNumber()).isEqualTo(2);
    }

    @Test
    void getRequestById_success() {
        User requestor = user(1L);
        ItemRequest req = request(3L, requestor, LocalDateTime.now());
        when(userRepository.findById(1L)).thenReturn(Optional.of(requestor));
        when(requestRepository.findById(3L)).thenReturn(Optional.of(req));
        Item item = Item.builder().id(50L).name("Saw").description("s")
                .available(true).owner(requestor).request(req).build();
        when(itemRepository.findByRequestId(3L)).thenReturn(List.of(item));

        ItemRequestResponseDto result = requestService.getRequestById(1L, 3L);

        assertThat(result.getId()).isEqualTo(3L);
        assertThat(result.getItems()).hasSize(1);
        assertThat(result.getItems().get(0).getName()).isEqualTo("Saw");
    }

    @Test
    void getRequestById_requestNotFound_throwsNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user(1L)));
        when(requestRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> requestService.getRequestById(1L, 99L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void getRequestById_userNotFound_throwsNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> requestService.getRequestById(1L, 3L))
                .isInstanceOf(NotFoundException.class);
        verify(requestRepository, never()).findById(anyLong());
    }
}
