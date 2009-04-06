package edu.northwestern.bioinformatics.studycalendar.security.internal;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarUserException;
import edu.northwestern.bioinformatics.studycalendar.security.AuthenticationSystemConfiguration;
import edu.northwestern.bioinformatics.studycalendar.security.CompleteAuthenticationSystem;
import edu.northwestern.bioinformatics.studycalendar.security.plugin.MultipleFilterFilter;
import org.osgi.service.cm.ConfigurationException;
import org.springframework.beans.factory.annotation.Required;

import java.util.Dictionary;

/**
 * @author Rhett Sutphin
 */
public class CompleteAuthenticationSystemImpl extends MultipleFilterFilter implements CompleteAuthenticationSystem {
    private AuthenticationSystemConfiguration authenticationSystemConfiguration;

    @SuppressWarnings({ "RawUseOfParameterizedType" })
    public void updated(Dictionary dictionary) throws ConfigurationException {
        authenticationSystemConfiguration.updated(dictionary);
        try {
            authenticationSystemConfiguration.getAuthenticationSystem();
        } catch (StudyCalendarUserException scue) {
            throw new ConfigurationException("Unknown", 
                "Problem initializing authentication system: " + scue.getMessage(), scue);
        }
    }

    ////// CONFIGURATION

    @Required
    public void setConfiguration(AuthenticationSystemConfiguration authenticationSystemConfiguration) {
        this.authenticationSystemConfiguration = authenticationSystemConfiguration;
    }
}