package test.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import test.entity.AppUser;

import java.util.Optional;

public interface UserRepository extends JpaRepository<AppUser, Long> {
    Optional<AppUser> findByUsername(String username);
}
