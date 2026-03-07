package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;

    /**
     * Создание пользователя
     * POST /users
     */
    public UserDto createUser(UserDto userDto) {
        log.debug("Создание пользователя email={}", userDto.getEmail());

        // Проверяем уникальность email
        if (userRepository.existsByEmail(userDto.getEmail())) {
            throw new ConflictException(
                    "Пользователь с email=" + userDto.getEmail() + " уже существует");
        }
        User user = UserMapper.toEntity(userDto);

        User savedUser = userRepository.create(user);

        log.info("Создан пользователь id={}, email={}", savedUser.getId(), savedUser.getEmail());

        return UserMapper.toDto(savedUser);
    }

    public UserDto updateUser(Long userId, UserDto userDto) {
        log.debug("Обновление пользователя userId={}", userId);

        User existingUser = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException(
                        "Пользователь с id=" + userId + " не найден"));

        //  уникальность email если он изменяется
        if (userDto.getEmail() != null &&
                !userDto.getEmail().equalsIgnoreCase(existingUser.getEmail())) {

            if (userRepository.existsByEmail(userDto.getEmail())) {
                throw new ConflictException(
                        "Пользователь с email=" + userDto.getEmail() + " уже существует");
            }
        }

        // Частичное обновление
        if (userDto.getName() != null) {
            existingUser.setName(userDto.getName());
        }
        if (userDto.getEmail() != null) {
            existingUser.setEmail(userDto.getEmail());
        }

        User updatedUser = userRepository.update(existingUser);

        log.info("Обновлён пользователь id={}, email={}", userId, updatedUser.getEmail());

        return UserMapper.toDto(updatedUser);
    }

    public UserDto getUserById(Long userId) {
        log.debug("Получение пользователя userId={}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException(
                        "Пользователь с id=" + userId + " не найден"));

        return UserMapper.toDto(user);
    }

    public List<UserDto> getAllUsers() {
        log.debug("Получение всех пользователей");

        List<User> users = userRepository.findAll();

        log.info("Найдено {} пользователей", users.size());

        return users.stream()
                .map(UserMapper::toDto)
                .collect(Collectors.toList());
    }

    public void deleteUser(Long userId) {
        log.debug("Удаление пользователя userId={}", userId);

        userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException(
                        "Пользователь с id=" + userId + " не найден"));

        userRepository.deleteById(userId);

        log.info("Удалён пользователь id={}", userId);
    }
}