package ru.practicum.shareit.user;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class UserServiceIntegrationTest {

    @Autowired
    private UserService userService;

    @Test
    void createAndGetUser() {
        UserDto dto = new UserDto(null, "Test", "test@test.com");

        UserDto created = userService.createUser(dto);

        assertNotNull(created.getId());
        assertEquals("Test", created.getName());
        assertEquals("test@test.com", created.getEmail());

        UserDto found = userService.getUserById(created.getId());
        assertEquals(created.getId(), found.getId());
        assertEquals("Test", found.getName());
    }

    @Test
    void createUser_whenDuplicateEmail_throwsConflict() {
        userService.createUser(new UserDto(null, "User1", "dup@test.com"));

        assertThrows(ConflictException.class,
                () -> userService.createUser(new UserDto(null, "User2", "dup@test.com")));
    }

    @Test
    void updateUser_partialUpdate() {
        UserDto created = userService.createUser(new UserDto(null, "Old", "old@test.com"));

        UserDto updated = userService.updateUser(created.getId(),
                new UserDto(null, "New", null));

        assertEquals("New", updated.getName());
        assertEquals("old@test.com", updated.getEmail());
    }

    @Test
    void deleteUser_thenNotFound() {
        UserDto created = userService.createUser(new UserDto(null, "ToDelete", "delete@test.com"));
        Long id = created.getId();

        userService.deleteUser(id);

        assertThrows(NoSuchElementException.class, () -> userService.getUserById(id));
    }

    @Test
    void getAllUsers_returnsAllCreated() {
        userService.createUser(new UserDto(null, "User1", "u1@test.com"));
        userService.createUser(new UserDto(null, "User2", "u2@test.com"));

        List<UserDto> users = userService.getAllUsers();

        assertTrue(users.size() >= 2);
    }
}