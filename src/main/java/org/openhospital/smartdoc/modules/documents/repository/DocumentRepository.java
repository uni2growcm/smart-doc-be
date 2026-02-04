package org.openhospital.smartdoc.modules.documents.repository;

import org.openhospital.smartdoc.modules.documents.model.Document;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.UUID;

public interface DocumentRepository extends CrudRepository<Document, String> {

	@Query("SELECT d FROM Document d WHERE d.person.id = ?1")
	List<Document> findByPersonId(UUID personId);

	@Query("SELECT d FROM Document d WHERE d.type.id = ?1")
	List<Document> findByType(UUID type);
}
