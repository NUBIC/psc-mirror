package edu.northwestern.bioinformatics.studycalendar.service.delta;

import edu.northwestern.bioinformatics.studycalendar.domain.delta.PropertyChange;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledCalendar;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledStudySegment;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledActivity;
import edu.northwestern.bioinformatics.studycalendar.service.TemplateService;
import edu.northwestern.bioinformatics.studycalendar.service.ScheduleService;

/**
 * @author Rhett Sutphin
 */
abstract class AbstractChangePeriodDurationMutator extends AbstractPeriodPropertyChangeMutator {
    protected ScheduleService scheduleService;

    public AbstractChangePeriodDurationMutator(PropertyChange change, TemplateService templateService, ScheduleService scheduleService) {
        super(change, templateService);
        this.scheduleService = scheduleService;
    }

    protected abstract int durationChangeInDays();

    @Override
    public void apply(ScheduledCalendar calendar) {
        for (ScheduledStudySegment studySegment : getScheduledStudySegmentsToMutate(calendar)) {
            for (ScheduledActivity event : studySegment.getEvents()) {
                if (getChangedPeriod().equals(templateService.findParent(event.getPlannedActivity()))) {
                    scheduleService.reviseDate(event, durationChangeInDays() * event.getRepetitionNumber(),
                        change.getDelta().getRevision());
                }
            }
        }
    }
}
