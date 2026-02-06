package org.openhospital.smartdoc.exceptions;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

/**
 * A custom exception class that extends {@link Exception} to provide additional information such as an HTTP status
 * code, error code, and debug message.
 *
 * @author Steve Tsala
 */
@Slf4j
@Getter
@Setter
public class CustomException extends RuntimeException {

	/**
	 * The HTTP status code associated with the exception.
	 */
	private final HttpStatus status;
	/**
	 * A unique error code representing the i18n key of the exception message.
	 */
	private String code;

	private Object[] args;
	/**
	 * A detailed debug message providing more context for troubleshooting.
	 */
	private String debugMessage;

	/**
	 * The date and time at which this error response was generated.
	 */
	private LocalDateTime timestamp;

	/**
	 * Creates a new {@code CustomException} with the specified cause and a default HTTP status code of
	 * {@link HttpStatus#INTERNAL_SERVER_ERROR}.
	 *
	 * @param cause the cause of the exception
	 */
	public CustomException(Throwable cause) {
		super(cause);
		this.status = HttpStatus.INTERNAL_SERVER_ERROR;
	}

	/**
	 * Creates a new {@code CustomException} with the specified message and cause, and a default HTTP status code of
	 * {@link HttpStatus#INTERNAL_SERVER_ERROR}.
	 *
	 * @param message the detail message
	 * @param cause   the cause of the exception
	 */
	public CustomException(String message, Throwable cause) {
		super(message, cause);
		this.status = HttpStatus.INTERNAL_SERVER_ERROR;
	}

	/**
	 * Creates a new {@code CustomException} with the specified HTTP status code and message.
	 *
	 * @param status  the HTTP status code
	 * @param message the detail message
	 */
	public CustomException(HttpStatus status, String message) {
		super(message);
		this.status = status;
	}

	/**
	 * Creates a new {@code CustomException} with the specified HTTP status code and message.
	 *
	 * @param status  the HTTP status code
	 * @param message the detail message
	 * @param args    the translation params
	 */
	public CustomException(HttpStatus status, String message, Object[] args) {
		super(message);
		this.status = status;
		this.code = message;
		this.args = args;
	}

	/**
	 * Creates a new {@code CustomException} with the specified HTTP status code, message, and error code.
	 *
	 * @param status  the HTTP status code
	 * @param message the detail message
	 * @param code    the error code
	 */
	public CustomException(HttpStatus status, String message, String code) {
		super(message);
		this.code = code;
		this.status = status;
	}

	/**
	 * Creates a new {@code CustomException} with the specified HTTP status code, message, and error code.
	 *
	 * @param status  the HTTP status code
	 * @param message the detail message
	 * @param code    the error code
	 */
	public CustomException(HttpStatus status, String message, String code, Object translationParams) {
		super(message);
		this.code = code;
		this.status = status;
	}

	/**
	 * Creates a new {@code CustomException} with the specified HTTP status code, message, error code, and debug
	 * message.
	 *
	 * @param status       the HTTP status code
	 * @param message      the detail message
	 * @param code         the error code
	 * @param debugMessage the debug message
	 */
	public CustomException(HttpStatus status, String message, String code, String debugMessage) {
		super(message);
		this.code = code;
		this.status = status;
		this.debugMessage = debugMessage;
	}

	// Static factory methods for common exception scenarios

	/**
	 * Creates a new {@code CustomException} representing a bad request with the specified message.
	 *
	 * @param code the detail message key
	 * @return the created {@code CustomException} with a status code of {@link HttpStatus#BAD_REQUEST}
	 */
	public static CustomException badRequest(String code) {
		return new CustomException(HttpStatus.BAD_REQUEST, code, code);
	}

	/**
	 * Creates a new {@code CustomException} representing a not found request with the specified message.
	 *
	 * @param code the detail message key
	 * @return the created {@code CustomException} with a status code of {@link HttpStatus#NOT_FOUND}
	 */
	public static CustomException notFound(String code) {
		return new CustomException(HttpStatus.NOT_FOUND, code, code);
	}

	/**
	 * Creates a new {@code CustomException} representing an unauthorized request with the specified message.
	 *
	 * @param code the detail message key
	 * @return the created {@code CustomException} with a status code of {@link HttpStatus#UNAUTHORIZED}
	 */
	public static CustomException unauthorized(String code) {
		return new CustomException(HttpStatus.UNAUTHORIZED, code, code);
	}

	/**
	 * Creates a new {@code CustomException} representing a forbidden request with the specified message.
	 *
	 * @param code the detail message key
	 * @return the created {@code CustomException} with a status code of {@link HttpStatus#FORBIDDEN}
	 */
	public static CustomException forbidden(String code) {
		return new CustomException(HttpStatus.FORBIDDEN, code, code);
	}

	/**
	 * Creates a new {@code CustomException} representing an internal server error request with the specified message.
	 *
	 * @param code the detail message key
	 * @return the created {@code CustomException} with a status code of {@link HttpStatus#INTERNAL_SERVER_ERROR}
	 */
	public static CustomException preconditionFailed(String code) {
		return new CustomException(HttpStatus.PRECONDITION_FAILED, code, code);
	}

	/**
	 * Creates a new {@code CustomException} representing a conflict request with the specified message.
	 *
	 * @param code the detail message key
	 * @return the created {@code CustomException} with a status code of {@link HttpStatus#CONFLICT}
	 */
	public static CustomException conflict(String code) {
		return new CustomException(HttpStatus.CONFLICT, code, code);
	}

	/**
	 * Creates a new {@code CustomException} representing an internal server error request with the specified message.
	 *
	 * @param code the detail message key
	 * @return the created {@code CustomException} with a status code of {@link HttpStatus#INTERNAL_SERVER_ERROR}
	 */
	public static CustomException internal(String code) {
		return new CustomException(HttpStatus.INTERNAL_SERVER_ERROR, code, code);
	}

	/**
	 * Creates a new {@code CustomException} representing a bad request with the specified message.
	 *
	 * @param code the detail message key
	 * @param args translation params
	 * @return the created {@code CustomException} with a status code of {@link HttpStatus#BAD_REQUEST}
	 */
	public static CustomException badRequest(String code, Object[] args) {
		return new CustomException(HttpStatus.BAD_REQUEST, code, args);
	}

	/**
	 * Creates a new {@code CustomException} representing a not found request with the specified message.
	 *
	 * @param code the detail message key
	 * @param args translation params
	 * @return the created {@code CustomException} with a status code of {@link HttpStatus#NOT_FOUND}
	 */
	public static CustomException notFound(String code, Object[] args) {
		return new CustomException(HttpStatus.NOT_FOUND, code, args);
	}

	/**
	 * Creates a new {@code CustomException} representing an unauthorized request with the specified message.
	 *
	 * @param code the detail message key
	 * @param args translation params
	 * @return the created {@code CustomException} with a status code of {@link HttpStatus#UNAUTHORIZED}
	 */
	public static CustomException unauthorized(String code, Object[] args) {
		return new CustomException(HttpStatus.UNAUTHORIZED, code, args);
	}

	/**
	 * Creates a new {@code CustomException} representing a forbidden request with the specified message.
	 *
	 * @param code the detail message key
	 * @param args translation params
	 * @return the created {@code CustomException} with a status code of {@link HttpStatus#FORBIDDEN}
	 */
	public static CustomException forbidden(String code, Object[] args) {
		return new CustomException(HttpStatus.FORBIDDEN, code, args);
	}

	/**
	 * Creates a new {@code CustomException} representing an internal server error request with the specified message.
	 *
	 * @param code the detail message key
	 * @param args translation params
	 * @return the created {@code CustomException} with a status code of {@link HttpStatus#INTERNAL_SERVER_ERROR}
	 */
	public static CustomException preconditionFailed(String code, Object[] args) {
		return new CustomException(HttpStatus.PRECONDITION_FAILED, code, args);
	}

	/**
	 * Creates a new {@code CustomException} representing a conflict request with the specified message.
	 *
	 * @param code the detail message key
	 * @param args translation params
	 * @return the created {@code CustomException} with a status code of {@link HttpStatus#CONFLICT}
	 */
	public static CustomException conflict(String code, Object[] args) {
		return new CustomException(HttpStatus.CONFLICT, code, args);
	}

	/**
	 * Creates a new {@code CustomException} representing an internal server error request with the specified message.
	 *
	 * @param code the detail message key
	 * @param args translation params
	 * @return the created {@code CustomException} with a status code of {@link HttpStatus#INTERNAL_SERVER_ERROR}
	 */
	public static CustomException internal(String code, Object[] args) {
		return new CustomException(HttpStatus.INTERNAL_SERVER_ERROR, code, args);
	}
}

