package com.bank.application.uzivatel.Repository;

import com.bank.application.uzivatel.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
}

