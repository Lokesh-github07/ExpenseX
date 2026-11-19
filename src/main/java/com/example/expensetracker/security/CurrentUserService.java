package com.example.expensetracker.security;

import com.example.expensetracker.exception.ForbiddenException;
import com.example.expensetracker.exception.UnauthorizedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Single server-side source of truth for "who is making this request".
 *
 * The identity always comes from the SecurityContext, which JwtFilter fills
 * from the verified JWT. It never comes from a request parameter or body.
 */
@Component
public class CurrentUserService {

    private static final String ROLE_ADMIN = "ROLE_ADMIN";

    /** The authenticated principal, or 401 if the request is anonymous. */
    public UserPrincipal getPrincipal() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || !(authentication.getPrincipal() instanceof UserPrincipal principal)) {

            throw new UnauthorizedException("Authentication required");
        }

        return principal;
    }

    /** ID of the authenticated user. */
    public Long getCurrentUserId() {
        return getPrincipal().getUserId();
    }

    public boolean isAdmin() {

        return getPrincipal().getAuthorities().stream()
                .anyMatch(a -> ROLE_ADMIN.equals(a.getAuthority()));
    }

    /**
     * Use for any user ID that arrived from the client (path, query, body).
     *
     * - null  -> the caller did not send one; the authenticated user's ID is used
     * - equal -> fine, returns the authenticated user's ID
     * - other -> 403, somebody is asking for another person's data
     */
    public Long requireSelf(Long requestedUserId) {

        Long currentUserId = getCurrentUserId();

        if (requestedUserId != null
                && !requestedUserId.equals(currentUserId)) {

            throw new ForbiddenException(
                    "You are not allowed to access another user's data"
            );
        }

        return currentUserId;
    }

    /** Own account, or an administrator. */
    public void requireSelfOrAdmin(Long requestedUserId) {

        if (!isAdmin()) {
            requireSelf(requestedUserId);
        }
    }

    /** Administrators only. */
    public void requireAdmin() {

        if (!isAdmin()) {
            throw new ForbiddenException(
                    "Administrator access required"
            );
        }
    }
}
