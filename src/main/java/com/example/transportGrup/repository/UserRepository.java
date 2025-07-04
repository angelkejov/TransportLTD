package com.example.transportGrup.repository;

import com.example.transportGrup.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);

    Optional<User> findByEmail(String email);
    Optional<User> findByVerificationCode(String verificationCode);

    Optional<User> findByPhoneNumber(String newPhone);
}