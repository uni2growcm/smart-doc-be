package org.openhospital.smartdoc.modules.persons.rest;

import org.junit.jupiter.api.*;
import org.openhospital.smartdoc.annotations.WithTestDatabase;
import org.openhospital.smartdoc.helpers.TestHelpers;
import org.openhospital.smartdoc.modules.persons.port.IPersonService;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

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
}
