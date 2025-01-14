package com.yj.security;

import java.util.Collections;

import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yj.repository.MemberRepository;
import com.yj.security.filter.CustomeLogoutFilter;
import com.yj.security.filter.JWTFilter;
import com.yj.security.filter.LoginFilter;
import com.yj.security.jwt.JWTUtil;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
		
		private final AuthenticationConfiguration authenticationConfiguration;
		//JWTUtil 주입
		private final JWTUtil jwtUtil;
		//액세스 토큰 재발급 서비스 
		private final RefreshService refreshService;
		private final ObjectMapper objectMapper;
		private final MemberRepository memberAccountRepository;
		
		
		
   
		@Bean
	    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
	
	        return configuration.getAuthenticationManager();
	    }
	
	    //암호화	
	    @Bean
	    public BCryptPasswordEncoder bCryptPasswordEncoder() {
	
	        return new BCryptPasswordEncoder();
	    }
	    
	    
	    //필터 및 시큐리티 설정 
	    @Bean
	    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    
    	
    	//분리 작업시 cors 관련 설정 
    	http
        .cors((corsCustomizer -> corsCustomizer.configurationSource(new CorsConfigurationSource() {

            @Override
            public CorsConfiguration getCorsConfiguration(HttpServletRequest request) {

                CorsConfiguration configuration = new CorsConfiguration();

                configuration.setAllowedOrigins(Collections.singletonList("http://localhost:3000"));//3000번 포트 허용 
                configuration.setAllowedMethods(Collections.singletonList("*"));//모든메소드 허용 
                configuration.setAllowCredentials(true);
                configuration.setAllowedHeaders(Collections.singletonList("*"));
                configuration.setMaxAge(3600L);

									configuration.setExposedHeaders(Collections.singletonList("Authorization"));

                return configuration;
            }
        })));
    	
    	http
        .csrf((auth) -> auth.disable()); //jwt 사용으로 인한 disable 
    	


		http
	    .formLogin((auth) -> auth.disable()); //jwt사용으로 인한 기본로그인폼  x
		
		http
	    .httpBasic((auth) -> auth.disable());
		
		http
		.authorizeHttpRequests((auth) -> auth
	    .requestMatchers("/api/v1/member/**","/api/v1/captcha").permitAll() //로그인 ,회원가입 , 토큰 재발급,이메일인증 api는 권한 필요없음 
		                .anyRequest().authenticated());//나머지는 인증이 필요함 			
				
		//JWTFilter 등록 = > 로그인 필터 전에 수행 		
		http
	    .addFilterBefore(new JWTFilter(jwtUtil,objectMapper), LoginFilter.class);
		
		
		// 로그인필터를  UsernamePasswordAuthenticationFilter 위치에 
		http
		.addFilterAt(new LoginFilter(authenticationManager(authenticationConfiguration), jwtUtil,refreshService,objectMapper), UsernamePasswordAuthenticationFilter.class);
		
		//커스텀한 로그아웃 필터를 등록 =>기존 필터위치에 
		http
		.addFilterBefore(new CustomeLogoutFilter(jwtUtil, refreshService,objectMapper), LogoutFilter.class);
		
		// 세션방식 미사용
		http
        .sessionManagement((session) -> session 
		.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

			return http.build();
}
}