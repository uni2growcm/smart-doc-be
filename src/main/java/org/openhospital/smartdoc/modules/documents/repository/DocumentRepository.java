package org.openhospital.smartdoc.modules.documents.repository;

import org.openhospital.smartdoc.modules.documents.model.Document;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface DocumentRepository extends JpaRepository<Document, UUID> {

	@Query("SELECT d FROM Document d WHERE d.person.id = ?1")
	List<Document> findByPersonId(UUID personId);

	@Query("SELECT d FROM Document d WHERE d.type.id = ?1")
	List<Document> findByType(UUID type);

	@Query("""
		SELECT d FROM Document d
		WHERE (?1 IS NULL OR d.person.id = ?1)
		AND (?2 IS NULL OR d.type.id = ?2)
		AND (?3 IS NULL OR d.date >= ?3)
		AND (?4 IS NULL OR d.date <= ?4)
		ORDER BY d.date DESC
		""")
	Page<Document> findWithFilters(UUID personId, UUID typeId, Instant fromDate, Instant toDate, Pageable pageable);
}