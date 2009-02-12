package edu.northwestern.bioinformatics.studycalendar.domain.scheduledactivitystate;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarError;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledActivityMode;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;
import java.util.ArrayList;
import java.util.Date;
import java.text.SimpleDateFormat;

import org.hibernate.annotations.Type;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.apache.commons.lang.StringUtils;
import gov.nih.nci.cabig.ctms.domain.AbstractMutableDomainObject;

/**
 * @author Rhett Sutphin
 */
@Entity // This isn't really an entity, but the @OneToMany from ScheduledActivity doesn't work otherwise
@GenericGenerator(name="id-generator", strategy = "native",
    parameters = {
        @Parameter(name="sequence", value="seq_scheduled_activity_stat_id")
    }
)
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@Table(name = "scheduled_activity_states")
@DiscriminatorColumn(name = "mode_id", discriminatorType = DiscriminatorType.INTEGER)
public abstract class ScheduledActivityState extends AbstractMutableDomainObject implements Cloneable, Serializable {
    private String reason;
    private Date date;

    protected ScheduledActivityState() { }

    protected ScheduledActivityState(String reason, Date date) {
        this.reason = reason;
        this.date = date;
    }



    ////// LOGIC

    @Transient
    public String getTextSummary() {
        StringBuilder sb = new StringBuilder();
        sb.append(StringUtils.capitalize(getMode().getName()));
        appendSummaryMiddle(sb);
        if (getReason() != null) sb.append(" - ").append(getReason());
        return sb.toString();
    }

    @Transient
    protected void appendSummaryMiddle(StringBuilder sb) {
        sb.append(' ');
        appendPreposition(sb);
        sb.append(' ');
        // TODO: centrally configure date format
        if (getDate() != null) sb.append(new SimpleDateFormat("M/d/yyyy").format(getDate()));
    }

    protected abstract void appendPreposition(StringBuilder sb);


    @Transient
    public List<Class<? extends ScheduledActivityState>> getAvailableConditionalStates(boolean conditional) {
        List<Class<? extends ScheduledActivityState>> list = new ArrayList<Class<? extends ScheduledActivityState>>();
        if (conditional) {
            list.add(Conditional.class);
            list.add(NotApplicable.class);
        }
        return list;
    }

    @Transient
    public abstract List<Class<? extends ScheduledActivityState>> getAvailableStates(boolean conditional);

    ////// BEAN PROPERTIES

    @Type(type = "scheduledActivityMode")
    @Column(name = "mode_id", insertable = false, updatable = false)
    public abstract ScheduledActivityMode getMode();
    void setMode(ScheduledActivityMode mode) { /* for hibernate; value ignored */ }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
    @Column(name = "actual_date")
    @Temporal(TemporalType.DATE)
    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    ////// OBJECT METHODS

    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            throw new StudyCalendarError("It is cloneable", e);
        }
    }


    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ScheduledActivityState that = (ScheduledActivityState) o;

        if (reason != null ? !reason.equals(that.reason) : that.reason != null) return false;

        return true;
    }

    public int hashCode() {
        return (reason != null ? reason.hashCode() : 0);
    }
}