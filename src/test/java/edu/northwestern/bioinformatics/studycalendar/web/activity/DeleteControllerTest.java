package edu.northwestern.bioinformatics.studycalendar.web.activity;

import edu.northwestern.bioinformatics.studycalendar.web.ControllerTestCase;
import edu.northwestern.bioinformatics.studycalendar.dao.ActivityDao;
import edu.northwestern.bioinformatics.studycalendar.dao.PlannedActivityDao;
import edu.northwestern.bioinformatics.studycalendar.service.ActivityService;
import edu.northwestern.bioinformatics.studycalendar.domain.*;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.setId;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.createNamedInstance;

import java.util.Map;
import java.util.List;
import java.util.ArrayList;

import static org.easymock.EasyMock.expect;

public class DeleteControllerTest extends ControllerTestCase {

    private ActivityDao activityDao;
    private PlannedActivityDao plannedActivityDao;
    private ActivityService activityService;
    private DeleteController controller;

    private Activity a0, a1, a2;
    private Source source;


    @Override
    protected void setUp() throws Exception {
        super.setUp();
        controller = new DeleteController();
        activityDao = registerDaoMockFor(ActivityDao.class);
        plannedActivityDao = registerDaoMockFor(PlannedActivityDao.class);
        activityService = registerMockFor(ActivityService.class);

        controller.setActivityDao(activityDao);
        controller.setPlannedActivityDao(plannedActivityDao);
        controller.setActivityService(activityService);

        source = setId(11, createNamedInstance("Test Source", Source.class));

        a0 = Fixtures.createActivity("Activity 0", "code0", source, ActivityType.INTERVENTION);
        a0.setId(10);

        a1 = Fixtures.createActivity("Activity 1", "code1", source, ActivityType.LAB_TEST);
        a1.setId(20);

        a2 = Fixtures.createActivity("Activity 2", "code2", source, ActivityType.DISEASE_MEASURE);
        a2.setId(30);

        PlannedActivity pa = Fixtures.createPlannedActivity(a2.getName(), 2);
    }

    @SuppressWarnings({ "unchecked" })
    public void testModelWithNoErorMessage() throws Exception {
        request.setParameter("activityId", "10");
        Map<String, Object> actualModel;
        expect(activityDao.getById(a0.getId())).andReturn(a0).anyTimes();
        expect(activityService.deleteActivity(a0)).andReturn(true).anyTimes();
        List<Activity> activityList = new ArrayList<Activity>();
        activityList.add(a1);
        expect(activityDao.getBySourceId(source.getId())).andReturn(activityList).anyTimes();
        List<PlannedActivity> plannedActivityList = new ArrayList<PlannedActivity>();
        expect(plannedActivityDao.getPlannedActivitiesForAcivity(a1.getId())).andReturn(plannedActivityList).anyTimes();

        replayMocks();
        actualModel = controller.handleRequestInternal(request, response).getModel();
        verifyMocks();

        assertTrue("Missing model object", actualModel.containsKey("enableDeletes"));
        assertTrue("Missing model object", actualModel.containsKey("activitiesPerSource"));
        assertTrue("Missing model object", actualModel.containsKey("activityTypes"));

    }


    @SuppressWarnings({ "unchecked" })
    public void testModelWithErorMessage() throws Exception {
        request.setParameter("activityId", "30");
        Map<String, Object> actualModel;
        expect(activityDao.getById(a2.getId())).andReturn(a2).anyTimes();
        expect(activityService.deleteActivity(a2)).andReturn(false).anyTimes();
        List<Activity> activityList = new ArrayList<Activity>();
        activityList.add(a1);
        expect(activityDao.getBySourceId(source.getId())).andReturn(activityList).anyTimes();
        List<PlannedActivity> plannedActivityList = new ArrayList<PlannedActivity>();
        expect(plannedActivityDao.getPlannedActivitiesForAcivity(a1.getId())).andReturn(plannedActivityList).anyTimes();

        replayMocks();
        actualModel = controller.handleRequestInternal(request, response).getModel();
        verifyMocks();

        assertTrue("Missing model error object", actualModel.containsKey("error"));
        assertTrue("Missing model object", actualModel.containsKey("enableDeletes"));
        assertTrue("Missing model object", actualModel.containsKey("activitiesPerSource"));
        assertTrue("Missing model object", actualModel.containsKey("activityTypes"));
    }
}
