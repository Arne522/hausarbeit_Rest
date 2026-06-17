package test.Controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import test.dto.AuthResponse;
import test.dto.LoginRequest;
import test.Service.AuthService;
import test.entity.AppUser;
import test.security.JwtUtil;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {

        AppUser user = authService.login(request.getUsername(), request.getPassword());

        return new AuthResponse(
                jwtUtil.generateAccessToken(user.getUsername(), user.getRole()),
                jwtUtil.generateRefreshToken(user.getUsername(), user.getRole())
        );
    }
}
