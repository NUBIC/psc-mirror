package edu.northwestern.bioinformatics.studycalendar.web.template;

import edu.northwestern.bioinformatics.studycalendar.core.Fixtures;
import edu.northwestern.bioinformatics.studycalendar.dao.StudySegmentDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySegment;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Amendment;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole;
import edu.northwestern.bioinformatics.studycalendar.service.DeltaService;
import edu.northwestern.bioinformatics.studycalendar.service.TestingTemplateService;
import edu.northwestern.bioinformatics.studycalendar.web.ControllerTestCase;
import static org.easymock.classextension.EasyMock.expect;
import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.ResourceAuthorization;
import org.springframework.web.servlet.ModelAndView;

import java.util.Collection;

/**
 * @author Rhett Sutphin
 */
public class SelectStudySegmentControllerTest extends ControllerTestCase {
    private static final int STUDY_SEGMENT_ID = 90;

    private SelectStudySegmentController controller;
    private StudySegmentDao studySegmentDao;
    private DeltaService deltaService;

    private StudySegment studySegment;
    private Study study;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        study = Fixtures.createBasicTemplate();
        Fixtures.assignIds(study);
        studySegment = study.getPlannedCalendar().getEpochs().get(0).getStudySegments().get(1);
        studySegment.setId(STUDY_SEGMENT_ID);

        controller = new SelectStudySegmentController();
        studySegmentDao = registerDaoMockFor(StudySegmentDao.class);
        deltaService = registerMockFor(DeltaService.class);

        controller.setStudySegmentDao(studySegmentDao);
        controller.setControllerTools(controllerTools);
        controller.setDeltaService(deltaService);
        controller.setTemplateService(new TestingTemplateService());

        expect(studySegmentDao.getById(STUDY_SEGMENT_ID)).andReturn(studySegment).anyTimes();
        request.setParameter("studySegment", Integer.toString(STUDY_SEGMENT_ID));
        request.setMethod("GET");
    }

    public void testAuthorizedRoles() {
        Collection<ResourceAuthorization> actualAuthorizations = controller.authorizations(null, null);
        assertRolesAllowed(actualAuthorizations, PscRole.valuesWithStudyAccess());
    }

    // TODO: test the inclusion of the plan tree hierarchy
    public void testRequest() throws Exception {
        replayMocks();
        ModelAndView mv = controller.handleRequest(request, response);
        verifyMocks();

        assertEquals("template/ajax/selectStudySegment", mv.getViewName());

        Object actualStudySegment = mv.getModel().get("studySegment");
        assertNotNull("study segment missing", actualStudySegment);
        assertTrue("study segment is not wrapped", actualStudySegment instanceof StudySegmentTemplate);
        System.out.println("mv.getModel " + mv.getModel());
        assertEquals("Wrong model: " + mv.getModel(), 6, mv.getModel().size());
    }

    public void testRequestWhenAmended() throws Exception {
        request.setParameter("developmentRevision", "true");
        request.setParameter("canEdit", "true");
        study.setDevelopmentAmendment(new Amendment("dev"));
        expect(deltaService.revise(studySegment)).andReturn((StudySegment) studySegment.transientClone());
        expect(deltaService.revise(study, study.getDevelopmentAmendment())).andReturn(study);

        replayMocks();
        ModelAndView mv = controller.handleRequest(request, response);
        verifyMocks();

        Object actualStudySegment = mv.getModel().get("studySegment");
        assertNotNull("study segment missing", actualStudySegment);
        assertTrue("study segment is not wrapped", actualStudySegment instanceof StudySegmentTemplate);
        assertNotNull("dev revision missing", mv.getModel().get("developmentRevision"));

        assertEquals("Wrong model: " + mv.getModel(), 7, mv.getModel().size());
    }

    public void testRequestWhenReleasedTemplateIsSelected() throws Exception {
        study.setDevelopmentAmendment(new Amendment("dev"));
        request.setParameter("canEdit", "false");
        replayMocks();
        ModelAndView mv = controller.handleRequest(request, response);
        verifyMocks();

        Object actualStudySegment = mv.getModel().get("studySegment");
        assertNotNull("study segment missing", actualStudySegment);
        assertTrue("study segment is not wrapped", actualStudySegment instanceof StudySegmentTemplate);
        assertNull("must not revise study", mv.getModel().get("developmentRevision"));

        assertEquals("Wrong model: " + mv.getModel(), 6, mv.getModel().size());
    }
}
