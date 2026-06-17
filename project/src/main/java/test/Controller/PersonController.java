package test.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import test.entity.Person;
import test.Service.PersonenService;

import java.util.List;

@RestController
@RequestMapping("/api")
public class PersonController {
    @Autowired
    private PersonenService personService;

    private String currentUser() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    @GetMapping("/personen")
    public List<Person> getAllPersons() {
        return personService.getAllPersons(currentUser());
    }

    @GetMapping("/personen/{id}")
    public Person getPersonsById(@PathVariable int id) {
        return personService.getPersonById(id, currentUser());
    }

    @PostMapping("/personen")
    public Person savePerson(@RequestBody Person person) {
        return personService.savePerson(person, currentUser());
    }

    @DeleteMapping("/personen/{id}")
    public void deletePerson(@PathVariable int id) {
        personService.deletePersonById(id, currentUser());
    }

    /**
     * Admin-only Endpoint: liefert ALLE Personen aller User, unabhaengig
     * vom BOLA-Owner-Filter. Nur fuer Nutzer mit ROLE_ADMIN erreichbar
     * (siehe SecurityConfig: /api/admin/** -> hasRole("ADMIN")).
     *
     * Fuer die Privilege-Escalation-Demo: Wird hier mit einem manipulierten
     * Token (role=ADMIN, ohne gueltige Signatur + jwt-secure-validation=false)
     * trotzdem 200 OK zurueckgegeben, beweist das die fehlende Rechteprüfung.
     */
    @GetMapping("/admin/personen")
    public List<Person> getAllPersonsAsAdmin() {
        return personService.getAllPersonsUnfiltered();
    }
}
