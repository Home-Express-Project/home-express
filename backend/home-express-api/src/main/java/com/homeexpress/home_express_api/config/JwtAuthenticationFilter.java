package com.homeexpress.home_express_api.config;

import com.homeexpress.home_express_api.entity.Customer;
import com.homeexpress.home_express_api.entity.Transport;
import com.homeexpress.home_express_api.repository.CustomerRepository;
import com.homeexpress.home_express_api.repository.TransportRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    // NOTE: filter nay chay moi request de extract JWT token tu header
    // neu valid thi set SecurityContext de Spring Security biet user da login
    
    @Autowired
    private JwtTokenProvider jwtTokenProvider;
    
    @Autowired
    private CustomerRepository customerRepository;
    
    @Autowired
    private TransportRepository transportRepository;
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                    HttpServletResponse response, 
                                    FilterChain filterChain) throws ServletException, IOException {
        
        try {
            // 1. lay JWT tu Authorization header
            String jwt = getJwtFromRequest(request);
            
            // 2. neu co token va valid
            if (StringUtils.hasText(jwt) && jwtTokenProvider.validateToken(jwt)) {
                // 3. extract user info tu token
                Long userId = jwtTokenProvider.getUserIdFromToken(jwt);
                String role = jwtTokenProvider.getRoleFromToken(jwt);
                
                // 4. load user entity based on role
                Object principal = userId; // default to userId
                
                if ("CUSTOMER".equals(role)) {
                    Optional<Customer> customerOpt = customerRepository.findByUser_UserId(userId);
                    if (customerOpt.isPresent()) {
                        principal = customerOpt.get();
                    }
                } else if ("TRANSPORT".equals(role)) {
                    Optional<Transport> transportOpt = transportRepository.findByUser_UserId(userId);
                    if (transportOpt.isPresent()) {
                        principal = transportOpt.get();
                    }
                }
                
                // 5. tao Authentication object
                // chu y: authorities phai co prefix "ROLE_" de Spring Security nhan dang
                SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + role);
                UsernamePasswordAuthenticationToken authentication = 
                    new UsernamePasswordAuthenticationToken(principal, null, Collections.singletonList(authority));
                
                // 6. set request details (IP, user agent)
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                
                // 7. set vao SecurityContext - tu day Spring Security biet user da login
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception ex) {
            // log loi nhung ko throw - de request tiep tuc
            // neu token invalid thi cu de 401 Unauthorized tu security config
            logger.error("Could not set user authentication in security context", ex);
        }
        
        // 7. tiep tuc filter chain
        filterChain.doFilter(request, response);
    }
    
    // helper: extract JWT tu "Authorization: Bearer <token>" header
    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        
        // format: "Bearer eyJhbGciOiJIUzI1NiIs..."
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7); // cat bo "Bearer "
        }

        // Fall back to HTTP-only cookie
        if (request.getCookies() != null) {
            return Arrays.stream(request.getCookies())
                    .filter(cookie -> "access_token".equals(cookie.getName()))
                    .map(Cookie::getValue)
                    .findFirst()
                    .orElse(null);
        }
        
        return null;
    }
}
