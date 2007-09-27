package edu.northwestern.bioinformatics.studycalendar.domain;

import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;
import edu.northwestern.bioinformatics.studycalendar.domain.scheduledeventstate.Scheduled;
import edu.nwu.bioinformatics.commons.DateUtils;

import java.util.Calendar;
import java.util.List;

public class ScheduledEventModeTest extends StudyCalendarTestCase {

    public void testScheduledGetAvailableModesPos() throws Exception {
        Scheduled scheduledState = new Scheduled("Scheduled", DateUtils.createDate(2007, Calendar.SEPTEMBER, 1));
        List<ScheduledEventMode> modes = ScheduledEventMode.getAvailableModes(scheduledState, true);
        assertEquals("Wrong size of available modes", 5, modes.size());
    }

    public void testScheduledGetAvailableModesNeg() throws Exception {
        Scheduled scheduledState = new Scheduled("Scheduled", DateUtils.createDate(2007, Calendar.SEPTEMBER, 1));
        List<ScheduledEventMode> modes = ScheduledEventMode.getAvailableModes(scheduledState, false);
        assertEquals("Wrong size of available modes", 3, modes.size());
    }
}
