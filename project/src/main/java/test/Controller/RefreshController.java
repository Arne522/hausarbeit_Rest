package test.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import test.dto.AuthResponse;
import test.security.JwtUtil;

@RestController
@RequestMapping("/auth")
public class RefreshController {

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/refresh")
    public AuthResponse refresh(@RequestBody String refreshToken) {

        String username = jwtUtil.validateRefreshToken(refreshToken);
        String role = jwtUtil.extractRoleFromRefreshToken(refreshToken);

        return new AuthResponse(
                jwtUtil.generateAccessToken(username, role),
                refreshToken
        );
    }
}
