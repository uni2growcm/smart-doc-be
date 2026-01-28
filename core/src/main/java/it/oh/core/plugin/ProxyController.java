package it.oh.core.plugin;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Collections;

@RestController
public class ProxyController {

    private final RoutesConfig config;
    private final WebClient webClient = WebClient.create();

    public ProxyController(RoutesConfig config) {
        this.config = config;
    }

    @RequestMapping("/api/**")
    public Mono<ResponseEntity<byte[]>> proxy(HttpServletRequest request) {

        String incomingPath = request.getRequestURI();

        RouteDefinition route = config.getRoutes().stream()
                                      .filter(r -> incomingPath.startsWith(r.getFrom()))
                                      .findFirst()
                                      .orElseThrow(() ->
                                                       new RuntimeException("No route for " + incomingPath));

        String targetPath =
            incomingPath.replace(route.getFrom(), route.getTo());

        return webClient
            .method(HttpMethod.valueOf(request.getMethod()))
            .uri(targetPath)
            .headers(h -> copyHeaders(request, h))
            .retrieve()
            .toEntity(byte[].class);
    }

    private void copyHeaders(HttpServletRequest req, HttpHeaders headers) {
        Collections.list(req.getHeaderNames())
                   .forEach(h ->
                                headers.put(h, Collections.list(req.getHeaders(h)))
                           );
    }
}
