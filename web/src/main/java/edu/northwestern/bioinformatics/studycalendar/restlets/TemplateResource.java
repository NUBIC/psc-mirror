package edu.northwestern.bioinformatics.studycalendar.restlets;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarUserException;
import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole;
import edu.northwestern.bioinformatics.studycalendar.service.StudyService;
import edu.northwestern.bioinformatics.studycalendar.service.importer.TemplateImportService;
import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.ResourceAuthorization;
import org.restlet.data.Disposition;
import org.restlet.data.Method;
import org.restlet.Request;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.resource.ResourceException;
import org.restlet.representation.Variant;
import org.springframework.beans.factory.annotation.Required;

import java.io.IOException;

/**
 * Resource representing a study and its planned calendar, including all amendments.
 *
 * @author Rhett Sutphin
 */
public class TemplateResource extends AbstractDomainObjectResource<Study> {
    private boolean useDownloadMode;
    private StudyDao studyDao;
    private StudyService studyService;
    private TemplateImportService templateImportService;

    @Override
    public Representation get(Variant variant) throws ResourceException {
        Representation representation = super.get(variant);
        if (useDownloadMode) {
            representation.setDisposition(new Disposition(Disposition.TYPE_ATTACHMENT));
            representation.getDisposition().setFilename(getRequestedObject().getAssignedIdentifier() + ".xml");
        }
        representation.setModificationDate(getRequestedObject().getLastModifiedDate());
        return representation;
    }

    @Override
    public void doInit() {
        super.doInit();
        Study study = getRequestedObjectDuringInit();
        addAuthorizationsFor(Method.GET, ResourceAuthorization.createAllStudyAuthorizations(study));
        addAuthorizationsFor(Method.PUT,
            ResourceAuthorization.createTemplateManagementAuthorizations(study, PscRole.STUDY_CALENDAR_TEMPLATE_BUILDER));

        useDownloadMode = getRequest().getResourceRef().getQueryAsForm().getNames().contains("download");
    }

    @Override
    protected Study loadRequestedObject(Request request) {
        String studyIdent = UriTemplateParameters.STUDY_IDENTIFIER.extractFrom(request);
        Study base = studyDao.getByAssignedIdentifier(studyIdent);
        if (base == null) {
            base = studyDao.getByGridId(studyIdent);
        }
        if (base != null && request.getMethod().equals(Method.GET)) {
            return studyService.getCompleteTemplateHistory(base);
        }
        return base;
    }

    @Override
    public Representation put(Representation entity, Variant variant) throws ResourceException {
        Study out;
        try {
            Study imported = templateImportService.readAndSaveTemplate(getRequestedObject(), entity.getStream());
            out = studyService.getCompleteTemplateHistory(imported);
        } catch (IOException e) {
            log.warn("PUT failed with IOException", e);
            throw new ResourceException(e);
        } catch (StudyCalendarUserException e) {
            log.error("Error PUTting study", e);
            throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, e);
        }
        getResponse().setEntity(createXmlRepresentation(out));
        if (getRequestedObject() == null) {
            getResponse().setStatus(Status.SUCCESS_CREATED);
        } else {
            getResponse().setStatus(Status.SUCCESS_OK);
        }

        return null;
    }

    ////// CONFIGURATION

    @Required
    public void setStudyDao(StudyDao studyDao) {
        this.studyDao = studyDao;
    }

    @Required
    public void setStudyService(StudyService studyService) {
        this.studyService = studyService;
    }

    @Required
    public void setTemplateImportService(TemplateImportService templateImportService) {
        this.templateImportService = templateImportService;
    }
}
