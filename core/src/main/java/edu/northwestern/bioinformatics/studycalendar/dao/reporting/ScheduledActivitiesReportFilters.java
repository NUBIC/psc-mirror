package edu.northwestern.bioinformatics.studycalendar.dao.reporting;

import edu.northwestern.bioinformatics.studycalendar.domain.ActivityType;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledActivityMode;
import edu.northwestern.bioinformatics.studycalendar.tools.MutableRange;
import edu.nwu.bioinformatics.commons.CollectionUtils;
import gov.nih.nci.security.authorization.domainobjects.User;

import java.util.Collection;
import java.util.*;

/**
 * @author John Dzak
 */
public class ScheduledActivitiesReportFilters extends ReportFilters {
    private SubstringFilterLimit studyAssignedIdentifier = new SubstringFilterLimit("studyAssignedIdentifier");
    private SubstringFilterLimit siteName = new SubstringFilterLimit("siteName");
    private ControlledVocabularyObjectInListFilterLimit<ScheduledActivityMode> currentStateModes =
        new ControlledVocabularyObjectInListFilterLimit<ScheduledActivityMode>("currentStateModes");
    private RangeFilterLimit<Date> actualActivityDate = new RangeFilterLimit<Date>("actualActivityDate");
    private RangeFilterLimit<Date> idealDate = new RangeFilterLimit<Date>("idealDate");

    private DomainObjectInListFilterLimit<ActivityType> activityTypes
        = new DomainObjectInListFilterLimit<ActivityType>("activityTypes");
    private ResponsibleUserFilterLimit responsibleUser = new ResponsibleUserFilterLimit();
    private StringFilter label = new StringFilter("label");
    private StringFilter personId = new StringFilter("personId");
    private IdentityInListFilterLimit<Integer> authorizedStudySiteIds =
        new IdentityInListFilterLimit<Integer>("authorizedStudySiteIds");

    @Override
    protected String getHibernateFilterPrefix() {
        return "filter_";
    }

    public String getStudyAssignedIdentifier() {
        return studyAssignedIdentifier.getValue();
    }

    public void setStudyAssignedIdentifier(String value) {
        studyAssignedIdentifier.setValue(value);
    }

    public ScheduledActivityMode getCurrentStateMode() {
        if (getCurrentStateModes() == null) {
            return null;
        } else {
            return CollectionUtils.firstElement(getCurrentStateModes());
        }
    }

    public void setCurrentStateModes(Collection<ScheduledActivityMode> mode) {
        currentStateModes.setValue(mode);
    }

    public Collection<ScheduledActivityMode> getCurrentStateModes() {
        return currentStateModes.getValue();
    }

    public String getSiteName() {
        return siteName.getValue();
    }

    public void setSiteName(String value) {
        siteName.setValue(value);
    }

    public MutableRange<Date> getActualActivityDate() {
        MutableRange<Date> range = actualActivityDate.getValue();
        Date stopDate = range.getStop();
        if (stopDate != null) {
            Calendar c1 = Calendar.getInstance();
            c1.setTime(stopDate);
            c1.add(Calendar.DATE, -1);
            range.setStop(c1.getTime());
        }
        return range;
    }

    public void setActualActivityDate(MutableRange<Date> range) {
        Date stopDate = range.getStop();
        // Update the search condition to less than, instead of less than and equal to,
        // To include the stop date with the time
        // So increment the Stop date by 1 day
        if (stopDate != null) {
            Calendar c1 = Calendar.getInstance();
            c1.setTime(stopDate);
            c1.add(Calendar.DATE, 1);
            range.setStop(c1.getTime());
        }
        this.actualActivityDate.setValue(range);
    }

    public MutableRange<Date> getIdealDate() {
        return idealDate.getValue();
    }

    public void setIdealDate(MutableRange<Date> range) {
        this.idealDate.setValue(range);
    }

    public Collection<ActivityType> getActivityTypes() {
        return activityTypes.getValue();
    }

    public void setActivityTypes(Collection<ActivityType> types) {
        activityTypes.setValue(types);
    }

    public User getResponsibleUser() {
        return responsibleUser.getValue();
    }

    public void setResponsibleUser(User responsibleUser) {
        this.responsibleUser.setValue(responsibleUser);
    }

    public String getLabel() {
        return label.getValue();
    }

    public void setLabel(String value) {
        label.setValue(value);
    }

    public String getPersonId() {
        return personId.getValue();
    }

    public void setPersonId(String value) {
        personId.setValue(value);
    }

    public Collection<Integer> getAuthorizedStudySiteIds() {
        return authorizedStudySiteIds.getValue();
    }

    public void setAuthorizedStudySiteIds(Collection<Integer> ids) {
        this.authorizedStudySiteIds.setValue(ids);
    }

    //////

    private class ResponsibleUserFilterLimit extends SingleFilterFilterLimit<Long, User> {
        private ResponsibleUserFilterLimit() {
            super("responsibleUser");
        }

        @Override
        protected Long getValueForFilter() {
            return getValue().getUserId();
        }
    }
}
