package com.bank.application.admin_app.Repository;

import com.bank.application.admin_app.Entity.User;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}

