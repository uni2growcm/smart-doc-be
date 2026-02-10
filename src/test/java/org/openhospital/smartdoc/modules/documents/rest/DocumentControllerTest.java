package org.openhospital.smartdoc.modules.documents.rest;

import org.junit.jupiter.api.*;
import org.openhospital.smartdoc.annotations.WithTestDatabase;
import org.openhospital.smartdoc.exceptions.CustomException;
import org.openhospital.smartdoc.helpers.DateUtils;
import org.openhospital.smartdoc.helpers.TestHelpers;
import org.openhospital.smartdoc.modules.documents.port.IDocumentService;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

@WithTestDatabase
@ActiveProfiles("test")
@SpringBootTest(
	webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT
)
public class DocumentControllerTest {
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
			assertEquals(1, result.getData().size());
			assertEquals("20260208_my-id-card.avif", result.getData().getFirst().getId());
			assertEquals(1, result.getData().getFirst().getPersonId());
			assertEquals("ID_CARD", result.getData().getFirst().getType());
		}

		@Test
		@DisplayName("Should find documents with pagination successfully")
		public void shouldFindDocumentsWithPagination() {
			var result = service.findDocuments(1, null, null, null, 0, 5);
			assertNotNull(result);
			assertEquals(1, result.getData().size());
		}

		@Test
		@DisplayName("Should filter documents by person")
		public void shouldFilterDocumentsByPerson() {
			var personId = 1; // Alice
			var result = service.findDocuments(personId, null, null, null, 0, 20);
			assertNotNull(result);
			assertEquals(1, result.getData().size());
			assertEquals(1, result.getData().getFirst().getPersonId());
		}

		@Test
		@DisplayName("Should filter documents by type")
		public void shouldFilterDocumentsByType() {
			var type = "ID_CARD";
			var result = service.findDocuments(1, type, null, null, 0, 20);
			assertNotNull(result);
			assertEquals(1, result.getData().size());
			assertEquals("ID_CARD", result.getData().getFirst().getType());
		}

		@Test
		@DisplayName("Should filter documents by date range")
		public void shouldFilterDocumentsByDateRange() {
			var fromDate = DateUtils.toInstant(LocalDate.of(2026, 1, 1));
			var toDate = DateUtils.toInstant(LocalDate.of(2026, 12, 31));
			var result = service.findDocuments(1, null, fromDate, toDate, 0, 20);
			assertNotNull(result);
			assertEquals(1, result.getData().size());
		}
	}

	@Nested
	@DisplayName("POST /documents")
	class UploadDocument {
		@Test
		@DisplayName("Should upload document successfully")
		public void shouldUploadDocumentSuccessfully() throws IOException {
			ClassPathResource resource = new ClassPathResource(
				"static/00/00/01/ID_CARD/20260208_my-id-card.avif"
			);

			try (InputStream is = resource.getInputStream()) {
				byte[] fileContent = is.readAllBytes();
				MultipartFile file = new MockMultipartFile("document", "my-id-card.avif", "image/avif", fileContent);
				var result = service.uploadDocument(file, 2, "ID_CARD", LocalDate.of(2026, 2, 8));
				assertNotNull(result);
				assert result.getId() != null;
				assertTrue(result.getId().startsWith("20260208_"));
			} catch (Exception e) {
				fail();
			}

		}

		@Test
		@DisplayName("Should throw exception when invalid client ID")
		public void shouldThrowExceptionWhenInvalidPersonId() {
			ClassPathResource resource = new ClassPathResource(
				"static/00/00/01/ID_CARD/20260208_my-id-card.avif"
			);

			try (InputStream is = resource.getInputStream()) {
				byte[] fileContent = is.readAllBytes();
				MultipartFile file = new MockMultipartFile("document", "my-id-card.avif", "image/avif", fileContent);
				var exception = assertThrows(CustomException.class, () -> service.uploadDocument(file, 14, "ID_CARD", null));
				assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
			} catch (Exception e) {
				fail();
			}
		}

		@Nested
		@DisplayName("GET /documents/{id}")
		class FindDocumentById {
			@Test
			@DisplayName("Should find document by ID successfully")
			public void shouldFindDocumentByIdSuccessfully() {
				var id = "20260208_my-id-card.avif";
				var result = service.findDocumentById(id, 1, "ID_CARD");
				assertNotNull(result);
				assertEquals(id, result.getId());
				assertEquals(1, result.getPersonId());
				assertEquals("ID_CARD", result.getType());
			}

			@Test
			@DisplayName("Should throw exception for non-existent document")
			public void shouldThrowExceptionForNonExistentDocument() {
				var id = "nonexistent.doc";
				var exception = assertThrows(CustomException.class, () -> service.findDocumentById(id, 1, "ID_CARD"));
				assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
			}

			@Test
			@DisplayName("Should throw exception for non-existent person")
			public void shouldThrowExceptionForNonExistentPerson() {
				var id = "20260208_my-id-card.avif";
				var exception = assertThrows(CustomException.class, () -> service.findDocumentById(id, 144, "ID_CARD"));
				assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
			}
		}

		@Nested
		@DisplayName("GET /documents/{id}/download")
		class DownloadDocument {
			@Test
			@DisplayName("Should download document successfully")
			public void shouldDownloadDocumentSuccessfully() {
				var id = "20260208_my-id-card.avif";
				var client = TestHelpers.buildRestClient();
				var result = client
					.get()
					.uri("/documents/%s/download?personId=1&type=ID_CARD".formatted(id))
					.accept(MediaType.ALL)
					.retrieve()
					.toBodilessEntity();
				assertNotNull(result);
				assertEquals(HttpStatus.OK, result.getStatusCode());
				assertEquals("image/avif", Objects.requireNonNull(result.getHeaders().getContentType()).toString());
			}

			@Test
			@DisplayName("Should throw exception for download non-existent")
			public void shouldThrowExceptionForDownloadNonExistent() {
				var id = "nonexistent.doc";
				var exception = assertThrows(CustomException.class, () -> service.downloadDocument(id, 1, "ID_CARD", false));
				assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
			}
		}
	}
}