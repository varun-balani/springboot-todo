package com.varunbalani.todoapp.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		http.authorizeHttpRequests((authorize) -> {
			try {
				authorize.requestMatchers("/styles/**").permitAll().anyRequest().authenticated().and()
						.formLogin(login -> login.loginPage("/login").permitAll().defaultSuccessUrl("/", true))
						.logout(logout -> logout.logoutUrl("/logout"));
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
		return http.build();
	}
}