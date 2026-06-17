package test.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import test.entity.Person;
import test.Repository.PersonRepository;

import java.util.List;

@Service
public class PersonenService {

    @Autowired
    private PersonRepository personRepository;

    /**
     * Steuert, ob die Object-Level-Authorization-Pruefung aktiv ist.
     * false  -> verwundbar (BOLA): jeder eingeloggte User kann jede Person lesen/loeschen
     * true   -> sicher: nur der Owner (ownerUsername) darf auf seine Personen zugreifen
     */
    @Value("${app.security.bola-protection:true}")
    private boolean bolaProtectionEnabled;


    public List<Person> getAllPersons(String currentUsername) {
        if (!bolaProtectionEnabled) {
            return personRepository.findAll();
        }
        return personRepository.findByOwnerUsername(currentUsername);
    }


    public List<Person> getAllPersonsUnfiltered() {
        return personRepository.findAll();
    }

    /**
     * Speichert eine Person und setzt den aktuell eingeloggten User als Owner.
     */
    public Person savePerson(Person person, String currentUsername) {
        person.setOwnerUsername(currentUsername);
        return personRepository.save(person);
    }

    public Person getPersonById(int id, String currentUsername) {
        Person person = personRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Person nicht gefunden"));

        checkOwnership(person, currentUsername);
        return person;
    }

    public void deletePersonById(int id, String currentUsername) {
        Person person = personRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Person nicht gefunden"));

        checkOwnership(person, currentUsername);
        personRepository.deleteById(id);
    }


    private void checkOwnership(Person person, String currentUsername) {
        if (!bolaProtectionEnabled) {
            return;
        }
        if (person.getOwnerUsername() == null || !person.getOwnerUsername().equals(currentUsername)) {
            throw new AccessDeniedException("Kein Zugriff auf diese Person");
        }
    }
}
