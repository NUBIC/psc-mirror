package edu.northwestern.bioinformatics.studycalendar.domain;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Sort;
import org.hibernate.annotations.SortType;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.util.SortedSet;
import java.util.TreeSet;

import edu.northwestern.bioinformatics.studycalendar.utils.DefaultDayRange;
import edu.northwestern.bioinformatics.studycalendar.utils.DayRange;
import edu.northwestern.bioinformatics.studycalendar.utils.EmptyDayRange;
import gov.nih.nci.cabig.ctms.domain.AbstractMutableDomainObject;

/**
 * @author Rhett Sutphin
 */
@Entity
@Table (name = "arms")
@GenericGenerator(name="id-generator", strategy = "native",
    parameters = {
        @Parameter(name="sequence", value="seq_arms_id")
    }
)
public class Arm extends AbstractMutableDomainObject implements Named {
    private Epoch epoch;
    private String name;
    private SortedSet<Period> periods = new TreeSet<Period>();

    // business methods

    public void addPeriod(Period period) {
        periods.add(period);
        period.setArm(this);
    }

    @Transient
    public String getQualifiedName() {
        StringBuilder sb = new StringBuilder();
        sb.append(epoch.getName());
        if (epoch.isMultipleArms()) {
            sb.append(": ").append(getName());
        }
        return sb.toString();
    }

    @Transient
    public int getLengthInDays() {
        return getDayRange().getDayCount();
    }

    @Transient
    public DayRange getDayRange() {
        if (getPeriods().size() == 0) {
            return EmptyDayRange.INSTANCE;
        } else {
            DefaultDayRange range = new DefaultDayRange(Integer.MAX_VALUE, Integer.MIN_VALUE);
            for (Period period : getPeriods()) {
                range.add(period.getTotalDayRange());
            }
            return range;
        }
    }

    // bean methods

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    // This is annotated this way so that the IndexColumn in the parent
    // will work with the bidirectional mapping
    @ManyToOne (fetch = FetchType.LAZY)
    @JoinColumn(insertable=false, updatable=false, nullable=false)
    public Epoch getEpoch() {
        return epoch;
    }

    public void setEpoch(Epoch epoch) {
        this.epoch = epoch;
    }

    @OneToMany (mappedBy = "arm")
    @Cascade (value = { CascadeType.ALL, CascadeType.DELETE_ORPHAN })
    @Sort (type = SortType.NATURAL)
    public SortedSet<Period> getPeriods() {
        return periods;
    }

    public void setPeriods(SortedSet<Period> periods) {
        this.periods = periods;
    }
}
