package ru.practicum.shareit.user.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.user.model.User;

import java.util.List;
import java.util.Optional;

// класс обертка
@Repository
@Primary  // будет использоваться вместо InMemory
@RequiredArgsConstructor
public class DbUserRepository implements UserRepository {

    private final UserJpaRepository jpa;

    @Override
    public User create(User user) {
        return jpa.save(user);
    }

    @Override
    public User update(User user) {
        return jpa.save(user);
    }

    @Override
    public void deleteById(Long id) {
        jpa.deleteById(id);
    }

    @Override
    public Optional<User> findById(Long id) {
        return jpa.findById(id);
    }

    @Override
    public List<User> findAll() {
        return jpa.findAll();
    }

    @Override
    public boolean existsByEmail(String email) {
        return jpa.existsByEmail(email);
    }
}