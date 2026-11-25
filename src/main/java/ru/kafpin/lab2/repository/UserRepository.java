package ru.kafpin.lab2.repository;

import org.springframework.data.repository.CrudRepository;
import ru.kafpin.lab2.entity.AppUser;

import java.util.Optional;

public interface UserRepository extends CrudRepository<AppUser, Long> {
    Optional<AppUser> findByUsername(String username);
}