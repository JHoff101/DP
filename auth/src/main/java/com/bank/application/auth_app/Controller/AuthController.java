package com.bank.application.auth_app.Controller;

import com.bank.application.auth_app.Entity.User;
import com.bank.application.auth_app.Repository.UserRepository;
import com.bank.application.auth_app.SecurityConfig.JwtTokenProvider;
import com.bank.application.auth_app.Service.AuthService;
import com.bank.application.auth_app.DTO.LoginRequestDTO;
import com.bank.application.auth_app.DTO.LoginResponseDTO;
import com.bank.application.auth_app.DTO.RegistraceUzivatelDTO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private static final Map<String, Map<String, Long>> failedLoginAttempts = new HashMap<>();
    private static final int MAX_ATTEMPTS = 5;
    private static final long BLOCK_TIME = TimeUnit.MINUTES.toMillis(60);

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequestDTO loginRequest) {
        String email = loginRequest.getEmail();

        if (failedLoginAttempts.containsKey(email)) {
            long currentTime = System.currentTimeMillis();
            long lastFailedAttemptTime = failedLoginAttempts.get(email).get("lastAttemptTime");
            long failedAttempts = failedLoginAttempts.get(email).get("count");

            if (failedAttempts >= MAX_ATTEMPTS && currentTime - lastFailedAttemptTime < BLOCK_TIME) {
                long retryAfter = lastFailedAttemptTime + BLOCK_TIME;
                SimpleDateFormat sdf = new SimpleDateFormat("d. M. yyyy H:mm", Locale.forLanguageTag("cs"));
                String formattedDate = sdf.format(new Date(retryAfter));

                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Překročil jste maximální počet pokusů, zkuste to prosím znovu v: " + formattedDate);
            } else if (failedAttempts >= MAX_ATTEMPTS) {
                failedLoginAttempts.remove(email);
            }
        }

        try {
            User user = userRepository.findByEmail(loginRequest.getEmail())
                    .orElseThrow(() -> new UsernameNotFoundException("Uživatel nenalezen."));

            if (!passwordEncoder.matches(loginRequest.getHeslo(), user.getHeslo())) {
                throw new BadCredentialsException("Neplatné přihlašovací údaje.");
            }

            String token = jwtTokenProvider.generateToken(loginRequest.getEmail());
            failedLoginAttempts.remove(email);

            return ResponseEntity.ok(new LoginResponseDTO(token, user.getJmeno(), user.getPrijmeni(), user.getEmail()));
        } catch (UsernameNotFoundException ex) {
            failedLoginAttempts.putIfAbsent(email, new HashMap<>());
            Map<String, Long> attempts = failedLoginAttempts.get(email);

            attempts.put("count", attempts.getOrDefault("count", 0L) + 1);
            attempts.put("lastAttemptTime", System.currentTimeMillis());

            if (attempts.get("count") >= MAX_ATTEMPTS) {
                long retryAfter = System.currentTimeMillis() + BLOCK_TIME;
                SimpleDateFormat sdf = new SimpleDateFormat("d. M. yyyy H:mm", Locale.forLanguageTag("cs"));
                String formattedDate = sdf.format(new Date(retryAfter));

                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Překročil jste maximální počet pokusů, zkuste to prosím znovu v: " + formattedDate);
            }

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Neplatné přihlašovací údaje.");
        } catch (BadCredentialsException ex) {
            failedLoginAttempts.putIfAbsent(email, new HashMap<>());
            Map<String, Long> attempts = failedLoginAttempts.get(email);

            attempts.put("count", attempts.getOrDefault("count", 0L) + 1);
            attempts.put("lastAttemptTime", System.currentTimeMillis());

            if (attempts.get("count") >= MAX_ATTEMPTS) {
                long retryAfter = System.currentTimeMillis() + BLOCK_TIME;
                SimpleDateFormat sdf = new SimpleDateFormat("d. M. yyyy H:mm", Locale.forLanguageTag("cs"));
                String formattedDate = sdf.format(new Date(retryAfter));

                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Překročil jste maximální počet pokusů, zkuste to prosím znovu v: " + formattedDate);
            }

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Neplatné přihlašovací údaje.");
        }
    }

    @PostMapping("/registrace")
    public ResponseEntity<String> registrovatUzivatele(@RequestBody @Valid RegistraceUzivatelDTO zadost) {
        return ResponseEntity.ok(authService.registraceUzivatele(zadost));
    }

    @PostMapping("/odhlaseni")
    public ResponseEntity<String> logout(HttpServletRequest request, HttpServletResponse response) {
        SecurityContextHolder.clearContext();
        return ResponseEntity.ok("Úspěšně odhlášeno.");
    }


}
