package test.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import test.entity.AppUser;
import test.security.JwtUtil;

@Service
public class AuthService {

    @Autowired
    private test.Repository.UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public AppUser login(String username, String password) {

        AppUser user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User nicht gefunden"));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Falsches Passwort");
        }

        return user;
    }
}
