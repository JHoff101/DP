package com.bank.application.uzivatel.Dao;

import com.bank.application.uzivatel.Entity.Payment;
import com.bank.application.uzivatel.Entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;

@Repository
public class PaymentJdbcDao {

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    public void setZustatek(Payment payment, User user) {
        BigDecimal newBalance = user.getUserBalance().getZustatek().subtract(payment.getCastka());

        String sql = "UPDATE users SET zustatek = :newBalance WHERE id = :userId";

        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("newBalance", newBalance);
        parameters.addValue("userId", user.getId());

        int rowsAffected = jdbcTemplate.update(sql, parameters);

        if (rowsAffected == 0) {
            throw new RuntimeException("Aktualizace zůstatku uživatele neproběhla úspěšně.");
        }
    }
}
