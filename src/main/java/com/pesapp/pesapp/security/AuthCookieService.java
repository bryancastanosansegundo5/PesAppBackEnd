package com.pesapp.pesapp.security;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthCookieService {

    @Value("${app.auth.access-cookie-name}")
    private String accessCookieName;

    @Value("${app.auth.refresh-cookie-name}")
    private String refreshCookieName;

    @Value("${app.auth.cookie-secure}")
    private boolean cookieSecure;

    @Value("${app.auth.cookie-same-site}")
    private String cookieSameSite;

    @Value("${app.auth.cookie-domain:}")
    private String cookieDomain;

    public String getAccessCookieName() {
        return accessCookieName;
    }

    public String getRefreshCookieName() {
        return refreshCookieName;
    }

    public String crearAccessTokenCookie(String token, Duration maxAge) {
        return ResponseCookie.from(accessCookieName, token)
                .httpOnly(true)
                .secure(cookieSecure)
                .sameSite(cookieSameSite)
                .path("/")
                .maxAge(maxAge)
                .domain(normalizarDominio())
                .build()
                .toString();
    }

    public String crearRefreshTokenCookie(String token, Duration maxAge) {
        return ResponseCookie.from(refreshCookieName, token)
                .httpOnly(true)
                .secure(cookieSecure)
                .sameSite(cookieSameSite)
                .path("/api/auth")
                .maxAge(maxAge)
                .domain(normalizarDominio())
                .build()
                .toString();
    }

    public String limpiarAccessTokenCookie() {
        return ResponseCookie.from(accessCookieName, "")
                .httpOnly(true)
                .secure(cookieSecure)
                .sameSite(cookieSameSite)
                .path("/")
                .maxAge(Duration.ZERO)
                .domain(normalizarDominio())
                .build()
                .toString();
    }

    public String limpiarRefreshTokenCookie() {
        return ResponseCookie.from(refreshCookieName, "")
                .httpOnly(true)
                .secure(cookieSecure)
                .sameSite(cookieSameSite)
                .path("/api/auth")
                .maxAge(Duration.ZERO)
                .domain(normalizarDominio())
                .build()
                .toString();
    }

    public String extraerAccessToken(HttpServletRequest request) {
        return extraerCookie(request, accessCookieName);
    }

    public List<String> extraerAccessTokens(HttpServletRequest request) {
        return extraerCookies(request, accessCookieName);
    }

    public String extraerRefreshToken(HttpServletRequest request) {
        return extraerCookie(request, refreshCookieName);
    }

    public void anadirCookiesAutenticacion(HttpHeaders headers, String accessToken, Duration accessMaxAge,
            String refreshToken, Duration refreshMaxAge) {
        headers.add(HttpHeaders.SET_COOKIE, crearAccessTokenCookie(accessToken, accessMaxAge));
        headers.add(HttpHeaders.SET_COOKIE, crearRefreshTokenCookie(refreshToken, refreshMaxAge));
    }

    public void limpiarCookiesAutenticacion(HttpHeaders headers) {
        headers.add(HttpHeaders.SET_COOKIE, limpiarAccessTokenCookie());
        headers.add(HttpHeaders.SET_COOKIE, limpiarRefreshTokenCookie());
    }

    private String extraerCookie(HttpServletRequest request, String nombre) {
        List<String> valores = extraerCookies(request, nombre);
        return valores.isEmpty() ? null : valores.get(0);
    }

    private List<String> extraerCookies(HttpServletRequest request, String nombre) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return List.of();
        }

        List<String> valores = new ArrayList<>();
        for (Cookie cookie : cookies) {
            if (nombre.equals(cookie.getName())) {
                valores.add(cookie.getValue());
            }
        }

        return valores;
    }

    private String normalizarDominio() {
        return cookieDomain == null || cookieDomain.isBlank() ? null : cookieDomain.trim();
    }
}
