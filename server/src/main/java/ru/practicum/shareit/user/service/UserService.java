package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.user.UserMapper;
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
        User saved = userRepository.save(user);
        log.info("Создан пользователь id={}, email={}", saved.getId(), saved.getEmail());
        return UserMapper.toDto(saved);
    }

    @Transactional
    public UserDto updateUser(Long userId, UserDto userDto) {
        log.debug("Обновление пользователя userId={}", userId);
        User existing = getUserOrThrow(userId);
        if (userDto.getEmail() != null &&
                !userDto.getEmail().equalsIgnoreCase(existing.getEmail())) {
            checkEmailUniqueOrThrow(userDto.getEmail());
        }
        if (userDto.getName() != null) existing.setName(userDto.getName());
        if (userDto.getEmail() != null) existing.setEmail(userDto.getEmail());
        User updated = userRepository.save(existing);
        log.info("Обновлён пользователь id={}", userId);
        return UserMapper.toDto(updated);
    }

    @Transactional(readOnly = true)
    public UserDto getUserById(Long userId) {
        log.debug("Получение пользователя userId={}", userId);
        return UserMapper.toDto(getUserOrThrow(userId));
    }

    @Transactional(readOnly = true)
    public List<UserDto> getAllUsers() {
        log.debug("Получение всех пользователей");
        return userRepository.findAll().stream()
                .map(UserMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteUser(Long userId) {
        log.debug("Удаление пользователя userId={}", userId);
        getUserOrThrow(userId);
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