package com.bank.application.auth_app.Repository;

import com.bank.application.auth_app.Entity.Role;
import com.bank.application.auth_app.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

}

