package org.openhospital.smartdoc.modules.documents.repository;

import org.openhospital.smartdoc.modules.documents.model.DocumentType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface DocumentTypeRepository extends JpaRepository<DocumentType, UUID> {

	@Query("SELECT dt FROM DocumentType dt WHERE dt.code = ?1")
	Optional<DocumentType> findByCode(String code);
}
