package edu.northwestern.bioinformatics.studycalendar.restlets;

import edu.northwestern.bioinformatics.studycalendar.domain.BlackoutDate;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.domain.Role;
import edu.northwestern.bioinformatics.studycalendar.service.SiteService;
import edu.northwestern.bioinformatics.studycalendar.xml.StudyCalendarXmlCollectionSerializer;
import edu.northwestern.bioinformatics.studycalendar.dao.BlackoutDateDao;
import org.restlet.Context;
import org.restlet.data.Method;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import org.springframework.beans.factory.annotation.Required;

import java.util.Collection;

/**
 * @author Saurabh Agrawal
 */
public class BlackoutDatesResource extends AbstractStorableCollectionResource<BlackoutDate> {

    private SiteService siteService;
    private StudyCalendarXmlCollectionSerializer<BlackoutDate> xmlSerializer;
    private Site site;
    private BlackoutDateDao blackoutDateDao;

    @Override
    public void init(Context context, Request request, Response response) {
        super.init(context, request, response);
        setAuthorizedFor(Method.GET, Role.SYSTEM_ADMINISTRATOR);
        setAuthorizedFor(Method.POST, Role.SYSTEM_ADMINISTRATOR);
    }

    public Collection<BlackoutDate> getAllObjects() throws ResourceException {
        String siteIdentifier = UriTemplateParameters.SITE_IDENTIFIER.extractFrom(getRequest());
        if (siteIdentifier == null) {
            throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "No site in request");
        }
        site = siteService.getByAssignedIdentifier(siteIdentifier);
        if (site == null) {
            throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Unknown site " + siteIdentifier );
        } else {
            return site.getBlackoutDates();
        }
    }

    @Override
    public String store(BlackoutDate blackoutDate){
        try {
            blackoutDateDao.save(blackoutDate);
            return String.format("sites/%s/blackout-dates/%s",
                    blackoutDate.getSite().getAssignedIdentifier(), blackoutDate.getGridId());
        } catch (Exception e) {
            String message = "Can not POST the blackoutDate on the site" + UriTemplateParameters.SITE_IDENTIFIER.extractFrom(getRequest());
            log.error(message, e);

        }

        return null;
    }

    public StudyCalendarXmlCollectionSerializer<BlackoutDate> getXmlSerializer() {
        return xmlSerializer;
    }

    public void setXmlSerializer(StudyCalendarXmlCollectionSerializer<BlackoutDate> xmlSerializer) {
        this.xmlSerializer = xmlSerializer;
    }

    @Required
    public void setSiteService(final SiteService siteService) {
        this.siteService = siteService;
    }

    @Required
    public void setBlackoutDateDao(BlackoutDateDao blackoutDateDao) {
        this.blackoutDateDao = blackoutDateDao;
    }
}