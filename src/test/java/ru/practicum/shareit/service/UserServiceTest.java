package ru.practicum.shareit.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.user.UserService;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.repository.InMemoryUserRepository;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

class UserServiceTest {

    private UserService userService;
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository = new InMemoryUserRepository();
        userService = new UserService(userRepository);
    }

    private UserDto makeUserDto(String name, String email) {
        return UserDto.builder()
                .name(name)
                .email(email)
                .build();
    }

    @Nested
    @DisplayName("createUser")
    class CreateUser {

        @Test
        @DisplayName("успешное создание пользователя")
        void shouldCreateUser() {
            UserDto dto = makeUserDto("Иван", "ivan@mail.ru");

            UserDto result = userService.createUser(dto);

            assertNotNull(result.getId());
            assertEquals("Иван", result.getName());
            assertEquals("ivan@mail.ru", result.getEmail());
        }

        @Test
        @DisplayName("id присваивается автоматически")
        void shouldAssignId() {
            UserDto first = userService.createUser(makeUserDto("Первый", "first@mail.ru"));
            UserDto second = userService.createUser(makeUserDto("Второй", "second@mail.ru"));

            assertNotNull(first.getId());
            assertNotNull(second.getId());
            assertNotEquals(first.getId(), second.getId());
        }

        @Test
        @DisplayName("ошибка при дублирующемся email")
        void shouldThrowOnDuplicateEmail() {
            userService.createUser(makeUserDto("Иван", "ivan@mail.ru"));

            assertThrows(ConflictException.class,
                    () -> userService.createUser(makeUserDto("Другой Иван", "ivan@mail.ru")));
        }

        @Test
        @DisplayName("ошибка при дублирующемся email без учёта регистра")
        void shouldThrowOnDuplicateEmailCaseInsensitive() {
            userService.createUser(makeUserDto("Иван", "ivan@mail.ru"));

            assertThrows(ConflictException.class,
                    () -> userService.createUser(makeUserDto("Другой", "IVAN@MAIL.RU")));
        }
    }

    @Nested
    @DisplayName("updateUser")
    class UpdateUser {

        private UserDto createdUser;

        @BeforeEach
        void createTestUser() {
            createdUser = userService.createUser(makeUserDto("Иван", "ivan@mail.ru"));
        }

        @Test
        @DisplayName("обновление всех полей")
        void shouldUpdateAllFields() {
            UserDto updateDto = makeUserDto("Пётр", "petr@mail.ru");

            UserDto result = userService.updateUser(createdUser.getId(), updateDto);

            assertEquals(createdUser.getId(), result.getId());
            assertEquals("Пётр", result.getName());
            assertEquals("petr@mail.ru", result.getEmail());
        }

        @Test
        @DisplayName("частичное обновление — только name")
        void shouldUpdateOnlyName() {
            UserDto updateDto = UserDto.builder().name("Пётр").build();

            UserDto result = userService.updateUser(createdUser.getId(), updateDto);

            assertEquals("Пётр", result.getName());
            assertEquals("ivan@mail.ru", result.getEmail());
        }

        @Test
        @DisplayName("частичное обновление — только email")
        void shouldUpdateOnlyEmail() {
            UserDto updateDto = UserDto.builder().email("newemail@mail.ru").build();

            UserDto result = userService.updateUser(createdUser.getId(), updateDto);

            assertEquals("Иван", result.getName());
            assertEquals("newemail@mail.ru", result.getEmail());
        }

        @Test
        @DisplayName("обновление email на тот же самый — без ошибки")
        void shouldAllowSameEmail() {
            UserDto updateDto = UserDto.builder().email("ivan@mail.ru").build();

            UserDto result = userService.updateUser(createdUser.getId(), updateDto);

            assertEquals("ivan@mail.ru", result.getEmail());
        }

        @Test
        @DisplayName("ошибка при обновлении email на уже занятый")
        void shouldThrowOnDuplicateEmail() {
            userService.createUser(makeUserDto("Пётр", "petr@mail.ru"));

            UserDto updateDto = UserDto.builder().email("petr@mail.ru").build();

            assertThrows(ConflictException.class,
                    () -> userService.updateUser(createdUser.getId(), updateDto));
        }

        @Test
        @DisplayName("ошибка при несуществующем пользователе")
        void shouldThrowWhenUserNotFound() {
            UserDto updateDto = makeUserDto("Хак", "hack@mail.ru");

            assertThrows(NoSuchElementException.class,
                    () -> userService.updateUser(999L, updateDto));
        }
    }

    @Nested
    @DisplayName("getUserById")
    class GetUserById {

        @Test
        @DisplayName("успешное получение пользователя")
        void shouldReturnUser() {
            UserDto created = userService.createUser(makeUserDto("Иван", "ivan@mail.ru"));

            UserDto result = userService.getUserById(created.getId());

            assertEquals(created.getId(), result.getId());
            assertEquals("Иван", result.getName());
            assertEquals("ivan@mail.ru", result.getEmail());
        }

        @Test
        @DisplayName("ошибка при несуществующем id")
        void shouldThrowWhenNotFound() {
            assertThrows(NoSuchElementException.class,
                    () -> userService.getUserById(999L));
        }
    }

    @Nested
    @DisplayName("getAllUsers")
    class GetAllUsers {

        @Test
        @DisplayName("возвращает всех пользователей")
        void shouldReturnAllUsers() {
            userService.createUser(makeUserDto("Иван", "ivan@mail.ru"));
            userService.createUser(makeUserDto("Пётр", "petr@mail.ru"));

            List<UserDto> result = userService.getAllUsers();

            assertEquals(2, result.size());
        }

        @Test
        @DisplayName("пустой список если нет пользователей")
        void shouldReturnEmptyList() {
            List<UserDto> result = userService.getAllUsers();

            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("deleteUser")
    class DeleteUser {

        @Test
        @DisplayName("успешное удаление пользователя")
        void shouldDeleteUser() {
            UserDto created = userService.createUser(makeUserDto("Иван", "ivan@mail.ru"));

            userService.deleteUser(created.getId());

            assertThrows(NoSuchElementException.class,
                    () -> userService.getUserById(created.getId()));
        }

        @Test
        @DisplayName("после удаления email снова доступен")
        void shouldFreeEmailAfterDelete() {
            UserDto created = userService.createUser(makeUserDto("Иван", "ivan@mail.ru"));
            userService.deleteUser(created.getId());

            UserDto newUser = userService.createUser(makeUserDto("Новый Иван", "ivan@mail.ru"));

            assertNotNull(newUser.getId());
            assertEquals("ivan@mail.ru", newUser.getEmail());
        }

        @Test
        @DisplayName("ошибка при удалении несуществующего пользователя")
        void shouldThrowWhenNotFound() {
            assertThrows(NoSuchElementException.class,
                    () -> userService.deleteUser(999L));
        }
    }
}