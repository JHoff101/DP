package com.bank.application.admin_app.Dao;
import com.bank.application.admin_app.DTO.AuditRecordDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class AuditJdbcDao {

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    private static final String SELECT_AUDIT_QUERY =
            "SELECT u.id AS user_id, CONCAT(u.jmeno, ' ', u.prijmeni) AS jmeno, t.castka AS castka, t.datum AS datum, t.typ as typ " +
                    "FROM uzivatel.uzivatel u " +
                    "JOIN uzivatel.transakce t ON u.id = t.uzivatel_id " +
                    "WHERE u.id = :uzivatelId " +
                    "ORDER BY t.datum DESC";

    public List<AuditRecordDTO> getAuditByUserId(Long uzivatelId) {
        Map<String, Object> params = new HashMap<>();
        params.put("uzivatelId", uzivatelId);

        return jdbcTemplate.query(SELECT_AUDIT_QUERY, params, auditRecordRowMapper());
    }

    public boolean pozastavitUcet(Long uzivatelId) {
        String sql = "UPDATE uzivatel.uzivatel SET account_status = 'SUSPENDED' WHERE id = :uzivatelId; " +
                "INSERT INTO admin.suspended_users (uzivatel_id, status) VALUES (:uzivatelId, 'SUSPENDED')";

        Map<String, Object> params = new HashMap<>();
        params.put("uzivatelId", uzivatelId);

        try {
            int rowsAffected = jdbcTemplate.update(sql, params);
            return rowsAffected > 0;
        } catch (Exception e) {
            return false;
        }
    }

    private RowMapper<AuditRecordDTO> auditRecordRowMapper() {
        return (rs, rowNum) -> {
            AuditRecordDTO auditRecord = new AuditRecordDTO();
            auditRecord.setUserId(rs.getLong("user_id"));
            auditRecord.setJmeno(rs.getString("jmeno"));
            auditRecord.setCastka(rs.getBigDecimal("castka"));
            auditRecord.setDatum(rs.getTimestamp("datum").toLocalDateTime());
            auditRecord.setTyp(rs.getString("typ"));
            return auditRecord;
        };
    }
}
