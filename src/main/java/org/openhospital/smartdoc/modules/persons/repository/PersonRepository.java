package org.openhospital.smartdoc.modules.persons.repository;

import org.openhospital.smartdoc.modules.persons.model.Person;
import org.openhospital.smartdoc.openapi.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.*;

public interface PersonRepository extends JpaRepository<Person, UUID> {

	@Query("SELECT p FROM Person p WHERE p.email = ?1")
	Optional<Person> findByEmail(String email);

	@Query("SELECT p FROM Person p WHERE p.name = ?1")
	Optional<Person> findByName(String name);

	@Query("SELECT p FROM Person p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', ?1, '%')) AND p.status IN ?2")
	Page<Person> findByNameContainingIgnoreCaseAndStatusIn(String name, List<Status> statuses, Pageable pageable);

	// Status-aware query methods
	Page<Person> findByStatusIn(List<Status> statuses, Pageable pageable);

	boolean existsByPidAndStatusNot(int pid, Status status);

	Optional<Person> findByIdAndStatusNot(UUID id, Status status);
}
