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
    pid                INT          NOT NULL,
    email              VARCHAR(255),
    phone_number       VARCHAR(255),
    gender             VARCHAR(255),
    status             ENUM('active', 'inactive', 'deleted')  NOT NULL DEFAULT 'active',
    UNIQUE KEY uk_persons_pid (pid)
);

-- Indexes for status columns
CREATE INDEX idx_persons_status ON persons (status);
CREATE INDEX idx_document_types_status ON document_types (status);
