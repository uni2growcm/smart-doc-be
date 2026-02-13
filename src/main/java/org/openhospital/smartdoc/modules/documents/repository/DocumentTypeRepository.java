package org.openhospital.smartdoc.modules.documents.repository;

import org.openhospital.smartdoc.modules.documents.model.DocumentType;
import org.openhospital.smartdoc.openapi.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.*;

public interface DocumentTypeRepository extends JpaRepository<DocumentType, UUID> {

	@Query("SELECT dt FROM DocumentType dt WHERE dt.code = ?1")
	Optional<DocumentType> findByCode(String code);

	Page<DocumentType> findByStatusIn(List<Status> statuses, Pageable pageable);

	boolean existsByCodeAndStatusNot(String code, Status status);

	Optional<DocumentType> findByIdAndStatusNot(UUID id, Status status);
}
