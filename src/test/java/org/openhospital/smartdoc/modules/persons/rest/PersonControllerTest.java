package org.openhospital.smartdoc.modules.persons.rest;

import org.junit.jupiter.api.*;
import org.openhospital.smartdoc.annotations.WithTestDatabase;
import org.openhospital.smartdoc.exceptions.CustomException;
import org.openhospital.smartdoc.helpers.DateUtils;
import org.openhospital.smartdoc.helpers.TestHelpers;
import org.openhospital.smartdoc.modules.persons.port.IPersonService;
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
public class PersonControllerTest {
	private IPersonService service;

	@BeforeEach
	public void setup() {
		service = TestHelpers.createService(IPersonService.class);
	}


	@Nested
	@DisplayName("GET /persons")
	class FindPersons {
		@Test
		@DisplayName("Should find persons successfully")
		public void shouldFindPersons() {
			var result = service.findPersons(null, false, 0, 20);
			assertNotNull(result);
			assertEquals(10, result.getData().size());
		}

		@Test
		@DisplayName("Should find persons with pagination successfully")
		public void shouldFindPersonsWithPagination() {
			var result = service.findPersons(null, false, 0, 5);
			assertNotNull(result);
			assertEquals(5, result.getData().size());
			assertEquals(0, result.getMetadata().getPage());
			assertEquals(10, result.getMetadata().getTotalElements());
			assertEquals(2, result.getMetadata().getTotalPages());
			result = service.findPersons(null, false, 1, 5);
			assertNotNull(result);
			assertEquals(1, result.getMetadata().getPage());
		}

		@Test
		@DisplayName("Should find persons with including inactive successfully")
		public void shouldFindPersonsWithInactive() {
			var result = service.findPersons(null, true, 0, 5);
			assertNotNull(result);
			assertEquals(5, result.getData().size());
			assertEquals(0, result.getMetadata().getPage());
			assertEquals(12, result.getMetadata().getTotalElements());
			assertEquals(3, result.getMetadata().getTotalPages());
		}

		@Test
		@DisplayName("Should find persons with filter successfully")
		public void shouldFindPersonsWithFilter() {
			var result = service.findPersons("David", false, 0, 20);
			assertNotNull(result);
			assertEquals(1, result.getData().size());
		}
	}

	@Nested
	@DisplayName("POST /persons")
	class CreatePerson {
		@Test
		@DisplayName("Should create person successfully")
		public void shouldCreatePersonSuccessfully() {
			var request = new CreatePersonRequest()
				.name("New Person")
				.pid("NEW001")
				.email("new.person@email.com")
				.phoneNumber("+1-555-0123")
				.gender(Gender.MALE);
			var result = service.createPerson(request);
			assertNotNull(result);
			assertEquals("New Person", result.getName());
			assertEquals("NEW001", result.getPid());
		}

		@Test
		@DisplayName("Should handle duplicate PID")
		public void shouldHandleDuplicatePid() {
			var request = new CreatePersonRequest()
				.name("Duplicate PID")
				.pid("PID001")
				.email("duplicate@email.com")
				.phoneNumber("+1-555-0124")
				.gender(Gender.FEMALE);
			var exception = assertThrows(CustomException.class, () -> service.createPerson(request));
			assertEquals(HttpStatus.CONFLICT, exception.getStatus());
		}

		@Test
		@DisplayName("Should handle missing required fields")
		public void shouldHandleMissingRequiredFields() {
			var request = new CreatePersonRequest()
				.name("")
				.pid("MISS001")
				.email("");
			var exception = assertThrows(CustomException.class, () -> service.createPerson(request));
			assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
		}

		@Test
		@DisplayName("Should handle invalid email format")
		public void shouldHandleInvalidEmailFormat() {
			var request = new CreatePersonRequest()
				.name("Invalid Email")
				.pid("INV001")
				.email("invalid-email") // Invalid format
				.phoneNumber("+1-555-0125")
				.gender(Gender.MALE);
			var exception = assertThrows(CustomException.class, () -> service.createPerson(request));
			assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
		}
	}

	@Nested
	@DisplayName("GET /persons/{id}")
	class FindPersonById {
		@Test
		@DisplayName("Should find person by ID successfully")
		public void shouldFindPersonByIdSuccessfully() {
			var id = UUID.fromString("660e8400-e29b-41d4-a716-446655440000"); // Alice
			var result = service.findPersonById(id);
			assertNotNull(result);
			assertEquals("Alice Johnson", result.getName());
			assertEquals(Status.ACTIVE, result.getStatus());
		}

		@Test
		@DisplayName("Should find inactive person by ID")
		public void shouldFindInactivePersonById() {
			var id = UUID.fromString("660e8400-e29b-41d4-a716-446655440010"); // Kevin (inactive)
			var result = service.findPersonById(id);
			assertNotNull(result);
			assertEquals("Kevin Lee", result.getName());
			assertEquals(Status.INACTIVE, result.getStatus());
		}

		@Test
		@DisplayName("Should return not found for non-existent ID")
		public void shouldReturnNotFoundForNonExistentId() {
			var id = UUID.randomUUID();
			var exception = assertThrows(CustomException.class, () -> service.findPersonById(id));
			assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
		}

		@Test
		@DisplayName("Should return not found for deleted person")
		public void shouldReturnNotFoundForDeletedPerson() {
			var id = UUID.fromString("660e8400-e29b-41d4-a716-446655440012"); // Megan (deleted)
			var exception = assertThrows(CustomException.class, () -> service.findPersonById(id));
			assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
		}
	}

	@Nested
	@DisplayName("PUT /persons/{id}")
	class UpdatePerson {
		@Test
		@DisplayName("Should update person successfully")
		public void shouldUpdatePersonSuccessfully() {
			var id = UUID.fromString("660e8400-e29b-41d4-a716-446655440001"); // Bob
			var original = service.findPersonById(id);
			var request = new UpdatePersonRequest()
				.name("Updated Bob")
				.pid(original.getPid())
				.email(original.getEmail())
				.phoneNumber(original.getPhoneNumber())
				.gender(original.getGender())
				.version(original.getVersion());
			var result = service.updatePerson(id, request);
			assertNotNull(result);
			assertEquals("Updated Bob", result.getName());
		}

		@Test
		@DisplayName("Should handle update non-existent")
		public void shouldHandleUpdateNonExistent() {
			var id = UUID.randomUUID();
			var request = new UpdatePersonRequest()
				.name("Non-existent")
				.pid("NON001")
				.email("non@email.com")
				.phoneNumber("+1-555-0126")
				.gender(Gender.MALE)
				.version(0L);
			var exception = assertThrows(CustomException.class, () -> service.updatePerson(id, request));
			assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
		}
	}

	@Nested
	@DisplayName("PATCH /persons/{id}")
	class PatchPerson {
		@Test
		@DisplayName("Should patch person successfully")
		public void shouldPatchPersonSuccessfully() {
			var id = UUID.fromString("660e8400-e29b-41d4-a716-446655440003"); // David
			var request = new PatchPersonRequest()
				.name("Patched David");
			var result = service.patchPerson(id, request);
			assertNotNull(result);
			assertEquals("Patched David", result.getName());
		}

		@Test
		@DisplayName("Should handle empty patch")
		public void shouldHandleEmptyPatch() {
			var id = UUID.fromString("660e8400-e29b-41d4-a716-446655440004"); // Emma
			var request = new PatchPersonRequest(); // Empty patch
			var result = service.patchPerson(id, request);
			assertNotNull(result);
			assertEquals("Emma Davis", result.getName()); // Unchanged
		}

		@Test
		@DisplayName("Should handle patch non-existent")
		public void shouldHandlePatchNonExistent() {
			var id = UUID.randomUUID();
			var request = new PatchPersonRequest()
				.name("Patched Non-existent");
			var exception = assertThrows(CustomException.class, () -> service.patchPerson(id, request));
			assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
		}
	}

	@Nested
	@DisplayName("DELETE /persons/{id}")
	class DeletePerson {
		@Test
		@DisplayName("Should delete person successfully")
		public void shouldDeletePersonSuccessfully() {
			var id = UUID.fromString("660e8400-e29b-41d4-a716-446655440005"); // Frank
			service.deletePerson(id);
			// Verify soft delete - should not be found in active queries
			var result = service.findPersons(null, false, 0, 20);
			assertEquals(9, result.getData().size()); // One less active person
		}

		@Test
		@DisplayName("Should handle delete non-existent")
		public void shouldHandleDeleteNonExistent() {
			var id = UUID.randomUUID();
			var exception = assertThrows(CustomException.class, () -> service.deletePerson(id));
			assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
		}

		@Test
		@DisplayName("Should handle delete already deleted")
		public void shouldHandleDeleteAlreadyDeleted() {
			var id = UUID.fromString("660e8400-e29b-41d4-a716-446655440013"); // Noah (status: deleted)
			var exception = assertThrows(CustomException.class, () -> service.deletePerson(id));
			assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
		}
	}

	@Nested
	@DisplayName("PUT /persons/{id}/activate")
	class ActivatePerson {
		@Test
		@DisplayName("Should activate person successfully")
		public void shouldActivatePersonSuccessfully() {
			var id = UUID.fromString("660e8400-e29b-41d4-a716-446655440011"); // Laura (inactive)
			var result = service.activatePerson(id);
			assertNotNull(result);
			assertEquals(Status.ACTIVE, result.getStatus());
		}

		@Test
		@DisplayName("Should handle activate already active")
		public void shouldHandleActivateAlreadyActive() {
			var id = UUID.fromString("660e8400-e29b-41d4-a716-446655440006"); // Grace (active)
			var exception = assertThrows(CustomException.class, () -> service.activatePerson(id));
			assertEquals(HttpStatus.CONFLICT, exception.getStatus());
		}

		@Test
		@DisplayName("Should handle activate non-existent")
		public void shouldHandleActivateNonExistent() {
			var id = UUID.randomUUID();
			var exception = assertThrows(CustomException.class, () -> service.activatePerson(id));
			assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
		}
	}

	@Nested
	@DisplayName("PUT /persons/{id}/deactivate")
	class DeactivatePerson {
		@Test
		@DisplayName("Should deactivate person successfully")
		public void shouldDeactivatePersonSuccessfully() {
			var id = UUID.fromString("660e8400-e29b-41d4-a716-446655440007"); // Henry (active)
			var result = service.deactivatePerson(id);
			assertNotNull(result);
			assertEquals(Status.INACTIVE, result.getStatus());
		}

		@Test
		@DisplayName("Should handle deactivate already inactive")
		public void shouldHandleDeactivateAlreadyInactive() {
			var id = UUID.fromString("660e8400-e29b-41d4-a716-446655440010"); // Kevin (inactive)
			var exception = assertThrows(CustomException.class, () -> service.deactivatePerson(id));
			assertEquals(HttpStatus.CONFLICT, exception.getStatus());
		}

		@Test
		@DisplayName("Should handle deactivate non-existent")
		public void shouldHandleDeactivateNonExistent() {
			var id = UUID.randomUUID();
			var exception = assertThrows(CustomException.class, () -> service.deactivatePerson(id));
			assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
		}
	}

	@Nested
	@DisplayName("POST /persons/{id}/restore")
	class RestorePerson {
		@Test
		@DisplayName("Should restore person successfully")
		public void shouldRestorePersonSuccessfully() {
			var id = UUID.fromString("660e8400-e29b-41d4-a716-446655440012"); // Megan (deleted)
			var result = service.restorePerson(id);
			assertNotNull(result);
			assertEquals(Status.ACTIVE, result.getStatus());
		}

		@Test
		@DisplayName("Should handle restore non-deleted")
		public void shouldHandleRestoreNonDeleted() {
			var id = UUID.fromString("660e8400-e29b-41d4-a716-446655440008"); // Ivy (active)
			var exception = assertThrows(CustomException.class, () -> service.restorePerson(id));
			assertEquals(HttpStatus.CONFLICT, exception.getStatus());
		}

		@Test
		@DisplayName("Should handle restore non-existent")
		public void shouldHandleRestoreNonExistent() {
			var id = UUID.randomUUID();
			var exception = assertThrows(CustomException.class, () -> service.restorePerson(id));
			assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
		}
	}

	@Nested
	@DisplayName("GET /persons/{id}/documents")
	class FindPersonDocuments {
		@Test
		@DisplayName("Should find person documents successfully")
		public void shouldFindPersonDocumentsSuccessfully() {
			var id = UUID.fromString("660e8400-e29b-41d4-a716-446655440000"); // Alice (has 1 active doc)
			var result = service.findPersonDocuments(id, null, null, null, 0, 20);
			assertNotNull(result);
			assertEquals(1, result.getData().size());
		}

		@Test
		@DisplayName("Should return empty for person without documents")
		public void shouldReturnEmptyForPersonWithoutDocuments() {
			var id = UUID.fromString("660e8400-e29b-41d4-a716-446655440009"); // Jack Anderson (no docs in test data)
			var result = service.findPersonDocuments(id, null, null, null, 0, 20);
			assertNotNull(result);
			assertEquals(0, result.getData().size());
		}

		@Test
		@DisplayName("Should filter documents by type")
		public void shouldFilterDocumentsByType() {
			var id = UUID.fromString("660e8400-e29b-41d4-a716-446655440000"); // Alice
			var typeId = UUID.fromString("550e8400-e29b-41d4-a716-446655440000"); // ID card type
			var result = service.findPersonDocuments(id, typeId, null, null, 0, 20);
			assertNotNull(result);
			assertEquals(1, result.getData().size());
		}

		@Test
		@DisplayName("Should filter documents by date range")
		public void shouldFilterDocumentsByDateRange() {
			var id = UUID.fromString("660e8400-e29b-41d4-a716-446655440000"); // Alice
			var fromDate = java.time.LocalDate.of(2024, 1, 1);
			var toDate = java.time.LocalDate.of(2024, 12, 31);
			var result = service.findPersonDocuments(id, null, DateUtils.toInstant(fromDate), DateUtils.toInstant(toDate), 0, 20);
			assertNotNull(result);
			assertEquals(1, result.getData().size());
		}

		@Test
		@DisplayName("Should handle pagination")
		public void shouldHandlePagination() {
			var id = UUID.fromString("660e8400-e29b-41d4-a716-446655440000"); // Alice
			var result = service.findPersonDocuments(id, null, null, null, 0, 1);
			assertNotNull(result);
			assertEquals(1, result.getData().size());
			assertEquals(0, result.getMetadata().getPage());
		}

		@Test
		@DisplayName("Should handle documents for non-existent person")
		public void shouldHandleDocumentsForNonExistentPerson() {
			var id = UUID.randomUUID();
			var exception = assertThrows(CustomException.class, () -> service.findPersonDocuments(id, null, null, null, 0, 20));
			assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
		}
	}
}
