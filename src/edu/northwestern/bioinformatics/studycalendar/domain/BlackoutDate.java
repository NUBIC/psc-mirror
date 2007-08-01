package edu.northwestern.bioinformatics.studycalendar.domain;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;

import javax.persistence.Entity;
import javax.persistence.Table;

import gov.nih.nci.cabig.ctms.domain.AbstractMutableDomainObject;


/**
 * @author Nataliya Shurupova
 */

@Entity
@Table (name = "holidays")
@GenericGenerator(name="id-generator", strategy = "native",
    parameters = {
        @Parameter(name="sequence", value="seq_holidays_id")
    }
)
@DiscriminatorColumn(name="discriminator_id", discriminatorType = DiscriminatorType.INTEGER)
public class BlackoutDate extends AbstractMutableDomainObject {
    private String description;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    protected int mapDayNameToInteger(String dayName) {
        Weekday match = Weekday.findByName(dayName);
        return match == null ? -1 : match.ordinal() + 1;
    }

    private static enum Weekday {
        SUNDAY,
        MONDAY,
        TUESDAY,
        WEDNESDAY,
        THURSDAY,
        FRIDAY,
        SATURDAY
        ;

        static Weekday findByName(String name) {
            for (Weekday weekday : values()) {
                if (weekday.name().equals(name.toUpperCase())) return weekday;
            }
            return null;
        }
    }

    @Override
    public String toString(){
        StringBuffer sb = new StringBuffer();
        sb.append(" Description = ");
        sb.append(getDescription());
        return sb.toString();
    }
}

