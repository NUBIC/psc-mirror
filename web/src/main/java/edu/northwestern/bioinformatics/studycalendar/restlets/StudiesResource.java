package edu.northwestern.bioinformatics.studycalendar.restlets;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarUserException;
import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.restlets.representations.StudyListJsonRepresentation;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole;
import edu.northwestern.bioinformatics.studycalendar.service.StudyService;
import edu.northwestern.bioinformatics.studycalendar.service.presenter.UserTemplateRelationship;
import edu.northwestern.bioinformatics.studycalendar.xml.StudyCalendarXmlCollectionSerializer;
import edu.northwestern.bioinformatics.studycalendar.xml.writers.StudySnapshotXmlSerializer;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Reference;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.Representation;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;
import org.springframework.beans.factory.annotation.Required;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static edu.northwestern.bioinformatics.studycalendar.restlets.QueryParameters.*;
import static edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole.STUDY_CALENDAR_TEMPLATE_BUILDER;

/**
 * @author Rhett Sutphin
 */
public class StudiesResource extends AbstractCollectionResource<Study> {

    private StudyDao studyDao;
    private StudyService studyService;

    private StudyCalendarXmlCollectionSerializer<Study> xmlSerializer;
    private StudySnapshotXmlSerializer studySnapshotXmlSerializer;

    @Override
    public void init(Context context, Request request, Response response) {
        super.init(context, request, response);
        addAuthorizationsFor(Method.GET, PscRole.valuesWithStudyAccess());
        addAuthorizationsFor(Method.POST, STUDY_CALENDAR_TEMPLATE_BUILDER);

        getVariants().add(new Variant(MediaType.APPLICATION_JSON));
    }

    @Override public boolean allowPost() { return true; }

    @Override
    @SuppressWarnings({ "ThrowInsideCatchBlockWhichIgnoresCaughtException" })
    public List<Study> getAllObjects() throws ResourceException {
        String q = Q.extractFrom(getRequest());
        List<Study> studies = studyDao.searchVisibleStudies(getCurrentUser().getVisibleStudyParameters(), q);
        Collection<String> privileges = PRIVILEGE.extractAllFrom(getRequest());

        if (!privileges.isEmpty()) {
            List<Study> filteredStudies =  new ArrayList<Study>();
            List<StudyPrivilege> requestedPrivileges = new ArrayList<StudyPrivilege>();
            for (String privilege : privileges) {
                StudyPrivilege requestedPrivilege = StudyPrivilege.lookUp(privilege);
                if (requestedPrivilege != null) {
                    requestedPrivileges.add(requestedPrivilege);
                } else {
                    throw new ResourceException(Status.CLIENT_ERROR_UNPROCESSABLE_ENTITY,
                        "Invalid study privilege: " + privilege);
                }
            }

            for (Study study : studies) {
                UserTemplateRelationship utr = new UserTemplateRelationship(getCurrentUser(), study);
                List<StudyPrivilege> studyPrivileges = StudyPrivilege.valuesFor(utr);
                if (studyPrivileges.containsAll(requestedPrivileges)) {
                   filteredStudies.add(study);
                }
            }
            return filteredStudies;
        } else {
            return studies;
        }
    }

    @Override
    public Representation represent(Variant variant) throws ResourceException {
        if (variant.getMediaType().includes(MediaType.APPLICATION_JSON)) {
            return new StudyListJsonRepresentation(getAllObjects(), getCurrentUser());
        } else {
            return super.represent(variant);
        }
    }

    @Override
    public void acceptRepresentation(Representation entity) throws ResourceException {
        if (MediaType.TEXT_XML.includes(entity.getMediaType())) {
            Study read;
            try {
                read = studySnapshotXmlSerializer.readDocument(entity.getStream());
                if (studyDao.getByAssignedIdentifier(read.getAssignedIdentifier()) != null) {
                    throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST,
                        "There is already a study with the identifier " + read.getAssignedIdentifier());
                }
                studyService.createInDesignStudyFromExamplePlanTree(read);
            } catch (IOException e) {
                log.warn("POST failed with IOException", e);
                throw new ResourceException(e);
            } catch (StudyCalendarUserException scue) {
                log.debug("POST failed due to validation problem", scue);
                throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, scue.getMessage(), scue);
            }
            getResponse().setStatus(Status.SUCCESS_CREATED);
            getResponse().setLocationRef(String.format(
                "studies/%s/template", Reference.encode(read.getAssignedIdentifier())));
        } else {
            throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST);
        }
    }

    @Override
    public StudyCalendarXmlCollectionSerializer<Study> getXmlSerializer() {
        return xmlSerializer;
    }

    ////// CONFIGURATION

    @Required
    public void setStudyDao(StudyDao studyDao) {
        this.studyDao = studyDao;
    }

    @Required
    public void setXmlSerializer(StudyCalendarXmlCollectionSerializer<Study> xmlSerializer) {
        this.xmlSerializer = xmlSerializer;
    }

    @Required
    public void setStudySnapshotXmlSerializer(StudySnapshotXmlSerializer studySnapshotXmlSerializer) {
        this.studySnapshotXmlSerializer = studySnapshotXmlSerializer;
    }

    @Required
    public void setStudyService(StudyService studyService) {
        this.studyService = studyService;
    }
}
