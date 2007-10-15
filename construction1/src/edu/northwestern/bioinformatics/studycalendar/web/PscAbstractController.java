package edu.northwestern.bioinformatics.studycalendar.web;

import edu.northwestern.bioinformatics.studycalendar.utils.breadcrumbs.Crumb;
import edu.northwestern.bioinformatics.studycalendar.utils.breadcrumbs.CrumbSource;
import org.springframework.web.servlet.mvc.AbstractController;
import org.springframework.beans.factory.annotation.Required;

/**
 * @author Rhett Sutphin
 */
public abstract class PscAbstractController extends AbstractController implements CrumbSource {
    private Crumb crumb;
    private ControllerTools controllerTools;

    ////// IMPLEMENTATION OF CrumbSource

    public Crumb getCrumb() {
        return crumb;
    }

    ////// CONFIGURATION

    public void setCrumb(Crumb crumb) {
        this.crumb = crumb;
    }

    public ControllerTools getControllerTools() {
        return controllerTools;
    }

    @Required
    public void setControllerTools(ControllerTools controllerTools) {
        this.controllerTools = controllerTools;
    }
}
