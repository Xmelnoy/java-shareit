package ru.practicum.shareit.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.NewUserRequest;
import ru.practicum.shareit.user.dto.UpdateUserRequest;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Тесты сервиса пользователей")
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserServiceImpl userService;

    private User user;
    private NewUserRequest newUserRequest;
    private UpdateUserRequest updateUserRequest;
    private UserDto userDto;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setUserId(1L);
        user.setName("Иван Иванов");
        user.setEmail("ivan@example.com");

        newUserRequest = NewUserRequest.builder()
                .name("Иван Иванов")
                .email("ivan@example.com")
                .build();

        updateUserRequest = UpdateUserRequest.builder()
                .name("Иван Петров")
                .email("ivan.petrov@example.com")
                .build();

        userDto = UserDto.builder()
                .id(1L)
                .name("Иван Иванов")
                .email("ivan@example.com")
                .build();
    }

    @Test
    @DisplayName("createUser - должен создавать пользователя")
    void createUser_shouldSaveAndReturnUser() {
        when(userMapper.toUserEntity(any(NewUserRequest.class))).thenReturn(user);
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userMapper.toUserDto(any(User.class))).thenReturn(userDto);

        UserDto result = userService.createUser(newUserRequest);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Иван Иванов");
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("updateUser - должен обновлять пользователя")
    void updateUser_shouldUpdateAndReturnUser() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userMapper.toUserDto(any(User.class))).thenReturn(userDto);

        UserDto result = userService.updateUser(1L, updateUserRequest);

        assertThat(result.getId()).isEqualTo(1L);
        verify(userMapper).updateUserFromRequest(any(User.class), any(UpdateUserRequest.class));
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("updateUser - должен выбрасывать NotFoundException, если пользователь не найден")
    void updateUser_shouldThrowNotFoundException_whenUserNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.updateUser(99L, updateUserRequest))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("не найден");
    }

    @Test
    @DisplayName("getUser - должен возвращать пользователя")
    void getUser_shouldReturnUser() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userMapper.toUserDto(any(User.class))).thenReturn(userDto);

        UserDto result = userService.getUser(1L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Иван Иванов");
    }

    @Test
    @DisplayName("getUser - должен выбрасывать NotFoundException, если пользователь не найден")
    void getUser_shouldThrowNotFoundException_whenUserNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUser(99L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("не найден");
    }

    @Test
    @DisplayName("deleteUser - должен удалять пользователя")
    void deleteUser_shouldDeleteUser() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        userService.deleteUser(1L);

        verify(userRepository).deleteById(1L);
    }

    @Test
    @DisplayName("deleteUser - должен выбрасывать NotFoundException, если пользователь не найден")
    void deleteUser_shouldThrowNotFoundException_whenUserNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.deleteUser(99L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("не найден");
    }

    @Test
    @DisplayName("getUsers - должен возвращать страницу пользователей с пагинацией и сортировкой")
    void getUsers_shouldReturnPageOfUsers() {
        User user1 = new User();
        user1.setUserId(1L);
        user1.setName("Иван");
        user1.setEmail("ivan@test.com");

        User user2 = new User();
        user2.setUserId(2L);
        user2.setName("Пётр");
        user2.setEmail("petr@test.com");

        UserDto userDto1 = UserDto.builder()
                .id(1L)
                .name("Иван")
                .email("ivan@test.com")
                .build();

        UserDto userDto2 = UserDto.builder()
                .id(2L)
                .name("Пётр")
                .email("petr@test.com")
                .build();

        Page<User> userPage = new PageImpl<>(List.of(user1, user2));

        when(userRepository.findAll(any(Pageable.class))).thenReturn(userPage);

        when(userMapper.toUserDto(user1)).thenReturn(userDto1);
        when(userMapper.toUserDto(user2)).thenReturn(userDto2);

        Page<UserDto> result = userService.getUsers(1L, 0, 10);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent().get(0).getId()).isEqualTo(1L);
        assertThat(result.getContent().get(0).getName()).isEqualTo("Иван");
        assertThat(result.getContent().get(1).getId()).isEqualTo(2L);
        assertThat(result.getContent().get(1).getName()).isEqualTo("Пётр");
        assertThat(result.getTotalElements()).isEqualTo(2);

        verify(userRepository).findAll(argThat((Pageable pageable) ->
                pageable.getPageNumber() == 0 &&
                        pageable.getPageSize() == 10 &&
                        pageable.getSort().isSorted() &&
                        pageable.getSort().getOrderFor("id") != null &&
                        pageable.getSort().getOrderFor("id").getDirection() == Sort.Direction.DESC
        ));

        verify(userMapper).toUserDto(user1);
        verify(userMapper).toUserDto(user2);
    }

    @Test
    @DisplayName("getUsers - должен правильно рассчитывать номер страницы из from/size")
    void getUsers_shouldCalculatePageNumberCorrectly() {
        User user = new User();
        user.setUserId(1L);
        user.setName("Иван");
        user.setEmail("ivan@test.com");

        UserDto userDto = UserDto.builder()
                .id(1L)
                .name("Иван")
                .email("ivan@test.com")
                .build();

        Page<User> userPage = new PageImpl<>(List.of(user));
        when(userRepository.findAll(any(Pageable.class))).thenReturn(userPage);
        when(userMapper.toUserDto(user)).thenReturn(userDto);

        Page<UserDto> result = userService.getUsers(1L, 20, 10);

        assertThat(result).isNotNull();

        verify(userRepository).findAll(argThat((Pageable pageable) ->
                pageable.getPageNumber() == 2 &&
                        pageable.getPageSize() == 10
        ));
    }

    @Test
    @DisplayName("getUsers - должен возвращать пустую страницу, если пользователей нет")
    void getUsers_shouldReturnEmptyPage_whenNoUsers() {
        Page<User> emptyPage = Page.empty();
        when(userRepository.findAll(any(Pageable.class))).thenReturn(emptyPage);

        Page<UserDto> result = userService.getUsers(1L, 0, 10);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isZero();
    }
}