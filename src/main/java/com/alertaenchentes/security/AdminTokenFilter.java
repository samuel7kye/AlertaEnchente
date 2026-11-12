package com.alertaenchentes.security;

import com.alertaenchentes.service.AdminService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@RequiredArgsConstructor
public class AdminTokenFilter extends OncePerRequestFilter {

    private static final String TOKEN_PREFIX = "Bearer ";
    private final AdminService adminService;

    public boolean hasValidToken(HttpServletRequest request) {
        String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
        return isAdminRequest(request) && authorization != null && authorization.startsWith(TOKEN_PREFIX)
                && adminService.verifyToken(authorization.substring(TOKEN_PREFIX.length()));
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        if (hasValidToken(request)) {
            var context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(UsernamePasswordAuthenticationToken.authenticated(
                    "admin", null, List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))));
            SecurityContextHolder.setContext(context);
        }
        filterChain.doFilter(request, response);
    }

    private boolean isAdminRequest(HttpServletRequest request) {
        return request.getServletPath().startsWith("/api/admin/");
    }
}
