package edu.northwestern.bioinformatics.studycalendar.domain;

import gov.nih.nci.cabig.ctms.domain.AbstractMutableDomainObject;
import org.hibernate.annotations.*;
import org.springframework.beans.BeanUtils;
import org.apache.commons.logging.Log;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import javax.persistence.*;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OrderBy;
import javax.persistence.Table;


/**
 * @author Nataliya Shurupova
 */

@Entity
@Table(name = "holidays")
@GenericGenerator(name = "id-generator", strategy = "native",
        parameters = {
        @Parameter(name = "sequence", value = "seq_holidays_id")
                }
)
@DiscriminatorColumn(name = "discriminator_id", discriminatorType = DiscriminatorType.INTEGER)
public class BlackoutDate extends AbstractMutableDomainObject {
    private String description;
    private Site site;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setSite(Site site) {
        this.site = site;
    }

    @ManyToOne
    @JoinColumn(name = "site_id")
    public Site getSite() {
        return site;
    }

    protected int mapDayNameToInteger(String dayName) {
        Weekday match = Weekday.findByName(dayName);
        return match == null ? -1 : match.ordinal() + 1;
    }

    @Transient
    public void mergeAnotherHoliday(final BlackoutDate anotherBlackoutDate) {
        BeanUtils.copyProperties(anotherBlackoutDate, this, new String[]{"id"});
    }

    private static enum Weekday {
        SUNDAY,
        MONDAY,
        TUESDAY,
        WEDNESDAY,
        THURSDAY,
        FRIDAY,
        SATURDAY;

        static Weekday findByName(String name) {
            for (Weekday weekday : values()) {
                if (weekday.name().equals(name.toUpperCase())) return weekday;
            }
            return null;
        }
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append(" Description = ");
        sb.append(getDescription());
        return sb.toString();
    }
}
