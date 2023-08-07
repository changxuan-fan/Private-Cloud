package com.ehz.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig {
  /*	@Bean
  public PasswordEncoder passwordEncoder() {
  	return new BCryptPasswordEncoder();
  }*/

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http.csrf(csrf -> csrf.disable())
        .authorizeHttpRequests(
            (requests) ->
                requests
                    .requestMatchers("/ehz/admin", "/ehz/admin/**")
                    .hasAuthority("ADMIN")
                    .anyRequest()
                    .authenticated())
        .formLogin((form) -> form.loginPage("/ehz/login").permitAll())
        .logout((logout) -> logout.permitAll());

    return http.build();
  }
}
