package org.openhospital.smartdoc.modules.persons.repository;

import java.util.Optional;
import org.openhospital.smartdoc.modules.persons.model.Person;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

public interface PersonRepository extends CrudRepository<Person, String> {

    @Query("SELECT p FROM Person p WHERE p.email = ?1")
    Optional<Person> findByEmail(String email);

    @Query("SELECT p FROM Person p WHERE p.name = ?1")
    Optional<Person> findByName(String name);
}
