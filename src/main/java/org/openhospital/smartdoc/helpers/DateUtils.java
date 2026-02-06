package org.openhospital.smartdoc.helpers;

import java.time.Instant;
import java.time.LocalDate;

public abstract class DateUtils {
	public static Instant toInstant(LocalDate date) {
		return date != null ? date.atStartOfDay().toInstant(java.time.ZoneOffset.UTC) : null;
	}
}
