package edu.northwestern.bioinformatics.studycalendar.restlets;

import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.domain.Role;
import edu.northwestern.bioinformatics.studycalendar.service.dataproviders.SiteConsumer;
import edu.northwestern.bioinformatics.studycalendar.xml.StudyCalendarXmlCollectionSerializer;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Method;
import org.springframework.beans.factory.annotation.Required;
import java.util.List;
/**
 * @author Jalpa Patel
 */
public class ProvidedSitesResource extends  AbstractCollectionResource<Site> {
    private StudyCalendarXmlCollectionSerializer<Site> xmlSerializer;
    private SiteConsumer siteConsumer;

    @Override
    public void init(Context context, Request request, Response response) {
        super.init(context, request, response);
        setAuthorizedFor(Method.GET, Role.SITE_COORDINATOR, Role.SYSTEM_ADMINISTRATOR);
    }

    public List<Site> getAllObjects() {
        String q = QueryParameters.Q.extractFrom(getRequest());
        return siteConsumer.search(q);
    }

    public StudyCalendarXmlCollectionSerializer<Site> getXmlSerializer() {
        return xmlSerializer;
    }

    public void setXmlSerializer(StudyCalendarXmlCollectionSerializer<Site> xmlSerializer) {
        this.xmlSerializer = xmlSerializer;
    }
    
    @Required
    public void setSiteConsumer(SiteConsumer siteProvider) {
        this.siteConsumer = siteProvider;
    }
}
