package edu.northwestern.bioinformatics.studycalendar.restlets;

import org.restlet.Restlet;
import org.restlet.data.Status;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.ChallengeResponse;
import org.acegisecurity.providers.AuthenticationProvider;
import org.acegisecurity.providers.UsernamePasswordAuthenticationToken;
import org.acegisecurity.BadCredentialsException;
import org.acegisecurity.GrantedAuthority;
import static org.easymock.classextension.EasyMock.*;

import java.util.regex.Pattern;

import edu.northwestern.bioinformatics.studycalendar.domain.User;
import edu.northwestern.bioinformatics.studycalendar.domain.Fixtures;

/**
 * @author Rhett Sutphin
 */
public class PscGuardTest extends RestletTestCase {
    private static final String USERNAME = "joe";

    private PscGuard guard;
    private User user;

    private Restlet nextRestlet;
    private AuthenticationProvider authenticationProvider;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        user = Fixtures.createNamedInstance(USERNAME, User.class);
        
        nextRestlet = registerMockFor(Restlet.class);
        authenticationProvider = registerMockFor(AuthenticationProvider.class);

        guard = new PscGuard();
        guard.setNext(nextRestlet);
        guard.setAuthenticationProvider(authenticationProvider);

        request.setResourceRef(BASE_URI);
    }

    public void testAuthenticationSkippedForExcepts() throws Exception {
        guard.setExcept(Pattern.compile("everyone-welcome.*"));
        request.setResourceRef(ROOT_URI + "/everyone-welcome/in-here");
        expectNextInvoked();
        doHandle();
    }

    public void testExceptsMatchedFromBeginningOfUri() throws Exception {
        Pattern except = Pattern.compile("in.*");
        String uri = "everyone-welcome/in-here";
        // ensure that the pattern would match if the guard was implemented using find instead of matches
        assertTrue("Test setup failure", except.matcher(uri).find());

        guard.setExcept(except);
        request.setResourceRef(ROOT_URI + '/' + uri);
        doHandle();

        assertResponseStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
    }

    public void testAuthenticationRequiredForNotSkipped() throws Exception {
        guard.setExcept(Pattern.compile("everyone-welcome.*"));
        request.setResourceRef(ROOT_URI + "/keep-out/of-here");
        doHandle();

        assertResponseStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
    }

    public void testAuthenticationRequiredForEverythingIfNoExcept() throws Exception {
        guard.setExcept(null);
        request.setResourceRef(ROOT_URI + "/keep-out/of-here");
        doHandle();

        assertResponseStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
    }

    public void testAuthenticationWorksIfAuthenticationProviderPasses() throws Exception {
        expectBasicAuthChallengeResponse(USERNAME, "pass");

        UsernamePasswordAuthenticationToken authenticated
            = new UsernamePasswordAuthenticationToken(user, "pass", new GrantedAuthority[0]);
        expect(authenticationProvider.authenticate(new UsernamePasswordAuthenticationToken(USERNAME, "pass")))
            .andReturn(authenticated);
        expectNextInvoked();

        doHandle();
        assertResponseStatus(Status.SUCCESS_OK);
    }

    public void testAuthenticationFailsIfAuthenticationProviderThrowsException() throws Exception {
        expectBasicAuthChallengeResponse(USERNAME, "wrongpass");
        expect(authenticationProvider.authenticate(new UsernamePasswordAuthenticationToken(USERNAME, "wrongpass")))
            .andThrow(new BadCredentialsException("That's not the right thing"));

        doHandle();
        assertResponseStatus(Status.CLIENT_ERROR_UNAUTHORIZED); // Not 403 -- failure sends rechallenge
    }

    public void testSuccessfulAuthenticationStoresTokenInRequest() throws Exception {
        expectBasicAuthChallengeResponse(USERNAME, "pass");

        UsernamePasswordAuthenticationToken authenticated
            = new UsernamePasswordAuthenticationToken(user, "pass", new GrantedAuthority[0]);
        expect(authenticationProvider.authenticate(new UsernamePasswordAuthenticationToken(USERNAME, "pass")))
            .andReturn(authenticated);
        expectNextInvoked();

        doHandle();

        Object actualToken = request.getAttributes().get(PscGuard.AUTH_TOKEN_ATTRIBUTE_KEY);
        assertNotNull("Token missing", actualToken);
        assertSame("Wrong token", authenticated, actualToken);
    }

    private void expectBasicAuthChallengeResponse(String username, String password) {
        // the guard will be presented with the username/pass, already decoded
        request.setChallengeResponse(
            new ChallengeResponse(ChallengeScheme.HTTP_BASIC, username, password));
    }

    private void doHandle() {
        replayMocks();
        guard.handle(request, response);
        verifyMocks();
    }

    private void expectNextInvoked() {
        nextRestlet.handle(request, response);
    }
}
