package edu.northwestern.bioinformatics.studycalendar.service.delta;

import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Remove;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Delta;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Amendment;
import edu.northwestern.bioinformatics.studycalendar.domain.*;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.createAmendments;
import edu.northwestern.bioinformatics.studycalendar.dao.PlannedActivityDao;
import gov.nih.nci.cabig.ctms.lang.DateTools;

import java.util.Calendar;

/**
 * @author Rhett Sutphin
 */
public class RemovePlannedActivityMutatorTest extends StudyCalendarTestCase {
    private RemovePlannedActivityMutator mutator;

    private Remove remove;
    private Delta<?> delta;
    private Amendment amendment;

    private PlannedActivity pe0, pe1, pe2;
    ScheduledCalendar scheduledCalendar;
    ScheduledArm scheduledArm;

    private PlannedActivityDao plannedActivityDao;
    private ScheduledActivity pe0se0, pe1se0, pe1se1, pe2se0, pe0se1;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        pe0 = Fixtures.createPlannedActivity("E0", 2);
        pe1 = Fixtures.createPlannedActivity("E1", 8);
        pe2 = Fixtures.createPlannedActivity("E2", 4);

        remove = Remove.create(pe1);
        delta = Delta.createDeltaFor(new Period(), remove);
        amendment = createAmendments("Oops");
        amendment.setDate(DateTools.createDate(1922, Calendar.SEPTEMBER, 1));
        amendment.addDelta(delta);

        scheduledCalendar = new ScheduledCalendar();
        scheduledArm = new ScheduledArm();
        scheduledArm.addEvent(pe0se0 = createUnschedulableMockEvent(pe0));
        scheduledArm.addEvent(pe1se0 = createUnschedulableMockEvent(pe1));
        scheduledArm.addEvent(pe1se1 = createUnschedulableMockEvent(pe1));
        scheduledArm.addEvent(pe2se0 = createUnschedulableMockEvent(pe2));
        scheduledArm.addEvent(pe0se1 = createUnschedulableMockEvent(pe0));

        plannedActivityDao = registerDaoMockFor(PlannedActivityDao.class);
        mutator = new RemovePlannedActivityMutator(remove, plannedActivityDao);
    }

    public void testAppliesToLiveSchedules() throws Exception {
        assertTrue(mutator.appliesToExistingSchedules());
    }

    public void testOnlyApplicableScheduledEventsUnscheduled() throws Exception {
        scheduledCalendar.addArm(scheduledArm);

        String expectedMessage = "Removed in revision 09/01/1922 (Oops)";
        pe1se0.unscheduleIfOutstanding(expectedMessage);
        pe1se1.unscheduleIfOutstanding(expectedMessage);

        replayMocks();
        mutator.apply(scheduledCalendar);
        verifyMocks();
    }

    private ScheduledActivity createUnschedulableMockEvent(PlannedActivity event) throws NoSuchMethodException {
        ScheduledActivity semimock = registerMockFor(ScheduledActivity.class,
            ScheduledActivity.class.getMethod("unscheduleIfOutstanding", String.class));
        semimock.setPlannedActivity(event);
        return semimock;
    }
}

