package edu.northwestern.bioinformatics.studycalendar.utils.breadcrumbs;

import edu.northwestern.bioinformatics.studycalendar.domain.Arm;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedCalendar;
import edu.northwestern.bioinformatics.studycalendar.domain.Epoch;
import edu.northwestern.bioinformatics.studycalendar.domain.Period;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedEvent;
import edu.northwestern.bioinformatics.studycalendar.service.TemplateService;
import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;
import static org.easymock.classextension.EasyMock.*;

/**
 * @author Rhett Sutphin
 */
public class BreadcrumbContextTest extends StudyCalendarTestCase {
    private TemplateService templateService;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        templateService = registerMockFor(TemplateService.class);
    }

    public void testSetPlannedElementsUsesTemplateServiceToResolveAncestors() throws Exception {
        PlannedCalendar plannedCalendar = new PlannedCalendar();
        Epoch epoch = new Epoch();
        Arm arm = new Arm();
        Period period = new Period();
        PlannedEvent plannedEvent = new PlannedEvent();
        expect(templateService.findParent(plannedEvent)).andReturn(period);
        expect(templateService.findParent(period)).andReturn(arm);
        expect(templateService.findParent(arm)).andReturn(epoch);
        expect(templateService.findParent(epoch)).andReturn(plannedCalendar);

        replayMocks();
        BreadcrumbContext context = BreadcrumbContext.create(plannedEvent, templateService);
        verifyMocks();

        assertSame(plannedEvent, context.getPlannedEvent());
        assertSame(period, context.getPeriod());
        assertSame(arm, context.getArm());
        assertSame(epoch, context.getEpoch());
        assertSame(plannedCalendar, context.getPlannedCalendar());
    }
}
