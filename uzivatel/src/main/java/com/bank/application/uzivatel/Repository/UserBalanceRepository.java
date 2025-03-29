package com.bank.application.uzivatel.Repository;

import com.bank.application.uzivatel.Entity.UserBalance;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserBalanceRepository extends JpaRepository<UserBalance,Long> {


}
