package org.openhospital.smartdoc.helpers;


import org.springframework.http.HttpHeaders;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

public abstract class TestHelpers {
	public static String BASE_URL = "http://localhost:8042/api";

	public static HttpServiceProxyFactory buildFactory(HttpHeaders headers) {
		return HttpClientHelpers.buildServiceProxyFactory(BASE_URL, headers);
	}

	public static <T> T createService(HttpHeaders headers, Class<T> clazz) {
		return HttpClientHelpers.buildServiceProxyFactory(BASE_URL, headers).createClient(clazz);
	}

	public static <T> T createService(Class<T> clazz) {
		return createService(null, clazz);
	}
}
