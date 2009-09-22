package edu.northwestern.bioinformatics.studycalendar.core.accesscontrol;

import edu.northwestern.bioinformatics.studycalendar.domain.User;
import edu.northwestern.bioinformatics.studycalendar.service.UserService;
import org.acegisecurity.Authentication;
import org.acegisecurity.context.SecurityContextHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

/**
 * @author Padmaja Vedula
 * @author Rhett Sutphin
 */
public class ApplicationSecurityManager {
    private final Logger log = LoggerFactory.getLogger(getClass());
    private UserService userService;

    public String getUserName() {
        String user = null;
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        log.trace("getUserName(): Retrieved authentication {}", authentication);
        if (authentication != null) {
            user = authentication.getName();
        }
        return user;
    }

    public User getUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        log.trace("getUser(): Retrieved authentication {}", authentication);
        if (authentication == null) {
            return null;
        } else {
            return (User) authentication.getPrincipal();
        }
    }

    public void removeUserSession() {
        log.debug("Removing authentication from {}", SecurityContextHolder.getContext());
        SecurityContextHolder.getContext().setAuthentication(null);
    }

    /**
     * Reloads the logged in {@link User} in the current hibernate session.
     */
    // TODO: copy metadata
    public User getFreshUser() {
        String userName = getUserName();
        if (userName ==  null) {
            return null;
        } else {
            return userService.getUserByName(userName);
        }
    }

    @Required
    public void setUserService(UserService userService) {
        this.userService = userService;
    }
}
