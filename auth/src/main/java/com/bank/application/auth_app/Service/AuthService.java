package com.bank.application.auth_app.Service;

import com.bank.application.auth_app.Entity.RegistraceUzivatel;
import com.bank.application.auth_app.Entity.Role;
import com.bank.application.auth_app.Repository.RegistraceUzivatelRepository;
import com.bank.application.auth_app.Repository.RoleRepository;
import com.bank.application.auth_app.DTO.RegistraceUzivatelDTO;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.HashMap;
import java.util.Map;

@Service
@Validated
public class AuthService {

    @Autowired
    private RegistraceUzivatelRepository uzivatelRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private RoleRepository roleRepository;

    public boolean isHesloSilne(String heslo) {
        return heslo.length() >= 8
                && heslo.matches(".*[!@#$%^&*].*")
                && heslo.matches(".*\\d.*");
    }

    @Transactional
    public String registraceUzivatele(RegistraceUzivatelDTO zadost) {
        if (existsByEmail(zadost.getEmail())) {
            throw new EmailUzExistujeException("E-mail je již používán.");
        }
        if (!isHesloSilne(zadost.getHeslo())) {
            throw new InvalidPasswordException("Heslo musí být silné.");
        }


        RegistraceUzivatel uzivatel = new RegistraceUzivatel();
        uzivatel.setJmeno(zadost.getJmeno());
        uzivatel.setPrijmeni(zadost.getPrijmeni());
        uzivatel.setEmail(zadost.getEmail());
        uzivatel.setTelefon(zadost.getTelefon());
        uzivatel.setDatumNarozeni(zadost.getDatumNarozeni());
        uzivatel.setHeslo(passwordEncoder.encode(zadost.getHeslo()));

        Role role = roleRepository.findByNazev("ROLE_UZIVATEL")
                .orElseThrow(() -> new RuntimeException("Role ROLE_UZIVATEL není definována."));
        uzivatel.setRole(role);


        uzivatelRepository.save(uzivatel);

        return "Uživatel úspěšně zaregistrován";
    }

    private boolean existsByEmail(String email) {
        return uzivatelRepository.existsByEmail(email);
    }




















    @ExceptionHandler(EmailUzExistujeException.class)
    public ResponseEntity<String> handleEmailUzExistujeException(EmailUzExistujeException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    @ExceptionHandler(InvalidPasswordException.class)
    public ResponseEntity<String> handleInvalidPasswordException(InvalidPasswordException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    @ExceptionHandler(InvalidDateOfBirthException.class)
    public ResponseEntity<String> handleInvalidDateOfBirthException(InvalidDateOfBirthException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage())
        );
        return ResponseEntity.badRequest().body(errors);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public class EmailUzExistujeException extends RuntimeException {
        public EmailUzExistujeException(String message) {
            super(message);
        }
    }


    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public class InvalidPasswordException extends RuntimeException {
        public InvalidPasswordException(String message) {
            super(message);
        }
    }


    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public class InvalidDateOfBirthException extends RuntimeException {
        public InvalidDateOfBirthException(String message) {
            super(message);
        }
    }

}
