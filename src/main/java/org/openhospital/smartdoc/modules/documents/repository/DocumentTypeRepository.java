package org.openhospital.smartdoc.modules.documents.repository;

import java.util.Optional;
import org.openhospital.smartdoc.modules.documents.model.DocumentType;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

public interface DocumentTypeRepository extends CrudRepository<DocumentType, String> {

    @Query("SELECT dt FROM DocumentType dt WHERE dt.code = ?1")
    Optional<DocumentType> findByCode(String code);
}
