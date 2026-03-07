package ru.practicum.shareit.user.repository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.user.model.User;

import java.util.*;

@Slf4j
@Repository
public class InMemoryUserRepository implements UserRepository {

    private final Map<Long, User> userMap = new HashMap<>();
    private Long counter = 1L; // id

    @Override
    public User create(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User не может быть null");
        }

        user.setId(generateId());
        userMap.put(user.getId(), user);
        log.debug("Создан user с id={}, email={}", user.getId(), user.getEmail());
        return user;
    }

    @Override
    public User update(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User не может быть null");
        }
        if (user.getId() == null) {
            throw new IllegalArgumentException("ID пользователя не может быть null при обновлении");
        }

        User existingUser = userMap.get(user.getId());
        if (existingUser == null) {
            throw new NoSuchElementException("User с id=" + user.getId() + " не найден");
        }

        userMap.put(user.getId(), user);
        log.debug("Обновлён user с id={}, email={}", user.getId(), user.getEmail());
        return user;
    }

    @Override
    public void deleteById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("ID не может быть null");
        }

        User removed = userMap.remove(id);
        if (removed == null) {
            throw new NoSuchElementException("User с id=" + id + " не найден");
        }

        log.info("Удалён user с id={}, email={}", id, removed.getEmail());
    }

    @Override
    public Optional<User> findById(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(userMap.get(id));
    }

    @Override
    public List<User> findAll() {
        return new ArrayList<>(userMap.values());
    }

    @Override
    public boolean existsByEmail(String email) {
        if (email == null || email.isBlank()) {
            return false;
        }

        return userMap.values().stream()
                .anyMatch(user -> email.equalsIgnoreCase(user.getEmail()));
    }

    public void clear() { // для тестов
        userMap.clear();
        counter = 1L;
    }


    private long generateId() {
        return counter++;
    }
}