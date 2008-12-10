package edu.northwestern.bioinformatics.studycalendar.restlets;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarValidationException;
import edu.northwestern.bioinformatics.studycalendar.dao.StudySubjectAssignmentDao;
import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Role;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledCalendar;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledStudySegment;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySubjectAssignment;
import edu.northwestern.bioinformatics.studycalendar.service.SubjectService;
import edu.northwestern.bioinformatics.studycalendar.xml.domain.NextScheduledStudySegment;
import edu.northwestern.bioinformatics.studycalendar.xml.writers.NextScheduledStudySegmentXmlSerializer;
import edu.northwestern.bioinformatics.studycalendar.xml.writers.ScheduledStudySegmentXmlSerializer;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.Representation;
import org.restlet.resource.ResourceException;
import org.restlet.resource.StringRepresentation;
import org.springframework.beans.factory.annotation.Required;

import java.io.IOException;

/**
 * @author John Dzak
 */
public class ScheduledCalendarResource extends AbstractDomainObjectResource<ScheduledCalendar> {
    private StudySubjectAssignmentDao studySubjectAssignmentDao;
    private StudyDao studyDao;
    private ScheduledStudySegmentXmlSerializer scheduledStudySegmentSerializer;
    private NextScheduledStudySegmentXmlSerializer nextScheduledStudySegmentSerializer;
    private SubjectService subjectService;

    @Override
    public void init(Context context, Request request, Response response) {
        super.init(context, request, response);
        setAuthorizedFor(Method.GET, Role.SUBJECT_COORDINATOR);
        setAuthorizedFor(Method.POST, Role.SUBJECT_COORDINATOR);
    }

    @Override
    public boolean allowPost() { return true; }

    @Override
    protected ScheduledCalendar loadRequestedObject(Request request) {
        String studyIdent = UriTemplateParameters.STUDY_IDENTIFIER.extractFrom(request);
        Study study = studyDao.getByAssignedIdentifier(studyIdent);
        if (study == null) return null;

        String assignmentId = UriTemplateParameters.ASSIGNMENT_IDENTIFIER.extractFrom(request);
        StudySubjectAssignment assignment = studySubjectAssignmentDao.getByGridId(assignmentId);
        if (assignment == null) {
            assignment = studySubjectAssignmentDao.getByStudySubjectIdentifier(study, assignmentId);
        }
        if (assignment != null) {
            return assignment.getScheduledCalendar();
        }
        return null;
    }


    public void acceptRepresentation(final Representation entity) throws ResourceException {
        if (entity.getMediaType() == MediaType.TEXT_XML) {

            NextScheduledStudySegment scheduled;
            try {
                scheduled = nextScheduledStudySegmentSerializer.readDocument(entity.getStream());
            } catch (IOException e) {
                log.warn("PUT failed with IOException", e);
                throw new ResourceException(e);
            } catch (StudyCalendarValidationException exp) {
                throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, exp.getMessage());
            }

            ScheduledStudySegment scheduledSegment = store(scheduled);

            getResponse().setEntity(
                    new StringRepresentation(
                            scheduledStudySegmentSerializer.createDocumentString(scheduledSegment), MediaType.TEXT_XML));

            getResponse().setStatus(Status.SUCCESS_CREATED);

        } else {
            throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST);
        }
    }

    private ScheduledStudySegment store(NextScheduledStudySegment scheduled) {
        ScheduledCalendar cal = getRequestedObject();
        return subjectService.scheduleStudySegment(cal.getAssignment(), scheduled.getStudySegment(), scheduled.getStartDate(), scheduled.getMode());
    }

    ////// Bean setters
    
    @Required
    public void setStudySubjectAssignmentDao(StudySubjectAssignmentDao studySubjectAssignmentDao) {
        this.studySubjectAssignmentDao = studySubjectAssignmentDao;
    }

    @Required
    public void setStudyDao(StudyDao studyDao) {
        this.studyDao = studyDao;
    }

    @Required
    public void setScheduledStudySegmentXmlSerializer(ScheduledStudySegmentXmlSerializer scheduledSegmentSerializer) {
        this.scheduledStudySegmentSerializer = scheduledSegmentSerializer;
    }

    @Required
    public void setNextScheduledStudySegmentXmlSerializer(NextScheduledStudySegmentXmlSerializer nextScheduledStudySegmentSerializer) {
        this.nextScheduledStudySegmentSerializer = nextScheduledStudySegmentSerializer;
    }

    @Required
    public void setSubjectService(SubjectService subjectService) {
        this.subjectService = subjectService;
    }
}
