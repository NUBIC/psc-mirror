package edu.northwestern.bioinformatics.studycalendar.domain;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.ManyToOne;
import javax.persistence.JoinColumn;
import javax.persistence.Transient;
import javax.persistence.OneToMany;
import java.util.List;
import java.util.ArrayList;

/**
 * @author Rhett Sutphin
 */
@Entity
@Table (name = "epochs")
@GenericGenerator(name="id-generator", strategy = "native",
    parameters = {
        @Parameter(name="sequence", value="seq_epochs_id")
    }
)
public class Epoch extends AbstractDomainObject implements Named {
    private PlannedCalendar plannedCalendar;
    private List<Arm> arms = new ArrayList<Arm>();
    private String name;

    ////// LOGIC

    public void addArm(Arm arm) {
        arms.add(arm);
        arm.setEpoch(this);
    }

    @Transient
    public int getLengthInDays() {
        int len = 0;
        for (Arm arm : getArms()) {
            len = Math.max(len, arm.getLengthInDays());
        }
        return len;
    }

    @Transient
    public boolean isMultipleArms() {
        return getArms().size() > 1;
    }

    ////// BEAN PROPERTIES

    @OneToMany (mappedBy = "epoch")
    @Cascade (value = { CascadeType.ALL, CascadeType.DELETE_ORPHAN })
    public List<Arm> getArms() {
        return arms;
    }

    public void setArms(List<Arm> arms) {
        this.arms = arms;
    }

    @ManyToOne
    @JoinColumn (name = "planned_calendar_id")
    public PlannedCalendar getPlannedCalendar() {
        return plannedCalendar;
    }

    public void setPlannedCalendar(PlannedCalendar plannedCalendar) {
        this.plannedCalendar = plannedCalendar;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
