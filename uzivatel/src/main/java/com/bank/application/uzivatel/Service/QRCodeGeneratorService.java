package com.bank.application.uzivatel.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class QRCodeGeneratorService {

    public static String getGoogleAuthenticatorQRUrl(String secret, String userEmail, String issuer) {
        String otpAuthURL = String.format(
                "otpauth://totp/%s:%s?secret=%s&issuer=%s",
                issuer, userEmail, secret, issuer
        );
        return "https://chart.googleapis.com/chart?chs=200x200&cht=qr&chl=" + URLEncoder.encode(otpAuthURL, StandardCharsets.UTF_8);
    }

}

