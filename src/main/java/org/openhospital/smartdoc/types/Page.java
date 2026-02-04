package org.openhospital.smartdoc.types;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.openhospital.smartdoc.openapi.PageInfoDTO;

import java.util.List;
import java.util.function.Function;

@Data
@AllArgsConstructor
public class Page<T> {
	List<T> data;
	PageInfoDTO metadata;

	public static <T, R> Page<R> from(org.springframework.data.domain.Page<T> page, Function<T, R> mapper) {
		return new Page<>(
			page.getContent().stream().map(mapper).toList(),
			new PageInfoDTO()
				.page(page.getNumber())
				.size(page.getSize())
				.totalElements((int) page.getTotalElements())
				.totalPages(page.getTotalPages())
		);
	}
}
