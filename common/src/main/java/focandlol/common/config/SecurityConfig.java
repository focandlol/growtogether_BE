package focandlol.common.config;

import focandlol.common.auth.filter.JwtAuthenticationFilter;
import focandlol.common.auth.handler.JwtAccessDeniedHandler;
import focandlol.common.auth.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;
    private final JwtUtil jwtUtil;

    private static final String[] PUBLIC_URLS = {
            // spring default url
            "/error",
            "/favicon.ico",

            // login & kakao oauth
            "/login",
            "/oauth2/authorization/kakao",
            "/oauth2/code/kakao",
            "/oauth/kakao",
            "/api/email/**",
            "/member/memberLogin",
            "/member/forgot-password",
            "/member/reset-password",
            "/member/find-email",
            "/payment/**",
            "/member/register",
            "/member/memberLogin",
            "/ws-chat/**",
            "/topic/**",
            "/queue/**",
            "/app/**",
            "/user/**",
            "/swagger-ui/**",
            "/v3/api-docs/**",
            "/swagger-ui.html",
            "/api/bootcamp/skillName",
            "/api/bootcamp/programCourses"
    };

    private static final String[] PUBLIC_GET_URLS = {
            "/api/study/**",
            "/api/study/comments/**",
            "/api/bootcamp/search",
            "/api/bootcamp/top",
            "/api/bootcamp/**",
            "/api/bootcamp/comments/**",


    };

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)
                .oauth2Login(oauth2 -> { }
                )
                .authorizeHttpRequests(authorizeRequests ->
                        authorizeRequests
                                // 퍼블릭 URL은 인증 없이 허용
                                .requestMatchers(PUBLIC_URLS).permitAll()
                                .requestMatchers(PUBLIC_GET_URLS).permitAll()
                                .requestMatchers("/ws-chat/**","/topic/**","/app/**").permitAll()
                                .requestMatchers("/api/bootcamp","/sse").authenticated()
                                // 그 외의 요청은 인증 필요
                                .anyRequest().authenticated()
                )
                .exceptionHandling(config -> config
                        .accessDeniedHandler(jwtAccessDeniedHandler)
                )
                .addFilterBefore(new JwtAuthenticationFilter(PUBLIC_URLS, PUBLIC_GET_URLS, jwtUtil), UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
       // configuration.setAllowedOrigins(List.of("https://jiangxy.github.io","http://localhost:3000"));
      //  configuration.setAllowedOriginPatterns(List.of("https://jiangxy.github.io", "http://localhost:*"));
       // configuration.setAllowedOriginPatterns(List.of("https://jiangxy.github.io", "http://localhost:*"));
        //onfiguration.setAllowedOrigins(List.of("https://jiangxy.github.io","http://localhost:5173"));
        configuration.setAllowedOriginPatterns(List.of("*"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setExposedHeaders(List.of("Authorization", "Access-Token-Expire-Time"));
     //   configuration.setExposedHeaders(List.of("Authorization")); //Authorization 헤더를 노출하여 프론트엔드에서 접근할 수 있도록 설정
      //  configuration.setExposedHeaders(List.of("Set-Cookie","loggedUser","authorization","Access-Token-Expire-Time","authentication"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

}