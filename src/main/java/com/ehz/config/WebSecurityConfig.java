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
                    .requestMatchers("/", "/home")
                    .permitAll()
                    .requestMatchers("admin", "/admin/**")
                    .hasRole("ADMIN")
                    .anyRequest()
                    .authenticated())
        .formLogin((form) -> form.loginPage("/login").permitAll())
        .logout((logout) -> logout.permitAll());

    return http.build();
  }

  /*	@Bean
  public UserDetailsManager users(DataSource dataSource) {
  	UserDetails user = User.withDefaultPasswordEncoder()
  			.username("user")
  			.password("password")
  			.roles("USER")
  			.build();
  	JdbcUserDetailsManager users = new JdbcUserDetailsManager(dataSource);
  	users.createUser(user);
  	return users;
  }*/

  /*	@Bean
  public UserDetailsManager users(DataSource dataSource) {
  	UserDetails user = User.builder()
  			.username("user")
  			.password("{bcrypt}$2a$10$GRLdNijSQMUvl/au9ofL.eDwmoohzzS7.rmNSJZ.0FxO/BTk76klW")
  			.build();
  	UserDetails admin = User.builder()
  			.username("admin")
  			.password("{bcrypt}$2a$10$GRLdNijSQMUvl/au9ofL.eDwmoohzzS7.rmNSJZ.0FxO/BTk76klW")
  			.build();
  	JdbcUserDetailsManager users = new JdbcUserDetailsManager(dataSource);
  	users.createUser(user);
  	users.createUser(admin);
  	return users;
  }*/
  /*
  @Bean
  public UserDetailsService userDetailsService() {
  	UserDetails user =
  			User.withDefaultPasswordEncoder()
  					.username("user")
  					.password("password")
  					.roles("USER")
  					.build();

  	return new InMemoryUserDetailsManager(user);
  }*/

}
