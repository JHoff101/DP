package com.bank.application.uzivatel.Repository;

import com.bank.application.uzivatel.Entity.Transakce;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransakceRepository extends JpaRepository<Transakce, Long> {
    List<Transakce> findByUzivatelId(Long uzivatelId);
}

