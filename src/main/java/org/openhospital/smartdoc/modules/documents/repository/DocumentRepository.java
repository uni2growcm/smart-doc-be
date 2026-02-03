package org.openhospital.smartdoc.modules.documents.repository;

import org.openhospital.smartdoc.modules.documents.model.Document;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface DocumentRepository extends CrudRepository<Document, String> {

    @Query("SELECT d FROM Document d WHERE d.person.id = ?1")
    List<Document> findByPersonId(String personId);

    @Query("SELECT d FROM Document d WHERE d.documentType.code = ?1")
    List<Document> findByType(String type);
}