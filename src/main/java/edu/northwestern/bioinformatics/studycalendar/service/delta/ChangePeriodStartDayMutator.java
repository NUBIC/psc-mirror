package edu.northwestern.bioinformatics.studycalendar.service.delta;

import edu.northwestern.bioinformatics.studycalendar.domain.delta.PropertyChange;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledCalendar;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledArm;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledEvent;
import edu.northwestern.bioinformatics.studycalendar.service.TemplateService;
import edu.northwestern.bioinformatics.studycalendar.service.ScheduleService;

/**
 * @author Rhett Sutphin
 */
public class ChangePeriodStartDayMutator extends AbstractPeriodPropertyChangeMutator {
    private ScheduleService scheduleService;
    private int shiftAmount;

    public ChangePeriodStartDayMutator(
        PropertyChange change, TemplateService templateService, ScheduleService scheduleService
    ) {
        super(change, templateService);
        this.scheduleService = scheduleService;
        shiftAmount = Integer.parseInt(change.getNewValue()) - Integer.parseInt(change.getOldValue());
    }

    @Override
    public void apply(ScheduledCalendar calendar) {
        for (ScheduledArm scheduledArm : getScheduledArmsToMutate(calendar)) {
            for (ScheduledEvent event : scheduledArm.getEvents()) {
                if (getChangedPeriod().equals(templateService.findParent(event.getPlannedActivity()))) {
                    scheduleService.reviseDate(event, shiftAmount, change.getDelta().getRevision());
                }
            }
        }
    }

}
