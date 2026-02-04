package org.openhospital.smartdoc.modules.persons.repository;

import org.openhospital.smartdoc.modules.persons.model.Person;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface PersonRepository extends JpaRepository<Person, UUID> {

	@Query("SELECT p FROM Person p WHERE p.email = ?1")
	Optional<Person> findByEmail(String email);

	@Query("SELECT p FROM Person p WHERE p.name = ?1")
	Optional<Person> findByName(String name);

	@Query("SELECT p FROM Person p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', ?1, '%'))")
	Page<Person> findByNameContainingIgnoreCase(String name, Pageable pageable);
}
