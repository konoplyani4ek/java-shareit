package ru.practicum.shareit.user.repository;

import ru.practicum.shareit.user.model.User;

import java.util.List;
import java.util.Optional;

public interface UserRepository {

    User create(User item);

    User update(User item);

    void deleteById(Long id);

    Optional<User> getById(Long id);

    List<User> findAll();

    boolean existsByEmail(String email);
}
