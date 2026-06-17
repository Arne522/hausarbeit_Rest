package test.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import test.entity.Person;

import java.util.List;

public interface PersonRepository extends JpaRepository<Person, Integer> {
    List<Person> findByOwnerUsername(String ownerUsername);
}
