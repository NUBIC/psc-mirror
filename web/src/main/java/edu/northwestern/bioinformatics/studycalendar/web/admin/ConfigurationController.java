package edu.northwestern.bioinformatics.studycalendar.web.admin;

import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.PscAuthorizedHandler;
import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.ResourceAuthorization;
import gov.nih.nci.cabig.ctms.tools.configuration.ConfigurationProperty;
import gov.nih.nci.cabig.ctms.tools.configuration.ConfigurationPropertyEditor;
import gov.nih.nci.cabig.ctms.tools.configuration.Configuration;
import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.AccessControl;
import edu.northwestern.bioinformatics.studycalendar.web.PscSimpleFormController;
import edu.northwestern.bioinformatics.studycalendar.domain.Role;
import edu.nwu.bioinformatics.commons.spring.ValidatableValidator;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.validation.BindException;

import java.util.Collection;
import java.util.Map;
import static edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole.SYSTEM_ADMINISTRATOR;

/**
 * @author Rhett Sutphin
 */
@AccessControl(roles = Role.SYSTEM_ADMINISTRATOR)
public class ConfigurationController extends PscSimpleFormController implements PscAuthorizedHandler {
    private Configuration configuration;

    public ConfigurationController() {
        setCommandClass(ConfigurationCommand.class);
        setValidator(new ValidatableValidator());
        setFormView("admin/configure");
    }

    public Collection<ResourceAuthorization> authorizations(String httpMethod, Map<String, String[]> queryParameters) {
        return ResourceAuthorization.createCollection(SYSTEM_ADMINISTRATOR);
    }    
    
    @Override
    protected Object formBackingObject(HttpServletRequest request) throws Exception {
        return new ConfigurationCommand(configuration);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) throws Exception {
        super.initBinder(request, binder);
        for (ConfigurationProperty<?> property : configuration.getProperties().getAll()) {
            binder.registerCustomEditor(Object.class, "conf[" + property.getKey() + "].value",
                new ConfigurationPropertyEditor(property));
        }
    }

    @Override
    protected ModelAndView onSubmit(Object command, BindException errors) throws Exception {
        return new ModelAndView("redirectToAdministration");
    }

    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }
}
