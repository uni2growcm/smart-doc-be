package org.openhospital.smartdoc.modules.documents.rest;

import org.junit.jupiter.api.*;
import org.openhospital.smartdoc.annotations.WithTestDatabase;
import org.openhospital.smartdoc.exceptions.CustomException;
import org.openhospital.smartdoc.helpers.TestHelpers;
import org.openhospital.smartdoc.modules.documents.port.IDocumentTypeService;
import org.openhospital.smartdoc.openapi.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@WithTestDatabase
@ActiveProfiles("test")
@SpringBootTest(
	webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT
)
public class DocumentTypeControllerTest {
	private IDocumentTypeService service;

	@BeforeEach
	public void setup() {
		service = TestHelpers.createService(IDocumentTypeService.class);
	}


	@Nested
	@DisplayName("GET /document-types")
	class FindDocumentTypes {
		@Test
		@DisplayName("Should find document types successfully")
		public void shouldFindDocumentTypes() {
			var result = service.findDocumentTypes(false);
			assertNotNull(result);
			assertEquals(10, result.size());
		}

		@Test
		@DisplayName("Should find document types with including inactive successfully")
		public void shouldFindDocumentTypesWithInactive() {
			var result = service.findDocumentTypes(true);
			assertNotNull(result);
			assertEquals(12, result.size());
		}
	}

	@Nested
	@DisplayName("POST /document-types")
	class CreateDocumentType {
		@Test
		@DisplayName("Should create document type successfully")
		public void shouldCreateDocumentTypeSuccessfully() {
			var request = new CreateDocumentTypeRequest()
				.code("NEW_TYPE")
				.name("New Document Type")
				.description("A new type for testing");
			var result = service.createDocumentType(request);
			assertNotNull(result);
			assertEquals("New Document Type", result.getName());
			assertEquals("NEW_TYPE", result.getCode());
		}

		@Test
		@DisplayName("Should throw exception for duplicate code")
		public void shouldThrowExceptionForDuplicateCode() {
			var request = new CreateDocumentTypeRequest()
				.code("ID_CARD")
				.name("Duplicate Code")
				.description("Duplicate code test");
			var exception = assertThrows(CustomException.class, () -> service.createDocumentType(request));
			assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
		}

		@Test
		@DisplayName("Should throw exception for missing required fields")
		public void shouldThrowExceptionForMissingRequiredFields() {
			var request = new CreateDocumentTypeRequest()
				.code("")
				.name("")
				.description("");
			var exception = assertThrows(CustomException.class, () -> service.createDocumentType(request));
			assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
		}
	}

	@Nested
	@DisplayName("GET /document-types/{id}")
	class FindDocumentTypeById {
		@Test
		@DisplayName("Should find document type by ID successfully")
		public void shouldFindDocumentTypeByIdSuccessfully() {
			var id = UUID.fromString("550e8400-e29b-41d4-a716-446655440000"); // ID_CARD
			var result = service.findDocumentTypeById(id);
			assertNotNull(result);
			assertEquals("Identity Card", result.getName());
			assertEquals(Status.ACTIVE, result.getStatus());
		}

		@Test
		@DisplayName("Should find inactive document type by ID")
		public void shouldFindInactiveDocumentTypeById() {
			var id = UUID.fromString("550e8400-e29b-41d4-a716-446655440010"); // VISA (inactive)
			var result = service.findDocumentTypeById(id);
			assertNotNull(result);
			assertEquals("Visa", result.getName());
			assertEquals(Status.INACTIVE, result.getStatus());
		}

		@Test
		@DisplayName("Should throw exception for non-existent ID")
		public void shouldThrowExceptionForNonExistentId() {
			var id = UUID.randomUUID();
			var exception = assertThrows(CustomException.class, () -> service.findDocumentTypeById(id));
			assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
		}

		@Test
		@DisplayName("Should throw exception for deleted document type")
		public void shouldThrowExceptionForDeletedDocumentType() {
			var id = UUID.fromString("550e8400-e29b-41d4-a716-446655440012"); // DIPLOMA (deleted)
			var exception = assertThrows(CustomException.class, () -> service.findDocumentTypeById(id));
			assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
		}
	}

	@Nested
	@DisplayName("PUT /document-types/{id}")
	class UpdateDocumentType {
		@Test
		@DisplayName("Should update document type successfully")
		public void shouldUpdateDocumentTypeSuccessfully() {
			var id = UUID.fromString("550e8400-e29b-41d4-a716-446655440001"); // PASSPORT
			var original = service.findDocumentTypeById(id);
			var request = new UpdateDocumentTypeRequest()
				.code(original.getCode())
				.name("Updated Passport")
				.description(original.getDescription())
				.version(original.getVersion());
			var result = service.updateDocumentType(id, request);
			assertNotNull(result);
			assertEquals("Updated Passport", result.getName());
		}

		@Test
		@DisplayName("Should throw exception for update non-existent")
		public void shouldThrowExceptionForUpdateNonExistent() {
			var id = UUID.randomUUID();
			var request = new UpdateDocumentTypeRequest()
				.code("NON_EXIST")
				.name("Non-existent")
				.description("Test")
				.version(0L);
			var exception = assertThrows(CustomException.class, () -> service.updateDocumentType(id, request));
			assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
		}
	}

	@Nested
	@DisplayName("PATCH /document-types/{id}")
	class PatchDocumentType {
		@Test
		@DisplayName("Should patch document type successfully")
		public void shouldPatchDocumentTypeSuccessfully() {
			var id = UUID.fromString("550e8400-e29b-41d4-a716-446655440003"); // BIRTH_CERT
			var request = new PatchDocumentTypeRequest()
				.name("Patched Birth Certificate");
			var result = service.patchDocumentType(id, request);
			assertNotNull(result);
			assertEquals("Patched Birth Certificate", result.getName());
		}

		@Test
		@DisplayName("Should handle empty patch")
		public void shouldHandleEmptyPatch() {
			var id = UUID.fromString("550e8400-e29b-41d4-a716-446655440004"); // MARRIAGE_CERT
			var request = new PatchDocumentTypeRequest(); // Empty patch
			var result = service.patchDocumentType(id, request);
			assertNotNull(result);
			assertEquals("Marriage Certificate", result.getName()); // Unchanged
		}

		@Test
		@DisplayName("Should throw exception for patch non-existent")
		public void shouldThrowExceptionForPatchNonExistent() {
			var id = UUID.randomUUID();
			var request = new PatchDocumentTypeRequest()
				.name("Patched Non-existent");
			var exception = assertThrows(CustomException.class, () -> service.patchDocumentType(id, request));
			assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
		}
	}

	@Nested
	@DisplayName("DELETE /document-types/{id}")
	class DeleteDocumentType {
		@Test
		@DisplayName("Should delete document type successfully")
		public void shouldDeleteDocumentTypeSuccessfully() {
			var id = UUID.fromString("550e8400-e29b-41d4-a716-446655440005"); // DRIVERS_LICENSE
			service.deleteDocumentType(id);
			// Verify deletion - should not be found anymore
			var exception = assertThrows(CustomException.class, () -> service.findDocumentTypeById(id));
			assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
		}

		@Test
		@DisplayName("Should throw exception for delete non-existent")
		public void shouldThrowExceptionForDeleteNonExistent() {
			var id = UUID.randomUUID();
			var exception = assertThrows(CustomException.class, () -> service.deleteDocumentType(id));
			assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
		}

		@Test
		@DisplayName("Should throw exception for delete already deleted")
		public void shouldThrowExceptionForDeleteAlreadyDeleted() {
			var id = UUID.fromString("550e8400-e29b-41d4-a716-446655440013"); // CONTRACT (deleted)
			var exception = assertThrows(CustomException.class, () -> service.deleteDocumentType(id));
			assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
		}
	}

	@Nested
	@DisplayName("PUT /document-types/{id}/activate")
	class ActivateDocumentType {
		@Test
		@DisplayName("Should activate document type successfully")
		public void shouldActivateDocumentTypeSuccessfully() {
			var id = UUID.fromString("550e8400-e29b-41d4-a716-446655440011"); // RESUME (inactive)
			var result = service.activateDocumentType(id);
			assertNotNull(result);
			assertEquals(Status.ACTIVE, result.getStatus());
		}

		@Test
		@DisplayName("Should throw exception for activate already active")
		public void shouldThrowExceptionForActivateAlreadyActive() {
			var id = UUID.fromString("550e8400-e29b-41d4-a716-446655440006"); // INSURANCE_CARD (active)
			var exception = assertThrows(CustomException.class, () -> service.activateDocumentType(id));
			assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
		}

		@Test
		@DisplayName("Should throw exception for activate non-existent")
		public void shouldThrowExceptionForActivateNonExistent() {
			var id = UUID.randomUUID();
			var exception = assertThrows(CustomException.class, () -> service.activateDocumentType(id));
			assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
		}
	}

	@Nested
	@DisplayName("PUT /document-types/{id}/deactivate")
	class DeactivateDocumentType {
		@Test
		@DisplayName("Should deactivate document type successfully")
		public void shouldDeactivateDocumentTypeSuccessfully() {
			var id = UUID.fromString("550e8400-e29b-41d4-a716-446655440007"); // ACADEMIC_RECORD (active)
			var result = service.deactivateDocumentType(id);
			assertNotNull(result);
			assertEquals(Status.INACTIVE, result.getStatus());
		}

		@Test
		@DisplayName("Should throw exception for deactivate already inactive")
		public void shouldThrowExceptionForDeactivateAlreadyInactive() {
			var id = UUID.fromString("550e8400-e29b-41d4-a716-446655440010"); // VISA (inactive)
			var exception = assertThrows(CustomException.class, () -> service.deactivateDocumentType(id));
			assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
		}

		@Test
		@DisplayName("Should throw exception for deactivate non-existent")
		public void shouldThrowExceptionForDeactivateNonExistent() {
			var id = UUID.randomUUID();
			var exception = assertThrows(CustomException.class, () -> service.deactivateDocumentType(id));
			assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
		}
	}

	@Nested
	@DisplayName("POST /document-types/{id}/restore")
	class RestoreDocumentType {
		@Test
		@DisplayName("Should restore document type successfully")
		public void shouldRestoreDocumentTypeSuccessfully() {
			var id = UUID.fromString("550e8400-e29b-41d4-a716-446655440012"); // DIPLOMA (deleted)
			var result = service.restoreDocumentType(id);
			assertNotNull(result);
			assertEquals(Status.ACTIVE, result.getStatus());
		}

		@Test
		@DisplayName("Should throw exception for restore non-deleted")
		public void shouldThrowExceptionForRestoreNonDeleted() {
			var id = UUID.fromString("550e8400-e29b-41d4-a716-446655440008"); // BANK_STATEMENT (active)
			var exception = assertThrows(CustomException.class, () -> service.restoreDocumentType(id));
			assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
		}

		@Test
		@DisplayName("Should throw exception for restore non-existent")
		public void shouldThrowExceptionForRestoreNonExistent() {
			var id = UUID.randomUUID();
			var exception = assertThrows(CustomException.class, () -> service.restoreDocumentType(id));
			assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
		}
	}
}