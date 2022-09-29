package com.lucasprojects.dscatalog.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

	@Bean
	public SecurityFilterChain configure(HttpSecurity httpSecurity) throws Exception {
		return httpSecurity
				.headers(headers -> headers.frameOptions().disable())			
				.csrf(csrf -> csrf.disable())
				.authorizeHttpRequests(auth -> {
					auth.antMatchers("/**").permitAll();
				})
				.build();
	}
}