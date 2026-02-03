package org.openhospital.smartdoc.modules.documents.repository;

import org.openhospital.smartdoc.modules.documents.model.DocumentType;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface DocumentTypeRepository extends CrudRepository<DocumentType, String> {

    @Query("SELECT dt FROM DocumentType dt WHERE dt.code = ?1")
    Optional<DocumentType> findByCode(String code);
}