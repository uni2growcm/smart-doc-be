package org.openhospital.smartdoc.annotations;

import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlGroup;

import java.lang.annotation.*;


@Documented
@Inherited
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@SqlGroup({
	@Sql(
		scripts = {
			"/sql/persons.sql",
			"/sql/document-types.sql",
			"/sql/documents.sql"
		},
		executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
	),
	@Sql(
		scripts = "/sql/cleanup.sql",
		executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD
	)
})
public @interface WithTestDatabase {
}