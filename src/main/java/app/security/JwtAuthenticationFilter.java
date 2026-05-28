package app.security;

import jakarta.annotation.Priority;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.SecurityContext;
import jakarta.ws.rs.ext.Provider;
import java.io.IOException;
import java.security.Principal;

@Provider
@Priority(Priorities.AUTHENTICATION)
public class JwtAuthenticationFilter implements ContainerRequestFilter {

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        String authHeader = requestContext.getHeaderString("Authorization");

        // Only process if the request has a Bearer token
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);

            try {
                if (JwtUtil.validateToken(token)) {
                    String username = JwtUtil.extractUsername(token);
                    String role = JwtUtil.extractRole(token);

                    // Inject custom SecurityContext so sec.isUserInRole() works
                    requestContext.setSecurityContext(new SecurityContext() {
                        @Override
                        public Principal getUserPrincipal() { return () -> username; }

                        @Override
                        public boolean isUserInRole(String r) {
                            return role != null && role.equalsIgnoreCase(r);
                        }

                        @Override
                        public boolean isSecure() { return true; }

                        @Override
                        public String getAuthenticationScheme() { return "Bearer"; }
                    });
                }
            } catch (Exception e) {
                // Token invalid or expired - we leave the context null, 
                // and the API will return 401/403 as defined in your controllers.
            }
        }
    }
}