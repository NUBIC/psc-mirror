package edu.northwestern.bioinformatics.studycalendar.domain;

import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.createAmendment;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Amendment;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.AmendmentApproval;
import edu.northwestern.bioinformatics.studycalendar.utils.FormatTools;
import edu.nwu.bioinformatics.commons.DateUtils;
import edu.nwu.bioinformatics.commons.testing.CoreTestCase;

import java.util.Calendar;
import java.util.Date;

/**
 * @author Saurabh Agrawal
 */
public class NotificationTest extends CoreTestCase {

    private Notification notification;

    private AdverseEvent adverseEvent;
    private Date detectionDate;
    private AmendmentApproval amendmentApproval;

    private ScheduledActivity scheduledActivity;

    private Amendment amendment;
    private StudySite studySite;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        adverseEvent = new AdverseEvent();
        adverseEvent.setDescription("desc");
        detectionDate = DateUtils.createDate(2007, Calendar.SEPTEMBER, 2);
        adverseEvent.setDetectionDate(detectionDate);
        scheduledActivity = Fixtures.createScheduledActivity("sch activity", 2008, 2, 3);

        amendment = createAmendment("amendment", new Date(), false);
        amendment.setId(2);

        amendmentApproval = new AmendmentApproval();


        final Study study = new Study();
        study.setId(3);
        studySite = Fixtures.createStudySite(study, new Site());
    }

    public void testCrateNotificationForAdverseEvent() {

        notification = new Notification(adverseEvent);

        String expectedTitle = "Serious Adverse Event on " + FormatTools.formatDate(detectionDate);
        assertEquals(expectedTitle, notification.getTitle());
        assertTrue("action is required", notification.isActionRequired());
        assertFalse(notification.isDismissed());
        assertEquals("desc", notification.getMessage());
        assertNull(notification.getAssignment());

    }

    public void testCrateNotificationForReconsents() {
        scheduledActivity.setId(2);
        notification = new Notification(scheduledActivity);

        String expectedTitle = "Reconsent scheduled for " + FormatTools.formatDate(new Date());

        assertEquals(expectedTitle, notification.getTitle());
        assertTrue("action is required", notification.isActionRequired());
        assertFalse(notification.isDismissed());
        assertEquals("/pages/cal/scheduleActivity?event=2", notification.getMessage());
        assertNull(notification.getAssignment());

    }

    public void testCrateNotificationWhenAmendmentIsApproved() {
        amendmentApproval.setAmendment(amendment);
        amendmentApproval.setStudySite(studySite);
        notification = new Notification(amendmentApproval);

        String expectedTitle = "Schedule amended according to " + amendment.getDisplayName();

        assertEquals(expectedTitle, notification.getTitle());
        assertFalse("action is not required", notification.isActionRequired());
        assertFalse(notification.isDismissed());
        assertEquals("/pages/cal/template/amendments?study=3#amendment=2", notification.getMessage());
        assertNull(notification.getAssignment());

    }
}
