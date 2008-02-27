package edu.northwestern.bioinformatics.studycalendar.web.admin;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.Errors;
import org.restlet.util.Template;
import edu.nwu.bioinformatics.commons.spring.Validatable;
import edu.northwestern.bioinformatics.studycalendar.web.GeneratedUriTemplateVariable;
import static edu.northwestern.bioinformatics.studycalendar.tools.configuration.Configuration.*;

import gov.nih.nci.cabig.ctms.tools.configuration.Configuration;
import gov.nih.nci.cabig.ctms.tools.configuration.ConfigurationProperty;

import java.util.Collection;
import java.util.Arrays;

/**
 * @author Rhett Sutphin
 */
public class ConfigurationCommand implements Validatable {
    private BindableConfiguration conf;
    private Configuration originalConfiguration;

    public ConfigurationCommand(Configuration configuration) {
        conf = new BindableConfiguration(configuration);
        originalConfiguration = configuration;
    }

    public BindableConfiguration getConf() {
        return conf;
    }

    public void validate(Errors errors) {
        Collection<ConfigurationProperty<Template>> templateProperties = Arrays.asList(PATIENT_PAGE_URL, STUDY_PAGE_URL);

        for (ConfigurationProperty<Template> templateProperty : templateProperties) {
            Template value = originalConfiguration.get(templateProperty);
            if (value !=null) {
                for (String varName : value.getVariableNames()) {
                    boolean matched = false;
                    for (GeneratedUriTemplateVariable knownVar : GeneratedUriTemplateVariable.values()) {
                        if (knownVar.attributeName().equals(varName)) {
                            matched = true; break;
                        }
                    }
                    if (!matched) {
                        errors.rejectValue(
                                "conf[" + templateProperty.getKey() + "].value",
                                "error.configuration.uriTemplate.invalidVariable", new Object[] { varName }, "Invalid variable");
                    }
                }
            }
        }
    }
}
