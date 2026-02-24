package org.openhospital.smartdoc.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

	@Value("${spring.web.cors.allowed-origins}")
	private String allowedOrigins;

	@Value("${spring.web.cors.allowed-methods}")
	private String allowedMethods;

	@Value("${spring.web.cors.allowed-headers}")
	private String allowedHeaders;

	@Value("${spring.web.cors.allow-credentials}")
	private boolean allowCredentials;

	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http
			.csrf(AbstractHttpConfigurer::disable)
			.sessionManagement(session ->
					session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
			                  )
			.authorizeHttpRequests(auth -> auth
					.anyRequest().permitAll()
			                      )
			.cors(Customizer.withDefaults())
		;

		return http.build();
	}

	@Bean
	public CorsConfigurationSource corsConfigurationSource() {

		CorsConfiguration configuration = new CorsConfiguration();
		configuration.setAllowedOrigins(Arrays.stream(allowedOrigins.split(",")).toList());
		configuration.setAllowedMethods(Arrays.stream(allowedMethods.split(",")).toList());
		configuration.setAllowedHeaders(Arrays.stream(allowedHeaders.split(",")).toList());
		configuration.setAllowCredentials(allowCredentials);

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", configuration);

		return source;
	}
}
