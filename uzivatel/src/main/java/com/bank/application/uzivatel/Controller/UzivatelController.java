package com.bank.application.uzivatel.Controller;

import com.bank.application.uzivatel.Dao.PaymentJdbcDao;
import com.bank.application.uzivatel.Entity.Payment;
import com.bank.application.uzivatel.Entity.User;
import com.bank.application.uzivatel.Entity.UserBalance;
import com.bank.application.uzivatel.Repository.PaymentRepository;
import com.bank.application.uzivatel.Repository.TransakceRepository;
import com.bank.application.uzivatel.Repository.UserBalanceRepository;
import com.bank.application.uzivatel.Repository.UserRepository;
import com.bank.application.uzivatel.SecurityConfig.JwtTokenProvider;
import com.bank.application.uzivatel.Service.TwoFactorAuthService;
import com.bank.application.uzivatel.dto.PaymentRequestDTO;
import com.bank.application.uzivatel.dto.TransakceDTO;
import com.bank.application.uzivatel.dto.UpdateUserDTO;
import com.bank.application.uzivatel.dto.UserProfileResponseDTO;
import jakarta.transaction.Transactional;
import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.math.BigDecimal;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/uzivatel")
public class UzivatelController {

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private UserBalanceRepository userBalanceRepository;

    @Autowired
    private TransakceRepository transakceRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private  TwoFactorAuthService twoFactorAuthService;

    @Autowired
    PaymentJdbcDao paymentJdbcDao;

    @Autowired
    PaymentRepository paymentRepository;


    @GetMapping("/{uzivatelId}/zustatek")
    public ResponseEntity<BigDecimal> getZustatek(@PathVariable Long uzivatelId) {
        String token = jwtTokenProvider.getJwtTokenFromRequest();

        if (token == null || !jwtTokenProvider.validateToken(token)) {
            return ResponseEntity.status(401).body(null);
        }

        Authentication authentication = jwtTokenProvider.getAuthentication(token);
        Optional<User> user = userRepository.findById(uzivatelId);
        if (user.isEmpty()) {
            throw new NoSuchElementException("Uživatel s ID " + uzivatelId + " nebyl nalezen.");
        }

        if (authentication.getAuthorities().stream()
                .noneMatch(grantedAuthority -> grantedAuthority.getAuthority()
                        .equals(user.get().getRole().getNazev()))) {
            System.out.println("Neplatné role pro uživatele");
            return ResponseEntity.status(403).body(null);
        }

        Optional<UserBalance> zustatek = userBalanceRepository.findById(uzivatelId);

        return zustatek.map(userBalance -> ResponseEntity.ok(userBalance.getZustatek())).orElseGet(() -> ResponseEntity.status(404).body(null));
    }

    @GetMapping("/{uzivatelId}/transakce")
    public ResponseEntity<List<TransakceDTO>> getHistorieTransakci(@PathVariable Long uzivatelId) {
        String token = jwtTokenProvider.getJwtTokenFromRequest();

        if (token == null || !jwtTokenProvider.validateToken(token)) {
            return ResponseEntity.status(401).body(null);
        }

        Authentication authentication = jwtTokenProvider.getAuthentication(token);
        Optional<User> user = userRepository.findById(uzivatelId);
        if (user.isEmpty()) {
            throw new NoSuchElementException("Uživatel s ID " + uzivatelId + " nebyl nalezen.");
        }

        if (authentication.getAuthorities().stream()
                .noneMatch(grantedAuthority -> grantedAuthority.getAuthority()
                        .equals(user.get().getRole().getNazev()))) {
            System.out.println("Neplatné role pro uživatele");
            return ResponseEntity.status(403).body(null);
        }

        List<TransakceDTO> transakce = transakceRepository.findByUzivatelId(uzivatelId)
                .stream()
                .map(transaction -> new TransakceDTO(
                        transaction.getId(),
                        transaction.getCastka(),
                        transaction.getDatum(),
                        transaction.getTyp()))
                .collect(Collectors.toList());

        return ResponseEntity.ok(transakce);
    }

    @GetMapping("/{uzivatelId}/profil")
    public ResponseEntity<UserProfileResponseDTO> getProfil(@PathVariable Long uzivatelId) {
        String token = jwtTokenProvider.getJwtTokenFromRequest();
        if (token == null || !jwtTokenProvider.validateToken(token)) {
            return ResponseEntity.status(401).body(null);
        }

        Authentication authentication = jwtTokenProvider.getAuthentication(token);
        User user = userRepository.findById(uzivatelId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Uživatel nenalezen"));
        if (authentication.getAuthorities().stream()
                .noneMatch(grantedAuthority -> grantedAuthority.getAuthority()
                        .equals(user.getRole().getNazev()))) {
            System.out.println("Neplatné role pro uživatele");
            return ResponseEntity.status(403).body(null);
        }

        return ResponseEntity.ok(new UserProfileResponseDTO(user.getJmeno(), user.getPrijmeni(), user.getEmail(), user.getAdresa(), user.getTelefonniCislo()));
    }

    @PatchMapping("/{uzivatelId}/profil/aktualizace")
    @Transactional
    public ResponseEntity<byte[]> vygenerujQRaUlozUzivatele(@PathVariable Long uzivatelId, @RequestBody UpdateUserDTO updatedUserDTO) throws Exception {
        String token = jwtTokenProvider.getJwtTokenFromRequest();
        if (token == null || !jwtTokenProvider.validateToken(token)) {
            return ResponseEntity.status(401).body(null);
        }

        Authentication authentication = jwtTokenProvider.getAuthentication(token);
        User user = userRepository.findById(uzivatelId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Uživatel nenalezen"));
        if (authentication.getAuthorities().stream()
                .noneMatch(grantedAuthority -> grantedAuthority.getAuthority()
                        .equals(user.getRole().getNazev()))) {
            System.out.println("Neplatné role pro uživatele");
            return ResponseEntity.status(403).body(null);
        }

        String secretKey = twoFactorAuthService.generateSecretKey();
        String qrCodeBase64 = twoFactorAuthService.generateQRCodeBase64(user.getEmail(), secretKey);
        user.setTwoFactorSecret(secretKey);

        User pendingUser = new User();
        pendingUser.setEmail(updatedUserDTO.getEmail());
        pendingUser.setJmeno(updatedUserDTO.getJmeno());
        pendingUser.setPrijmeni(updatedUserDTO.getPrijmeni());
        pendingUser.setAdresa(updatedUserDTO.getAdresa());

        user.setPendingUpdates(pendingUser);
        userRepository.save(user);

        System.out.println("Saved Secret Key: " + secretKey);

        byte[] imageBytes = Base64.decodeBase64(qrCodeBase64);
        return ResponseEntity.ok().contentType(MediaType.IMAGE_PNG).body(imageBytes);
    }


    @PatchMapping("/{uzivatelId}/profil/aktualizace-2fa")
    public ResponseEntity<String> ulozUzivatelePo2FA(@PathVariable Long uzivatelId, @RequestParam String code) {
        User user = userRepository.findById(uzivatelId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Uživatel nenalezen"));

        String secretKey = user.getTwoFactorSecret();
        if (secretKey == null || secretKey.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Tajný klíč pro 2FA není nastaven.");
        }

        System.out.println("Retrieved Secret Key: " + secretKey);
        System.out.println("Entered Code: " + code);

        boolean isCodeValid = twoFactorAuthService.verifyTwoFactorCode(secretKey, code);

        if (isCodeValid) {
            user.applyPendingUpdates();
            userRepository.save(user);
            System.out.println("Saved: " + user);
            return ResponseEntity.ok("Změny byly úspěšně potvrzeny a uloženy.");
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Neplatný ověřovací kód.");
        }
    }


    @PostMapping("/{uzivatelId}/platba/generovat-qr")
    @Transactional
    public ResponseEntity<byte[]> generovatQRProPlatbu(@PathVariable Long uzivatelId, @RequestBody PaymentRequestDTO paymentRequest) throws Exception {
        String token = jwtTokenProvider.getJwtTokenFromRequest();
        if (token == null || !jwtTokenProvider.validateToken(token)) {
            return ResponseEntity.status(401).body(null);
        }

        Authentication authentication = jwtTokenProvider.getAuthentication(token);
        User user = userRepository.findById(uzivatelId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Uživatel nenalezen"));
        if (authentication.getAuthorities().stream()
                .noneMatch(grantedAuthority -> grantedAuthority.getAuthority()
                        .equals(user.getRole().getNazev()))) {
            System.out.println("Neplatné role pro uživatele");
            return ResponseEntity.status(403).body(null);
        }

        String secretKey = twoFactorAuthService.generateSecretKey();
        String qrCodeBase64 = twoFactorAuthService.generateQRCodeBase64(user.getEmail(), secretKey);
        user.setTwoFactorSecret(secretKey);
        userRepository.save(user);

        System.out.println("Saved Secret Key: " + secretKey);

        byte[] imageBytes = Base64.decodeBase64(qrCodeBase64);
        return ResponseEntity.ok().contentType(MediaType.IMAGE_PNG).body(imageBytes);
    }

    @PostMapping("/{uzivatelId}/platba/odeslat")
    @Transactional
    public ResponseEntity<String> odeslatPlatbu(@PathVariable Long uzivatelId, @RequestBody PaymentRequestDTO paymentRequest, @RequestParam String code) {
        String token = jwtTokenProvider.getJwtTokenFromRequest();
        if (token == null || !jwtTokenProvider.validateToken(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Neplatný nebo chybějící token.");
        }

        Authentication authentication = jwtTokenProvider.getAuthentication(token);
        User user = userRepository.findById(uzivatelId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Uživatel nenalezen"));

        if (authentication.getAuthorities().stream()
                .noneMatch(grantedAuthority -> grantedAuthority.getAuthority().equals(user.getRole().getNazev()))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Uživatel nemá potřebná oprávnění.");
        }

        String secretKey = user.getTwoFactorSecret();
        if (secretKey == null || secretKey.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Tajný klíč pro 2FA není nastaven.");
        }

        boolean isCodeValid = twoFactorAuthService.verifyTwoFactorCode(secretKey, code);
        if (!isCodeValid) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Neplatný ověřovací kód.");
        }

        try {
            Payment payment = new Payment();
            payment.setUser(user);
            payment.setCastka(paymentRequest.getCastka());
            payment.setStatus("PENDING");
            payment.setRecipient(paymentRequest.getRecipientId());
            paymentRepository.save(payment);
            boolean isPaymentSuccessful = processPayment(payment, user);

            if (isPaymentSuccessful) {
                payment.setStatus("COMPLETED");
                paymentRepository.save(payment);
                return ResponseEntity.ok("Platba byla úspěšně odeslána.");
            } else {
                payment.setStatus("FAILED");
                paymentRepository.save(payment);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Chyba při zpracování platby.");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Chyba serveru při zpracování platby.");
        }
    }

    private boolean processPayment(Payment payment, User user) {
        try {
            BigDecimal userBalance = user.getUserBalance().getZustatek();

            if (userBalance.compareTo(payment.getCastka()) < 0) {
                return false;
            }

            paymentJdbcDao.setZustatek(payment, user);

            return true;

        } catch (Exception e) {
            return false;
        }
    }
}
