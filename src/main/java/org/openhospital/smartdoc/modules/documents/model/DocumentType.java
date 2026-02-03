package org.openhospital.smartdoc.modules.documents.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.openhospital.smartdoc.models.BaseEntity;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "document_types")
public class DocumentType extends BaseEntity {

    @Column(unique = true, nullable = false)
    private String code;

    @Column(nullable = false)
    private String name;

    private String description;
}
