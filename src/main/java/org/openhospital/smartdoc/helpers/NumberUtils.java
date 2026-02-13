package org.openhospital.smartdoc.helpers;

public abstract class NumberUtils {
	public static String normalizeTo6Digits(int number) {
		if (number < 1 || number > 999_999) {
			throw new IllegalArgumentException("Number must be between 1 and 999999");
		}
		return String.format("%06d", number);
	}

	public static String toSixDigitPath(int number) {
		if (number < 1 || number > 999_999) {
			throw new IllegalArgumentException("Number must be between 1 and 999999");
		}

		String padded = String.format("%06d", number);

		return "%s/%s/%s".formatted(padded.substring(0, 2), padded.substring(2, 4), padded.substring(4, 6));
	}
}
