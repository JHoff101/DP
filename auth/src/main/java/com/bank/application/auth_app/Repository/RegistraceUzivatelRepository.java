package com.bank.application.auth_app.Repository;

import com.bank.application.auth_app.Entity.RegistraceUzivatel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RegistraceUzivatelRepository extends JpaRepository<RegistraceUzivatel, Long> {
        boolean existsByEmail(String email);
        Optional<RegistraceUzivatel> findByEmail(String email);

}
