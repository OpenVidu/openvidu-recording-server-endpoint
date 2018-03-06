package io.openvidu.recording.server.endpoint;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.configurers.ExpressionUrlAuthorizationConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import io.openvidu.recording.server.endpoint.PropertiesConfig;

@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter {

	@Autowired
	PropertiesConfig config;

	@Override
	protected void configure(HttpSecurity http) throws Exception {

		final Logger log = LoggerFactory.getLogger(SecurityConfig.class);

		Path path = Paths.get(config.getRecordingsPath());

		if (!Files.exists(path)) {
			log.info("Folder '{}' does not exist. Creating folder", path);
			path = Files.createDirectories(path);
			log.info("Created folder '{}'", path.toAbsolutePath().toString());
		}

		ExpressionUrlAuthorizationConfigurer<HttpSecurity>.ExpressionInterceptUrlRegistry conf = http.csrf().disable()
				.cors().and().authorizeRequests().antMatchers(HttpMethod.POST, "/recording").authenticated()
				.antMatchers(HttpMethod.GET, "/recording").authenticated();

		/*
		 * if (config.getOpenViduRecordingFreeAccess()) { conf =
		 * conf.antMatchers("/recordings/*").permitAll(); } else { conf =
		 * conf.antMatchers("/recordings/*").authenticated(); }
		 */

		conf.and().sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and().httpBasic();
	}

	@Autowired
	public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
		auth.inMemoryAuthentication().withUser("OPENVIDUAPP").password(config.getPassword()).roles("USER");
		;
	}

	@Bean
	public WebMvcConfigurer corsConfigurer() {
		return new WebMvcConfigurerAdapter() {
			@Override
			public void addCorsMappings(CorsRegistry registry) {
				registry.addMapping("/recording/**").allowedOrigins("*");
			}
		};
	}

}