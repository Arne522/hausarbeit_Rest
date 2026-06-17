package test.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.access-expiration-ms}")
    private long accessExpirationMs;

    @Value("${jwt.refresh-expiration-ms}")
    private long refreshExpirationMs;

    private Key key() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    // ACCESS TOKEN
    public String generateAccessToken(String username, String role) {
        return Jwts.builder()
                .setSubject(username)
                .claim("type", "access")
                .claim("role", role)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + accessExpirationMs))
                .signWith(key(), SignatureAlgorithm.HS256)
                .compact();
    }

    // REFRESH TOKEN
    public String generateRefreshToken(String username, String role) {
        return Jwts.builder()
                .setSubject(username)
                .claim("type", "refresh")
                .claim("role", role)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + refreshExpirationMs))
                .signWith(key(), SignatureAlgorithm.HS256)
                .compact();
    }

    private Claims parse(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * Validiert ein beliebiges Token und gibt den Username (Subject) zurück.
     */
    public String validateToken(String token) {
        return parse(token).getSubject();
    }

    /**
     * VERWUNDBARE Variante zu Demonstrationszwecken (Broken Authentication):
     * - Akzeptiert Tokens mit alg=none (unsignierte Tokens)
     * - Prueft die Signatur fuer signierte Tokens NICHT
     * - Ignoriert die Ablaufzeit (exp)
     */
    public String validateTokenInsecure(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length < 2) {
                throw new JwtException("Ungueltiges Token-Format");
            }
            String payloadJson = new String(
                    java.util.Base64.getUrlDecoder().decode(parts[1]),
                    java.nio.charset.StandardCharsets.UTF_8
            );
            int subIndex = payloadJson.indexOf("\"sub\"");
            if (subIndex == -1) {
                throw new JwtException("Kein subject im Token");
            }
            int colon = payloadJson.indexOf(':', subIndex);
            int firstQuote = payloadJson.indexOf('"', colon + 1);
            int secondQuote = payloadJson.indexOf('"', firstQuote + 1);
            return payloadJson.substring(firstQuote + 1, secondQuote);
        } catch (Exception e) {
            throw new JwtException("Token konnte nicht gelesen werden", e);
        }
    }

    /**
     * Liest die Rolle (Claim "role") aus einem gueltigen, signierten Token.
     */
    public String extractRole(String token) {
        Object role = parse(token).get("role");
        return role != null ? role.toString() : "USER";
    }

    /**
     * Validiert speziell ein Refresh-Token (prüft den "type"-Claim) und gibt den Username zurück.
     * Wirft eine Exception, falls es sich nicht um ein Refresh-Token handelt.
     */
    public String validateRefreshToken(String token) {
        Claims claims = parse(token);
        if (!"refresh".equals(claims.get("type"))) {
            throw new JwtException("Kein gültiges Refresh-Token");
        }
        return claims.getSubject();
    }

    /**
     * Liest die Rolle aus einem Refresh-Token (signaturgeprueft).
     */
    public String extractRoleFromRefreshToken(String token) {
        return extractRole(token);
    }

    /**
     * VERWUNDBARE Variante: liest den Claim "role" direkt aus dem
     * Base64-codierten Payload, ohne Signaturpruefung. Faellt eine
     * eigene "role" im Token vor, wird diese ungeprueft uebernommen -
     * genau das macht die Privilege-Escalation-Demo moeglich.
     */
    public String extractRoleInsecure(String token) {
        try {
            String[] parts = token.split("\\.");
            String payloadJson = new String(
                    java.util.Base64.getUrlDecoder().decode(parts[1]),
                    java.nio.charset.StandardCharsets.UTF_8
            );
            int roleIndex = payloadJson.indexOf("\"role\"");
            if (roleIndex == -1) {
                return "USER";
            }
            int colon = payloadJson.indexOf(':', roleIndex);
            int firstQuote = payloadJson.indexOf('"', colon + 1);
            int secondQuote = payloadJson.indexOf('"', firstQuote + 1);
            return payloadJson.substring(firstQuote + 1, secondQuote);
        } catch (Exception e) {
            return "USER";
        }
    }
}

