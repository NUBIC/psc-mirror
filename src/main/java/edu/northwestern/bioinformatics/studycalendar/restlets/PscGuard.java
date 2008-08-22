package edu.northwestern.bioinformatics.studycalendar.restlets;

import com.noelios.restlet.Engine;
import com.noelios.restlet.authentication.AuthenticationHelper;
import edu.northwestern.bioinformatics.studycalendar.StudyCalendarSystemException;
import edu.northwestern.bioinformatics.studycalendar.security.AuthenticationSystemConfiguration;
import edu.northwestern.bioinformatics.studycalendar.security.plugin.AuthenticationSystem;
import org.acegisecurity.Authentication;
import org.acegisecurity.AuthenticationException;
import org.acegisecurity.AuthenticationManager;
import org.acegisecurity.context.SecurityContextHolder;
import org.restlet.Guard;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.MediaType;
import org.restlet.data.Reference;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import java.util.regex.Pattern;

/**
 * Authentication piece of the API security implementation.  There is a single
 * instance of this guard for the entire API router.  It performs authentication
 * and then puts the Acegi authentication token as a request attribute under the key
 * {@link #AUTH_TOKEN_ATTRIBUTE_KEY}.
 * <p>
 * Authorization is handled by {@link AuthorizingFinder} based on resources
 * implementing {@link AuthorizedResource}.
 *  
 *
 * @author Rhett Sutphin
 */
public class PscGuard extends Guard {
    private final Logger log = LoggerFactory.getLogger(getClass());

    public static final ChallengeScheme PSC_TOKEN
        = new ChallengeScheme("HTTP_psc_token", "psc_token", "Token-based pluggable authentication for PSC");
    public static final String AUTH_TOKEN_ATTRIBUTE_KEY = "pscAuthenticationToken";

    private Pattern except;
    private AuthenticationSystemConfiguration authenticationSystemConfiguration;

    public PscGuard() {
        super(null, ChallengeScheme.HTTP_BASIC, "PSC");
    }

    @Override
    public int doHandle(Request request, Response response) {
        if (doesNotRequireAuthentication(request)) {
            accept(request, response);
            return CONTINUE;
        }
        Authentication sessionAuth = SecurityContextHolder.getContext().getAuthentication();
        if (sessionAuth != null && sessionAuth.isAuthenticated()) {
            preserveAuthentationForAuthorizationDecisions(request, sessionAuth);
            accept(request, response);
            return CONTINUE;
        } else {
            try {
                return super.doHandle(request, response);
            } catch (UnimplementedScheme unimplementedScheme) {
                response.setEntity(unimplementedScheme.getMessage(), MediaType.TEXT_PLAIN);
                response.setStatus(Status.SERVER_ERROR_NOT_IMPLEMENTED);
                return STOP;
            }
        }
    }

    private boolean doesNotRequireAuthentication(Request request) {
        Reference ref = request.getResourceRef().getRelativeRef(request.getRootRef());
        log.debug("Checking for guard exceptions against {}", ref);
        return except != null && except.matcher(ref.toString()).matches();
    }

    @Override
    // Mostly copied from Restlet's AuthenticationUtils.authenticate.
    // This is a roundabout implementation (since I'm overriding this
    // method anyway, delegating to an AuthenticationHelper sort of obfuscates
    // things), but once Restlet supports multiple challenge schemes
    // it will be easier to upgrade.
    public int authenticate(Request request) {
        int result = Guard.AUTHENTICATION_MISSING;

        // An authentication scheme has been defined,
        // the request must be authenticated
        ChallengeResponse cr = request.getChallengeResponse();

        if (cr != null) {
            if (this.supportsScheme(cr.getScheme())) {
                AuthenticationHelper helper = Engine.getInstance()
                        .findHelper(cr.getScheme(), false, true);

                if (helper != null) {
                    result = helper.authenticate(cr, request, this);
                } else {
                    throw new IllegalArgumentException("Challenge scheme "
                            + cr.getScheme()
                            + " not supported by the Restlet engine.");
                }
            } else {
                // The challenge schemes are incompatible, we need to
                // challenge the client
            }
        } else {
            // No challenge response found, we need to challenge the client
        }

        return result;
    }

    public boolean supportsScheme(ChallengeScheme scheme) {
        return ChallengeScheme.HTTP_BASIC.equals(scheme)
            || PscGuard.PSC_TOKEN.equals(scheme);
    }

    @Override
    public boolean checkSecret(Request request, String identifier, char[] secret) {
        return authenticate(request, getAuthenticationSystem()
            .createUsernamePasswordAuthenticationRequest(identifier, new String(secret)));
    }

    public boolean checkToken(Request request, String credentials) {
        return authenticate(request, getAuthenticationSystem()
            .createTokenAuthenticationRequest(credentials));
    }

    protected boolean authenticate(Request request, Authentication token) {
        if (token == null) {
            throw new UnimplementedScheme(request.getChallengeResponse().getScheme());
        }

        try {
            Authentication auth = getAuthenticationManager().authenticate(token);
            if (auth == null) {
                return false;
            } else {
                preserveAuthentationForAuthorizationDecisions(request, auth);
                return auth.isAuthenticated();
            }
        } catch (AuthenticationException ae) {
            log.debug("Authentication using injected authentication provider failed", ae);
            return false;
        }
    }

    private void preserveAuthentationForAuthorizationDecisions(Request request, Authentication auth) {
        request.getAttributes().put(AUTH_TOKEN_ATTRIBUTE_KEY, auth);
    }

    ////// CONFIGURATION

    public Pattern getExcept() {
        return except;
    }

    public void setExcept(Pattern except) {
        this.except = except;
    }

    protected AuthenticationManager getAuthenticationManager() {
        return getAuthenticationSystem().authenticationManager();
    }

    protected AuthenticationSystem getAuthenticationSystem() {
        return authenticationSystemConfiguration.getAuthenticationSystem();
    }

    @Required
    public void setAuthenticationSystemConfiguration(AuthenticationSystemConfiguration authenticationSystemConfiguration) {
        this.authenticationSystemConfiguration = authenticationSystemConfiguration;
    }

    ///// INNER

    private static final class UnimplementedScheme extends StudyCalendarSystemException {
        public UnimplementedScheme(ChallengeScheme scheme) {
            super("%s authentication is not supported with the configured authentication system", scheme.getTechnicalName());
        }
    }
}
