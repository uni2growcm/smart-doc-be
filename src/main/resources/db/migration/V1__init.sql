-- Initial schema for SmartDoc API
-- Based on JPA entities: BaseEntity (abstract), Document, Person, DocumentType
-- Compatible with both MySQL and H2 for testing

-- Status enum values: active, inactive, deleted

CREATE TABLE document_types
(
    id                 VARCHAR(36)  NOT NULL PRIMARY KEY,
    created_by         VARCHAR(255),
    created_date       TIMESTAMP,
    last_modified_by   VARCHAR(255),
    last_modified_date TIMESTAMP,
    version            INTEGER      NOT NULL,
    code               VARCHAR(255) NOT NULL,
    name               VARCHAR(255) NOT NULL,
    description        VARCHAR(255),
    status             ENUM('active', 'inactive', 'deleted')  NOT NULL DEFAULT 'active',
    UNIQUE KEY uk_document_types_code (code)
);

CREATE TABLE persons
(
    id                 VARCHAR(36)  NOT NULL PRIMARY KEY,
    created_by         VARCHAR(255),
    created_date       TIMESTAMP,
    last_modified_by   VARCHAR(255),
    last_modified_date TIMESTAMP,
    version            INTEGER      NOT NULL,
    name               VARCHAR(255) NOT NULL,
    pid                VARCHAR(255) NOT NULL,
    email              VARCHAR(255),
    phone_number       VARCHAR(255),
    gender             VARCHAR(255),
    status             ENUM('active', 'inactive', 'deleted')  NOT NULL DEFAULT 'active',
    UNIQUE KEY uk_persons_pid (pid)
);

CREATE TABLE documents
(
    id                 VARCHAR(36) NOT NULL PRIMARY KEY,
    created_by         VARCHAR(255),
    created_date       TIMESTAMP,
    last_modified_by   VARCHAR(255),
    last_modified_date TIMESTAMP,
    version            INTEGER     NOT NULL,
    file_name          VARCHAR(255),
    path               VARCHAR(255),
    person_id          VARCHAR(36),
    type_id            VARCHAR(36),
    date               TIMESTAMP,
    description        VARCHAR(255),
    file_size          BIGINT,
    mime_type          VARCHAR(255),
    status             ENUM('active', 'archived', 'deleted')  NOT NULL DEFAULT 'active',
    upload_date        TIMESTAMP,
    CONSTRAINT fk_documents_person_id FOREIGN KEY (person_id) REFERENCES persons (id),
    CONSTRAINT fk_documents_type_id FOREIGN KEY (type_id) REFERENCES document_types (id)
);

-- Indexes for FKs
CREATE INDEX idx_documents_person_id ON documents (person_id);
CREATE INDEX idx_documents_type_id ON documents (type_id);

-- Indexes for status columns
CREATE INDEX idx_persons_status ON persons (status);
CREATE INDEX idx_document_types_status ON document_types (status);
