package ru.practicum.shareit.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.user.UserController;
import ru.practicum.shareit.user.UserService;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.repository.InMemoryUserRepository;

import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

class UserControllerTest {

    private UserController userController;
    private UserService userService;
    private InMemoryUserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository = new InMemoryUserRepository();
        userService = new UserService(userRepository);
        userController = new UserController(userService);
    }

    private UserDto makeUserDto(String name, String email) {
        return UserDto.builder().name(name).email(email).build();
    }

    @Nested
    @DisplayName("createUser")
    class CreateUser {

        @Test
        @DisplayName("успешное создание пользователя")
        void shouldCreateUser() {
            UserDto result = userController.createUser(makeUserDto("Иван", "ivan@mail.ru"));

            assertNotNull(result.getId());
            assertEquals("Иван", result.getName());
            assertEquals("ivan@mail.ru", result.getEmail());
        }

        @Test
        @DisplayName("id присваивается последовательно")
        void shouldAssignSequentialIds() {
            UserDto first = userController.createUser(makeUserDto("Иван", "ivan@mail.ru"));
            UserDto second = userController.createUser(makeUserDto("Пётр", "petr@mail.ru"));

            assertNotEquals(first.getId(), second.getId());
        }

        @Test
        @DisplayName("ошибка при дублирующемся email")
        void shouldThrowOnDuplicateEmail() {
            userController.createUser(makeUserDto("Иван", "ivan@mail.ru"));

            assertThrows(ConflictException.class,
                    () -> userController.createUser(makeUserDto("Другой", "ivan@mail.ru")));
        }
    }

    @Nested
    @DisplayName("updateUser")
    class UpdateUser {

        private Long userId;

        @BeforeEach
        void createTestUser() {
            userId = userController.createUser(makeUserDto("Иван", "ivan@mail.ru")).getId();
        }

        @Test
        @DisplayName("обновление всех полей")
        void shouldUpdateAllFields() {
            UserDto result = userController.updateUser(userId, makeUserDto("Пётр", "petr@mail.ru"));

            assertEquals(userId, result.getId());
            assertEquals("Пётр", result.getName());
            assertEquals("petr@mail.ru", result.getEmail());
        }

        @Test
        @DisplayName("частичное обновление — только name")
        void shouldUpdateOnlyName() {
            UserDto result = userController.updateUser(userId,
                    UserDto.builder().name("Пётр").build());

            assertEquals("Пётр", result.getName());
            assertEquals("ivan@mail.ru", result.getEmail());
        }

        @Test
        @DisplayName("частичное обновление — только email")
        void shouldUpdateOnlyEmail() {
            UserDto result = userController.updateUser(userId,
                    UserDto.builder().email("new@mail.ru").build());

            assertEquals("Иван", result.getName());
            assertEquals("new@mail.ru", result.getEmail());
        }

        @Test
        @DisplayName("ошибка при несуществующем пользователе")
        void shouldThrowWhenNotFound() {
            assertThrows(NoSuchElementException.class,
                    () -> userController.updateUser(999L, makeUserDto("Пётр", "petr@mail.ru")));
        }

        @Test
        @DisplayName("ошибка при дублирующемся email")
        void shouldThrowWhenEmailTaken() {
            userController.createUser(makeUserDto("Пётр", "petr@mail.ru"));

            assertThrows(ConflictException.class,
                    () -> userController.updateUser(userId,
                            UserDto.builder().email("petr@mail.ru").build()));
        }
    }

    @Nested
    @DisplayName("getUser")
    class GetUser {

        @Test
        @DisplayName("успешное получение")
        void shouldReturnUser() {
            Long id = userController.createUser(makeUserDto("Иван", "ivan@mail.ru")).getId();

            UserDto result = userController.getUser(id);

            assertEquals(id, result.getId());
            assertEquals("Иван", result.getName());
            assertEquals("ivan@mail.ru", result.getEmail());
        }

        @Test
        @DisplayName("ошибка при несуществующем id")
        void shouldThrowWhenNotFound() {
            assertThrows(NoSuchElementException.class,
                    () -> userController.getUser(999L));
        }
    }

    @Nested
    @DisplayName("getAllUsers")
    class GetAllUsers {

        @Test
        @DisplayName("возвращает список пользователей")
        void shouldReturnList() {
            userController.createUser(makeUserDto("Иван", "ivan@mail.ru"));
            userController.createUser(makeUserDto("Пётр", "petr@mail.ru"));

            List<UserDto> result = userController.getAllUsers();

            assertEquals(2, result.size());
        }

        @Test
        @DisplayName("пустой список")
        void shouldReturnEmptyList() {
            List<UserDto> result = userController.getAllUsers();

            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("deleteUser")
    class DeleteUser {

        @Test
        @DisplayName("успешное удаление")
        void shouldDeleteUser() {
            Long id = userController.createUser(makeUserDto("Иван", "ivan@mail.ru")).getId();

            userController.deleteUser(id);

            assertThrows(NoSuchElementException.class,
                    () -> userController.getUser(id));
        }

        @Test
        @DisplayName("ошибка при несуществующем пользователе")
        void shouldThrowWhenNotFound() {
            assertThrows(NoSuchElementException.class,
                    () -> userController.deleteUser(999L));
        }
    }
}