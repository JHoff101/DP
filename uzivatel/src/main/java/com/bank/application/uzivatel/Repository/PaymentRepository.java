package com.bank.application.uzivatel.Repository;

import com.bank.application.uzivatel.Entity.Payment;
import com.bank.application.uzivatel.Entity.UserBalance;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment,Long> {
}
