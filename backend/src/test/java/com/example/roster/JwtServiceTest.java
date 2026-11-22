package com.example.roster;

import com.example.roster.security.JwtService;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {
    private static final String SECRET = "unit-test-secret-that-is-at-least-32-chars!!";

    @Test
    void generatedToken_roundTripsSubjectAndRole() {
        JwtService jwt = new JwtService(SECRET, 30);
        var claims = jwt.parse(jwt.generate("admin", "ADMIN"));
        assertEquals("admin", claims.getSubject());
        assertEquals("ADMIN", claims.get("role", String.class));
    }

    @Test
    void tokenSignedWithAnotherSecret_isRejected() {
        String token = new JwtService("a-completely-different-secret-of-32-chars!!", 30).generate("admin", "ADMIN");
        assertThrows(JwtException.class, () -> new JwtService(SECRET, 30).parse(token));
    }

    @Test
    void expiredToken_isRejected() {
        JwtService jwt = new JwtService(SECRET, -1);
        assertThrows(ExpiredJwtException.class, () -> jwt.parse(jwt.generate("admin", "ADMIN")));
    }
}
