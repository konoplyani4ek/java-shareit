package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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

    @Transactional
    public UserDto createUser(UserDto userDto) {
        log.debug("Создание пользователя email={}", userDto.getEmail());

        checkEmailUniqueOrThrow(userDto.getEmail());

        User user = UserMapper.toEntity(userDto);
        User savedUser = userRepository.save(user);

        log.info("Создан пользователь id={}, email={}", savedUser.getId(), savedUser.getEmail());

        return UserMapper.toDto(savedUser);
    }

    @Transactional
    public UserDto updateUser(Long userId, UserDto userDto) {
        log.debug("Обновление пользователя userId={}", userId);

        User existingUser = getUserOrThrow(userId);

        // проверка уникальности email при изменении
        if (userDto.getEmail() != null &&
                !userDto.getEmail().equalsIgnoreCase(existingUser.getEmail())) {
            checkEmailUniqueOrThrow(userDto.getEmail());
        }

        // частичное обновление
        if (userDto.getName() != null) {
            existingUser.setName(userDto.getName());
        }
        if (userDto.getEmail() != null) {
            existingUser.setEmail(userDto.getEmail());
        }

        User updatedUser = userRepository.save(existingUser);

        log.info("Обновлён пользователь id={}, email={}", userId, updatedUser.getEmail());

        return UserMapper.toDto(updatedUser);
    }

    @Transactional(readOnly = true)
    public UserDto getUserById(Long userId) {
        log.debug("Получение пользователя userId={}", userId);

        User user = getUserOrThrow(userId);
        return UserMapper.toDto(user);
    }

    @Transactional(readOnly = true)
    public List<UserDto> getAllUsers() {
        log.debug("Получение всех пользователей");

        List<User> users = userRepository.findAll();
        log.info("Найдено {} пользователей", users.size());

        return users.stream()
                .map(UserMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteUser(Long userId) {
        log.debug("Удаление пользователя userId={}", userId);

        getUserOrThrow(userId); // проверка, что пользователь существует
        userRepository.deleteById(userId);

        log.info("Удалён пользователь id={}", userId);
    }

    private User getUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException(
                        "Пользователь с id=" + userId + " не найден"));
    }

    private void checkEmailUniqueOrThrow(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new ConflictException("Пользователь с email=" + email + " уже существует");
        }
    }
}