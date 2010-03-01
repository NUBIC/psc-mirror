package edu.northwestern.bioinformatics.studycalendar.domain;

import edu.northwestern.bioinformatics.studycalendar.domain.tools.Differences;
import gov.nih.nci.cabig.ctms.domain.AbstractMutableDomainObject;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "sources")
@GenericGenerator(name = "id-generator", strategy = "native",
        parameters = {
                @Parameter(name = "sequence", value = "seq_sources_id")
        }
)
public class Source extends AbstractMutableDomainObject
        implements Named, NaturallyKeyed, TransientCloneable<Source> {
    private String name;
    private List<Activity> activities = new ArrayList<Activity>();
    private boolean memoryOnly;

    ////// LOGIC

    @Transient
    public String getNaturalKey() {
        return getName();
    }

    public void addActivity(Activity activity) {
        getActivities().add(activity);
        activity.setSource(this);
    }

    @Transient
    public boolean isMemoryOnly() {
        return memoryOnly;
    }

    public void setMemoryOnly(boolean memoryOnly) {
        this.memoryOnly = memoryOnly;
    }

    public Source transientClone() {
        Source clone = new Source();
        clone.setName(getName());
        clone.setMemoryOnly(true);
        return clone;
    }

    ////// BEAN PROPERTIES

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @OneToMany(mappedBy = "source")
    @Cascade(value = {CascadeType.ALL})
    @OrderBy
    // ensure consistent ordering
    public List<Activity> getActivities() {
        return activities;
    }

    public void setActivities(List<Activity> activities) {
        this.activities = activities;
    }

    public Differences deepEquals(Object o) {
        Differences differences =  new Differences();
        if (this == o) return differences;

        if (o == null || getClass() != o.getClass()) {
            differences.addMessage("not an instance of source");
        }

        Source source = (Source) o;

        if (name != null ? !name.equals(source.name) : source.name != null) {
            differences.addMessage(String.format("Source name %s differs to %s", name, source.name));
        }

        return differences;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Source source = (Source) o;

        return !(name != null ? !name.equals(source.name) : source.name != null);

    }

    public int hashCode() {
        return (name != null ? name.hashCode() : 0);
    }


    /**
     * DO NOT CALLL THIS METHOD DIRECTLY. instead use SourceService#updateSource
     * <p>  add new activities  to the source. It does  update/delete any activitiy.
     * <li>
     * Add any activities that do not already exist.   </li>
     * </p>
     *
     * @param activities activities which may be added
     */
    @Transient
    public void addNewActivities(final List<Activity> activities) {
        List<Activity> existingActivities = getActivities();
        for (Activity activity : activities) {
            // check for new activity
            Activity existingActivity = activity.findActivityInCollectionWhichHasSameCode(existingActivities);
            if (existingActivity == null) {
                this.addActivity(activity);

            }
        }
    }
}
