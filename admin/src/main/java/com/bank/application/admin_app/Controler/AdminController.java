package com.bank.application.admin_app.Controler;

import com.bank.application.admin_app.DTO.AuditRecordDTO;
import com.bank.application.admin_app.Dao.AuditJdbcDao;
import com.bank.application.admin_app.Entity.User;
import com.bank.application.admin_app.Repository.UserRepository;
import com.bank.application.admin_app.SecurityConfig.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    AuditJdbcDao auditJdbcDao;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;
    @Autowired
    private UserRepository userRepository;

    @GetMapping("/{uzivatelId}/audit")
    public ResponseEntity<List<AuditRecordDTO>> getUserAudit(@PathVariable Long uzivatelId) {
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

        List<AuditRecordDTO> auditRecords = auditJdbcDao.getAuditByUserId(uzivatelId);

        if (auditRecords.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(auditRecords);
    }

    @PostMapping("/{uzivatelId}/pozastavit")
    public ResponseEntity<String> pozastavitUcet(@PathVariable("uzivatelId") Long uzivatelId) {
        try {
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

            if (auditJdbcDao.pozastavitUcet(uzivatelId)) {
                return ResponseEntity.ok("Účet byl úspěšně pozastaven.");
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Uživatel nebyl nalezen.");
            }

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Došlo k chybě při pozastavování účtu.");
        }
    }

}
