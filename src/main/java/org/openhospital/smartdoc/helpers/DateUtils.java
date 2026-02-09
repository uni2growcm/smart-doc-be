package org.openhospital.smartdoc.helpers;

import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public abstract class DateUtils {
	public static Instant toInstant(LocalDate date) {
		return date != null ? date.atStartOfDay().toInstant(java.time.ZoneOffset.UTC) : null;
	}

	public static String format(LocalDate date, DateTimeFormatter formatter) {
		return date != null ? date.format(formatter) : "";
	}
}
