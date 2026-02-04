package org.openhospital.smartdoc.modules.persons.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.openhospital.smartdoc.models.BaseEntity;
import org.openhospital.smartdoc.modules.documents.model.Document;
import org.openhospital.smartdoc.openapi.Gender;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "persons")
public class Person extends BaseEntity {

    private String name;

    @Column(unique = true, nullable = false)
    private String pid;

    private String email;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    @OneToMany(mappedBy = "person", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Document> documents = new ArrayList<>();
}
