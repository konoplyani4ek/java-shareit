package ru.practicum.shareit.user;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Test
    void createUser_whenValid_returnsDto() {
        UserDto dto = new UserDto(null, "Test", "test@test.com");
        User saved = new User(1L, "Test", "test@test.com");

        when(userRepository.existsByEmail(dto.getEmail())).thenReturn(false);
        when(userRepository.save(any())).thenReturn(saved);

        UserDto result = userService.createUser(dto);

        assertEquals(1L, result.getId());
        assertEquals("Test", result.getName());
        assertEquals("test@test.com", result.getEmail());
        verify(userRepository).save(any());
    }

    @Test
    void createUser_whenEmailDuplicate_throwsConflict() {
        UserDto dto = new UserDto(null, "Test", "test@test.com");
        when(userRepository.existsByEmail(dto.getEmail())).thenReturn(true);

        assertThrows(ConflictException.class, () -> userService.createUser(dto));
        verify(userRepository, never()).save(any());
    }

    @Test
    void getUserById_whenExists_returnsDto() {
        User user = new User(1L, "Test", "test@test.com");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        UserDto result = userService.getUserById(1L);

        assertEquals(1L, result.getId());
        assertEquals("Test", result.getName());
    }

    @Test
    void getUserById_whenNotFound_throwsException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> userService.getUserById(99L));
    }

    @Test
    void updateUser_whenEmailChanged_checksUniqueness() {
        User existing = new User(1L, "Old", "old@test.com");
        UserDto dto = new UserDto(null, "New", "new@test.com");

        when(userRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(userRepository.existsByEmail("new@test.com")).thenReturn(false);
        when(userRepository.save(any())).thenReturn(new User(1L, "New", "new@test.com"));

        UserDto result = userService.updateUser(1L, dto);

        assertEquals("New", result.getName());
        assertEquals("new@test.com", result.getEmail());
    }

    @Test
    void updateUser_whenEmailTaken_throwsConflict() {
        User existing = new User(1L, "Old", "old@test.com");
        UserDto dto = new UserDto(null, null, "taken@test.com");

        when(userRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(userRepository.existsByEmail("taken@test.com")).thenReturn(true);

        assertThrows(ConflictException.class, () -> userService.updateUser(1L, dto));
    }

    @Test
    void deleteUser_whenExists_deletesSuccessfully() {
        User user = new User(1L, "Test", "test@test.com");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        userService.deleteUser(1L);

        verify(userRepository).deleteById(1L);
    }

    @Test
    void deleteUser_whenNotFound_throwsException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> userService.deleteUser(99L));
        verify(userRepository, never()).deleteById(any());
    }

    @Test
    void getAllUsers_returnsListOfDto() {
        List<User> users = List.of(
                new User(1L, "User1", "user1@test.com"),
                new User(2L, "User2", "user2@test.com")
        );
        when(userRepository.findAll()).thenReturn(users);

        List<UserDto> result = userService.getAllUsers();

        assertEquals(2, result.size());
        assertEquals("User1", result.get(0).getName());
        assertEquals("User2", result.get(1).getName());
    }
}