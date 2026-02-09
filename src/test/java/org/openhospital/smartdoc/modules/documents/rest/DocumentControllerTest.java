package org.openhospital.smartdoc.modules.documents.rest;

import org.junit.jupiter.api.*;
import org.mockito.InjectMocks;
import org.openhospital.smartdoc.annotations.WithTestDatabase;
import org.openhospital.smartdoc.exceptions.CustomException;
import org.openhospital.smartdoc.helpers.DateUtils;
import org.openhospital.smartdoc.helpers.TestHelpers;
import org.openhospital.smartdoc.modules.documents.port.IDocumentService;
import org.openhospital.smartdoc.modules.shared.port.IStorageService;
import org.openhospital.smartdoc.openapi.DocumentMetadata;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@WithTestDatabase
@ActiveProfiles("test")
@SpringBootTest(
	webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT
)
public class DocumentControllerTest {
	@InjectMocks
	IStorageService storageService;

	private IDocumentService service;

	@BeforeEach
	public void setup() {
		service = TestHelpers.createService(IDocumentService.class);
	}


	@Nested
	@DisplayName("GET /documents")
	class FindDocuments {
		@Test
		@DisplayName("Should find documents successfully")
		public void shouldFindDocuments() {
			var result = service.findDocuments(1, null, null, null, 0, 20);
			assertNotNull(result);
			// TODO: Update assertions based on filesystem test data
		}

		@Test
		@DisplayName("Should find documents with pagination successfully")
		public void shouldFindDocumentsWithPagination() {
			var result = service.findDocuments(1, null, null, null, 0, 5);
			assertNotNull(result);
			// TODO: Update assertions
		}

		@Test
		@DisplayName("Should filter documents by person")
		public void shouldFilterDocumentsByPerson() {
			var personId = 1; // Alice
			var result = service.findDocuments(personId, null, null, null, 0, 20);
			assertNotNull(result);
			// TODO: Update assertions
		}

		@Test
		@DisplayName("Should filter documents by type")
		public void shouldFilterDocumentsByType() {
			var type = "ID_CARD";
			var result = service.findDocuments(1, type, null, null, 0, 20);
			assertNotNull(result);
			// TODO: Update assertions
		}

		@Test
		@DisplayName("Should filter documents by date range")
		public void shouldFilterDocumentsByDateRange() {
			var fromDate = DateUtils.toInstant(LocalDate.of(2024, 1, 1));
			var toDate = DateUtils.toInstant(LocalDate.of(2024, 6, 30));
			var result = service.findDocuments(1, null, fromDate, toDate, 0, 20);
			assertNotNull(result);
			// TODO: Update assertions
		}
	}

	@Nested
	@DisplayName("GET /documents/{id}")
	class FindDocumentById {
		@Test
		@DisplayName("Should find document by ID successfully")
		public void shouldFindDocumentByIdSuccessfully() {
			var id = "00/00/01/ID_CARD/20240101_alice_id_card.jpg"; // example relative path
			var result = service.findDocumentById(id);
			assertNotNull(result);
			// TODO: Update assertions
		}

		@Test
		@DisplayName("Should throw exception for non-existent document ID")
		public void shouldThrowExceptionForNonExistentDocumentId() {
			var id = "nonexistent";
			var exception = assertThrows(CustomException.class, () -> service.findDocumentById(id));
			assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
		}
	}

	@Nested
	@DisplayName("POST /documents")
	class UploadDocument {
		@Test
		@DisplayName("Should upload document successfully")
		public void shouldUploadDocumentSuccessfully() {
			MultipartFile file = new MockMultipartFile("document", "test.pdf", "application/pdf", "test content".getBytes());
			var metadata = new DocumentMetadata()
				.personId(1) // Alice
				.type("ID_CARD");
			var result = service.uploadDocument(file, metadata);
			assertNotNull(result);
			// TODO: Update assertions
		}

		@Test
		@DisplayName("Should throw exception for invalid upload")
		public void shouldThrowExceptionForInvalidUpload() {
			MultipartFile file = new MockMultipartFile("document", "", "application/pdf", new byte[0]);
			var metadata = new DocumentMetadata();
			var exception = assertThrows(CustomException.class, () -> service.uploadDocument(file, metadata));
			assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
		}
	}

	@Nested
	@DisplayName("DELETE /documents/{id}")
	class DeleteDocument {
		@Test
		@DisplayName("Should delete document successfully")
		public void shouldDeleteDocumentSuccessfully() {
			var id = "00/00/01/ID_CARD/20240101_test.pdf"; // example
			service.deleteDocument(id);
			// TODO: Verify deletion
		}

		@Test
		@DisplayName("Should throw exception for delete non-existent")
		public void shouldThrowExceptionForDeleteNonExistent() {
			var id = "nonexistent";
			var exception = assertThrows(CustomException.class, () -> service.deleteDocument(id));
			assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
		}
	}

	@Nested
	@DisplayName("GET /documents/{id}/download")
	class DownloadDocument {
		@Test
		@DisplayName("Should download document successfully")
		public void shouldDownloadDocumentSuccessfully() {
			var id = "00/00/01/ID_CARD/20240101_test.pdf"; // example
			var result = service.downloadDocument(id, false);
			assertNotNull(result);
			assertEquals(HttpStatus.OK, result.getStatusCode());
		}

		@Test
		@DisplayName("Should throw exception for download non-existent")
		public void shouldThrowExceptionForDownloadNonExistent() {
			var id = "nonexistent";
			var exception = assertThrows(CustomException.class, () -> service.downloadDocument(id, false));
			assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
		}
	}
}


	@Nested
	@DisplayName("GET /documents")
	class FindDocuments {
		@Test
		@DisplayName("Should find documents successfully")
		public void shouldFindDocuments() {
			var result = service.findDocuments(null, null, null, null, 0, 20);
			assertNotNull(result);
			assertEquals(10, result.getData().size()); // 10 active
		}

		@Test
		@DisplayName("Should find documents with pagination successfully")
		public void shouldFindDocumentsWithPagination() {
			var result = service.findDocuments(null, null, null, null, 0, 5);
			assertNotNull(result);
			assertEquals(5, result.getData().size());
			assertEquals(0, result.getMetadata().getPage());
			assertEquals(10, result.getMetadata().getTotalElements());
			assertEquals(2, result.getMetadata().getTotalPages());
			result = service.findDocuments(null, null, null, null, 1, 5);
			assertNotNull(result);
			assertEquals(1, result.getMetadata().getPage());
		}

		@Test
		@DisplayName("Should filter documents by person")
		public void shouldFilterDocumentsByPerson() {
			var personId = UUID.fromString("660e8400-e29b-41d4-a716-446655440000"); // Alice
			var result = service.findDocuments(personId, null, null, null, 0, 20);
			assertNotNull(result);
			assertEquals(1, result.getData().size());
		}

		@Test
		@DisplayName("Should filter documents by type")
		public void shouldFilterDocumentsByType() {
			var typeId = UUID.fromString("550e8400-e29b-41d4-a716-446655440000"); // ID_CARD
			var result = service.findDocuments(null, typeId, null, null, 0, 20);
			assertNotNull(result);
			assertEquals(1, result.getData().size());
		}

		@Test
		@DisplayName("Should filter documents by date range")
		public void shouldFilterDocumentsByDateRange() {
			var fromDate = DateUtils.toInstant(LocalDate.of(2024, 1, 1));
			var toDate = DateUtils.toInstant(LocalDate.of(2024, 6, 30));
			var result = service.findDocuments(null, null, fromDate, toDate, 0, 20);
			assertNotNull(result);
			assertEquals(6, result.getData().size()); // Documents in Jan-Jun
		}
	}

	@Nested
	@DisplayName("GET /documents/{id}")
	class FindDocumentById {
		@Test
		@DisplayName("Should find document by ID successfully")
		public void shouldFindDocumentByIdSuccessfully() {
			var id = UUID.fromString("770e8400-e29b-41d4-a716-446655440000"); // alice_id_card
			var result = service.findDocumentById(id);
			assertNotNull(result);
			assertEquals("alice_id_card.jpg", result.getFileName());
		}

		@Test
		@DisplayName("Should throw exception for non-existent document ID")
		public void shouldThrowExceptionForNonExistentDocumentId() {
			var id = UUID.randomUUID();
			var exception = assertThrows(CustomException.class, () -> service.findDocumentById(id));
			assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
		}
	}

	@Nested
	@DisplayName("POST /documents")
	class UploadDocument {
		@Test
		@DisplayName("Should upload document successfully")
		public void shouldUploadDocumentSuccessfully() {
			MultipartFile file = new MockMultipartFile("document", "test.pdf", "application/pdf", "test content".getBytes());
			var metadata = new DocumentMetadata()
				.personId(UUID.fromString("660e8400-e29b-41d4-a716-446655440000")) // Alice
				.type(UUID.fromString("550e8400-e29b-41d4-a716-446655440000")) // ID_CARD
				.description("Test upload");
			var result = service.uploadDocument(file, metadata);
			assertNotNull(result);
			assertEquals("test.pdf", result.getFileName());
		}

		@Test
		@DisplayName("Should throw exception for invalid upload")
		public void shouldThrowExceptionForInvalidUpload() {
			MultipartFile file = new MockMultipartFile("document", "", "application/pdf", new byte[0]);
			var metadata = new DocumentMetadata();
			var exception = assertThrows(CustomException.class, () -> service.uploadDocument(file, metadata));
			assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
		}
	}

	@Nested
	@DisplayName("PUT /documents/{id}")
	class UpdateDocument {
		@Test
		@DisplayName("Should update document successfully")
		public void shouldUpdateDocumentSuccessfully() {
			var id = UUID.fromString("770e8400-e29b-41d4-a716-446655440001"); // bob_passport
			MultipartFile file = new MockMultipartFile("document", "updated.pdf", "application/pdf", "updated content".getBytes());
			var metadata = new DocumentMetadata()
				.personId(UUID.fromString("660e8400-e29b-41d4-a716-446655440001")) // Bob
				.type(UUID.fromString("550e8400-e29b-41d4-a716-446655440001")) // PASSPORT
				.description("Updated passport");
			var result = service.updateDocument(id, file, metadata);
			assertNotNull(result);
			assertEquals("updated.pdf", result.getFileName());
		}

		@Test
		@DisplayName("Should throw exception for update non-existent")
		public void shouldThrowExceptionForUpdateNonExistent() {
			var id = UUID.randomUUID();
			MultipartFile file = new MockMultipartFile("document", "test.pdf", "application/pdf", "content".getBytes());
			var metadata = new DocumentMetadata();
			var exception = assertThrows(CustomException.class, () -> service.updateDocument(id, file, metadata));
			assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
		}
	}

	@Nested
	@DisplayName("DELETE /documents/{id}")
	class DeleteDocument {
		@Test
		@DisplayName("Should delete document successfully")
		public void shouldDeleteDocumentSuccessfully() {
			var id = UUID.fromString("770e8400-e29b-41d4-a716-446655440005"); // frank_license
			service.deleteDocument(id);
			// Verify - should not be found
			var exception = assertThrows(CustomException.class, () -> service.findDocumentById(id));
			assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
		}

		@Test
		@DisplayName("Should throw exception for delete non-existent")
		public void shouldThrowExceptionForDeleteNonExistent() {
			var id = UUID.randomUUID();
			var exception = assertThrows(CustomException.class, () -> service.deleteDocument(id));
			assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
		}
	}

	@Nested
	@DisplayName("GET /documents/{id}/download")
	class DownloadDocument {
		@Test
		@DisplayName("Should download document successfully")
		public void shouldDownloadDocumentSuccessfully() {
			var id = UUID.fromString("770e8400-e29b-41d4-a716-446655440000"); // alice_id_card
			var result = service.downloadDocument(id, false);
			assertNotNull(result);
			assertEquals(HttpStatus.OK, result.getStatusCode());
		}

		@Test
		@DisplayName("Should throw exception for download non-existent")
		public void shouldThrowExceptionForDownloadNonExistent() {
			var id = "nonexistent";
			var exception = assertThrows(CustomException.class, () -> service.downloadDocument(id, false));
			assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
		}
	}
}