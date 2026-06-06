package ru.practicum.shareit.user;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    private User buildUser(Long id, String name, String email) {
        return User.builder().id(id).name(name).email(email).build();
    }

    @Test
    void create_success() {
        UserDto dto = UserDto.builder().name("John").email("john@mail.com").build();
        when(userRepository.findByEmail("john@mail.com")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class)))
                .thenReturn(buildUser(1L, "John", "john@mail.com"));

        UserDto result = userService.create(dto);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("John");
        assertThat(result.getEmail()).isEqualTo("john@mail.com");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void create_duplicateEmail_throwsConflict() {
        UserDto dto = UserDto.builder().name("John").email("john@mail.com").build();
        when(userRepository.findByEmail("john@mail.com"))
                .thenReturn(Optional.of(buildUser(99L, "Other", "john@mail.com")));

        assertThatThrownBy(() -> userService.create(dto))
                .isInstanceOf(ConflictException.class);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void update_name_only() {
        User existing = buildUser(1L, "Old", "old@mail.com");
        UserDto dto = UserDto.builder().name("New").build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        UserDto result = userService.update(1L, dto);

        assertThat(result.getName()).isEqualTo("New");
        assertThat(result.getEmail()).isEqualTo("old@mail.com");
        verify(userRepository, never()).findByEmail(any());
    }

    @Test
    void update_email_only() {
        User existing = buildUser(1L, "Old", "old@mail.com");
        UserDto dto = UserDto.builder().email("new@mail.com").build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(userRepository.findByEmail("new@mail.com")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        UserDto result = userService.update(1L, dto);

        assertThat(result.getName()).isEqualTo("Old");
        assertThat(result.getEmail()).isEqualTo("new@mail.com");
    }

    @Test
    void update_email_sameOwner_allowed() {
        User existing = buildUser(1L, "Old", "old@mail.com");
        UserDto dto = UserDto.builder().email("old@mail.com").build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(userRepository.findByEmail("old@mail.com")).thenReturn(Optional.of(existing));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        UserDto result = userService.update(1L, dto);

        assertThat(result.getEmail()).isEqualTo("old@mail.com");
    }

    @Test
    void update_email_takenByOther_throwsConflict() {
        User existing = buildUser(1L, "Old", "old@mail.com");
        UserDto dto = UserDto.builder().email("taken@mail.com").build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(userRepository.findByEmail("taken@mail.com"))
                .thenReturn(Optional.of(buildUser(2L, "Other", "taken@mail.com")));

        assertThatThrownBy(() -> userService.update(1L, dto))
                .isInstanceOf(ConflictException.class);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void update_userNotFound_throwsNotFound() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.update(1L, UserDto.builder().name("x").build()))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void getById_success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(buildUser(1L, "John", "j@mail.com")));

        UserDto result = userService.getById(1L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("John");
    }

    @Test
    void getById_notFound_throwsNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getById(1L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void getAll_returnsList() {
        when(userRepository.findAll()).thenReturn(List.of(
                buildUser(1L, "A", "a@mail.com"),
                buildUser(2L, "B", "b@mail.com")));

        List<UserDto> result = userService.getAll();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getEmail()).isEqualTo("a@mail.com");
    }

    @Test
    void delete_callsRepository() {
        userService.delete(5L);

        ArgumentCaptor<Long> captor = ArgumentCaptor.forClass(Long.class);
        verify(userRepository).deleteById(captor.capture());
        assertThat(captor.getValue()).isEqualTo(5L);
    }
}
