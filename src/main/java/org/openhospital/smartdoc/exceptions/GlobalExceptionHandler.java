package org.openhospital.smartdoc.exceptions;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.openhospital.smartdoc.openapi.Problem;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.dao.*;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.net.URI;
import java.util.UUID;

/**
 * Global exception handler for the application. Catches common and custom exceptions and returns structured error
 * responses.
 *
 * @author Steve Tsala
 */
@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE)
@ControllerAdvice
public class GlobalExceptionHandler {

	private final MessageSource messageSource;

	/**
	 * Constructor with injected {@link MessageSource} for error message localization.
	 *
	 * @param messageSource the source used to resolve localized messages
	 */
	public GlobalExceptionHandler(MessageSource messageSource) {
		this.messageSource = messageSource;
	}

	private String getTypeUri(int status) {
		return switch (status) {
			case 400 -> "https://tools.ietf.org/html/rfc7231#section-6.5.1";
			case 401 -> "https://tools.ietf.org/html/rfc7231#section-6.5.3";
			case 403 -> "https://tools.ietf.org/html/rfc7231#section-6.5.4";
			case 404 -> "https://tools.ietf.org/html/rfc7231#section-6.5.4";
			case 409 -> "https://tools.ietf.org/html/rfc7231#section-6.5.8";
			case 412 -> "https://tools.ietf.org/html/rfc7231#section-6.5.12";
			case 422 -> "https://tools.ietf.org/html/rfc4918#section-11.2";
			case 500 -> "https://tools.ietf.org/html/rfc7231#section-6.6.1";
			default -> "about:blank";
		};
	}

	private ResponseEntity<Problem> buildResponse(CustomException exception) {
		exception.printStackTrace();
		Problem problem = new Problem().type(URI.create(getTypeUri(exception.getStatus().value()))).title(messageSource.getMessage(exception.getCode(), exception.getArgs(), LocaleContextHolder.getLocale())).detail(exception.getDebugMessage()).status(exception.getStatus().value()).instance(URI.create(org.springframework.web.servlet.support.ServletUriComponentsBuilder.fromCurrentRequest().build().toUriString())).traceId(UUID.randomUUID());

		return ResponseEntity.status(exception.getStatus().value()).contentType(MediaType.APPLICATION_JSON).body(problem);
	}


	/**
	 * Handles custom exceptions of type {@link CustomException}.
	 *
	 * @param exception the custom exception thrown by the application
	 * @return a structured error response with appropriate status and localized message
	 */
	@ExceptionHandler({CustomException.class})
	ResponseEntity<Problem> handle(CustomException exception) {
		String messageKey = exception.getCode() != null ? exception.getCode() : "errors.common.internal";
		exception.setCode(messageKey);
		exception.setDebugMessage(messageSource.getMessage(messageKey, exception.getArgs(), LocaleContextHolder.getLocale()));
		return buildResponse(exception);
	}

	/**
	 * Handles access denied exceptions dues to insufficient permissions.
	 *
	 * @param exception the exception thrown when permission is denied
	 * @return a 403 FORBIDDEN response with a localized error message
	 */
	@ExceptionHandler({PermissionDeniedDataAccessException.class})
	ResponseEntity<Problem> handle(PermissionDeniedDataAccessException exception) {
		CustomException customEx = CustomException.forbidden("auth.errors.permission-denied");
		customEx.setDebugMessage(exception.getLocalizedMessage());
		return buildResponse(customEx);
	}

	/**
	 * Handles {@link AccessDeniedException} thrown when an authenticated user attempts to access a resource they are
	 * not authorized to access.
	 *
	 * <p>This method is annotated with {@code @ExceptionHandler} to intercept {@code AccessDeniedException}
	 * instances and return an HTTP 403 (Forbidden) response with a JSON body detailing the error.</p>
	 *
	 * @param exception the {@code AccessDeniedException} instance caught
	 * @return a {@code ResponseEntity} containing the error details with HTTP status 403
	 */
	@ExceptionHandler({AccessDeniedException.class})
	ResponseEntity<Problem> handle(AccessDeniedException exception) {
		CustomException customEx = CustomException.forbidden("auth.errors.access-denied");
		customEx.setDebugMessage(exception.getLocalizedMessage());
		return buildResponse(customEx);
	}

	/**
	 * Handles optimistic locking failures (typically version conflicts).
	 *
	 * @param exception the exception thrown when an optimistic locking error occurs
	 * @return a 412 PRECONDITION_FAILED response with a localized message
	 */
	@ExceptionHandler({OptimisticLockingFailureException.class})
	ResponseEntity<Problem> handle(OptimisticLockingFailureException exception) {
		CustomException customEx = CustomException.preconditionFailed("errors.dao.locking-failed");
		customEx.setDebugMessage(exception.getLocalizedMessage());
		return buildResponse(customEx);
	}

	/**
	 * Handles data integrity violations such as constraint violations or duplicate keys.
	 *
	 * @param exception the exception thrown when a data integrity issue occurs
	 * @return a 400 BAD_REQUEST response with a localized message
	 */
	@ExceptionHandler({DataIntegrityViolationException.class})
	ResponseEntity<Problem> handle(DataIntegrityViolationException exception) {
		log.debug("Exception {} occurred", exception.getClass(), exception);

		String message = exception.getLocalizedMessage().toLowerCase();
		String code;

		if (message.contains("uk_persons_pid")) {
			code = "persons.errors.pid-already-exists";
		} else if (message.contains("uk_document_types_code")) {
			code = "documents.errors.type-code-already-exists";
		} else {
			code = exception instanceof DuplicateKeyException ? "errors.dao.duplicate-key" : "errors.dao.data-integrity-violation";
		}

		CustomException customEx = CustomException.badRequest(code);
		customEx.setDebugMessage(exception.getLocalizedMessage());
		return buildResponse(customEx);
	}

	@ExceptionHandler({ConstraintViolationException.class})
	ResponseEntity<Problem> handle(ConstraintViolationException exception) {
		log.debug("Exception {} occurred", exception.getClass(), exception);

		CustomException customEx = CustomException.badRequest("errors.validation.constraint-violation");
		customEx.setDebugMessage(exception.getLocalizedMessage());
		return buildResponse(customEx);
	}

	@ExceptionHandler({MethodArgumentNotValidException.class})
	ResponseEntity<Problem> handle(MethodArgumentNotValidException exception) {
		CustomException customEx = CustomException.badRequest("errors.validation.constraint-violation");
		customEx.setDebugMessage(exception.getLocalizedMessage());
		return buildResponse(customEx);
	}

	/**
	 * Handles any other unhandled exceptions (fallback).
	 *
	 * @param exception the unhandled exception
	 * @return a 500 INTERNAL_SERVER_ERROR response with debug information
	 */
	@ExceptionHandler({Exception.class})
	ResponseEntity<Problem> handleException(Exception exception) {
		log.debug("Exception {} occurred", exception.getClass(), exception);

		CustomException customEx = CustomException.internal("errors.common.internal");
		customEx.setDebugMessage(exception.getLocalizedMessage());
		return buildResponse(customEx);
	}
}
