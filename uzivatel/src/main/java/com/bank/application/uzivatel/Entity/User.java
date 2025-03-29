package com.bank.application.uzivatel.Entity;

import com.bank.application.uzivatel.dto.UpdateUserDTO;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "uzivatel", schema = "uzivatel")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String jmeno;
    private String prijmeni;
    private String email;
    private String adresa;
    private String twoFactorSecret;
    private int telefonniCislo;
    @ManyToOne
    @JoinColumn(name = "role_id")
    private Role role;
    @OneToOne
    @JoinColumn(name = "zustatek_id")
    private UserBalance userBalance;
    @Column(columnDefinition = "TEXT")
    private String pendingUpdatesJson;

    @JsonIgnore
    public void setPendingUpdates(User pendingUpdates) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            this.pendingUpdatesJson = objectMapper.writeValueAsString(pendingUpdates);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Chyba při serializaci pendingUpdates", e);
        }
    }

    @JsonIgnore
    public User getPendingUpdates() {
        if (this.pendingUpdatesJson == null) return null;
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(this.pendingUpdatesJson, User.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Chyba při deserializaci pendingUpdates", e);
        }
    }

    public void applyPendingUpdates() {
        User pending = getPendingUpdates();
        if (pending != null) {
            this.jmeno = pending.jmeno;
            this.prijmeni = pending.prijmeni;
            this.email = pending.email;
            this.adresa = pending.adresa;
            this.pendingUpdatesJson = null;
        }
    }

}
