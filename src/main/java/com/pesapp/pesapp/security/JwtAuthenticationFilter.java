package com.pesapp.pesapp.security;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UsuarioDetailsService usuarioDetailsService;
    private final AuthCookieService authCookieService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            for (String token : extraerTokens(request)) {
                if (autenticarSiEsValido(token, request)) {
                    break;
                }
            }
        }

        filterChain.doFilter(request, response);
    }

    private List<String> extraerTokens(HttpServletRequest request) {
        List<String> tokens = new ArrayList<>();
        String accessCookie = authCookieService.extraerAccessToken(request);
        if (accessCookie != null && !accessCookie.isBlank()) {
            tokens.add(accessCookie);
        }

        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            tokens.add(authHeader.substring(7));
        }

        return tokens;
    }

    private boolean autenticarSiEsValido(String token, HttpServletRequest request) {
        try {
            String email = jwtService.extraerEmail(token);
            if (email == null) {
                return false;
            }

            UserDetails userDetails = usuarioDetailsService.loadUserByUsername(email);
            if (!jwtService.esTokenValido(token, userDetails)) {
                return false;
            }

            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                    userDetails,
                    null,
                    userDetails.getAuthorities());
            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authToken);
            return true;
        } catch (JwtException | IllegalArgumentException | AuthenticationException exception) {
            SecurityContextHolder.clearContext();
            return false;
        }
    }
}
