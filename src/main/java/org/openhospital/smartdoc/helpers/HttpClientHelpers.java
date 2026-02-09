package org.openhospital.smartdoc.helpers;

import org.openhospital.smartdoc.exceptions.CustomException;
import org.springframework.http.*;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;
import tools.jackson.databind.ObjectMapper;

import java.util.Map;

public final class HttpClientHelpers {

	private HttpClientHelpers() {
	}

	/**
	 * Builds a configured RestClient with base URL and dynamic headers.
	 * Includes default error handling that throws CustomException.
	 */
	public static RestClient buildRestClient(String baseUrl, HttpHeaders headers) {

		if (baseUrl == null || baseUrl.isBlank()) {
			baseUrl = "";
		}

		return RestClient.builder().baseUrl(baseUrl).defaultHeader(HttpHeaders.ACCEPT, "application/json").requestInterceptor((request, body, execution) -> {
			if (headers != null) {
				request.getHeaders().putAll(headers);
			}
			return execution.execute(request, body);
		}).defaultStatusHandler(HttpStatusCode::isError, (req, res) -> {
			HttpStatusCode statusCode = res.getStatusCode();
			String message = "Something went wrong";
			String debugMessage = null;

			var mapper = new ObjectMapper();

			try {
				Map<String, Object> body = mapper.readValue(res.getBody(), Map.class);
				if (body != null) {
					Object msg = body.get("title");
					if (msg instanceof String) message = (String) msg;
					
					Object detail = body.get("detail");
					if (detail instanceof String) debugMessage = (String) detail;
				}
			} catch (Exception ignored) {
			}

			throw new CustomException(HttpStatus.valueOf(statusCode.value()), message, message, debugMessage);
		}).build();
	}

	public static HttpServiceProxyFactory buildServiceProxyFactory(String baseUrl, HttpHeaders headers) {
		return HttpServiceProxyFactory.builderFor(RestClientAdapter.create(buildRestClient(baseUrl, headers))).build();
	}

	public static HttpServiceProxyFactory buildServiceProxyFactory(String baseUrl) {
		return buildServiceProxyFactory(baseUrl, null);
	}

	public static HttpServiceProxyFactory buildServiceProxyFactory() {
		return buildServiceProxyFactory("", null);
	}

	// Convenience overloads
	public static RestClient buildRestClient(String baseUrl) {
		return buildRestClient(baseUrl, null);
	}

	public static RestClient buildRestClient() {
		return buildRestClient("", null);
	}
}