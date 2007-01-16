package edu.northwestern.bioinformatics.studycalendar.domain;

import java.util.Date;
import java.util.List;
import java.util.LinkedList;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.OneToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Transient;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;


/**
 * @author Ram Chilukuri
 */

@Entity
@Table (name = "participant_assignments")
@GenericGenerator(name="id-generator", strategy = "native",
    parameters = {
        @Parameter(name="sequence", value="seq_participant_assignments_id")
    }
)
public class StudyParticipantAssignment extends AbstractDomainObject {
    private String studyId;
    private StudySite studySite;
    private Participant participant;
    private Date startDateEpoch;

    private ScheduledCalendar scheduledCalendar;
    private List<AdverseEventNotification> aeNotifications = new LinkedList<AdverseEventNotification>();

    ////// LOGIC

    @Transient
    public List<AdverseEventNotification> getCurrentAeNotifications() {
        List<AdverseEventNotification> aes = new LinkedList<AdverseEventNotification>();
        for (AdverseEventNotification notification : getAeNotifications()) {
            if (!notification.isDismissed()) aes.add(notification);
        }
        return aes;
    }

    public void addAeNotification(AdverseEventNotification notification) {
        getAeNotifications().add(notification);
        notification.setAssignment(this);
    }

    ////// BEAN PROPERTIES

    public void setStudySite(StudySite studySite) {
        this.studySite = studySite;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "study_site_id")
    public StudySite getStudySite() {
        return studySite;
    }

    public void setParticipant(Participant participant) {
        this.participant = participant;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "participant_id")
    public Participant getParticipant() {
        return participant;
    }

    public void setStartDateEpoch(Date startDateEpoch) {
        this.startDateEpoch = startDateEpoch;
    }

    @Column(name = "first_epoch_stdate")
    public Date getStartDateEpoch() {
        return startDateEpoch;
    }

    @OneToOne (mappedBy = "assignment")
    @Cascade(value = CascadeType.ALL)
    public ScheduledCalendar getScheduledCalendar() {
        return scheduledCalendar;
    }

    public void setScheduledCalendar(ScheduledCalendar scheduledCalendar) {
        this.scheduledCalendar = scheduledCalendar;
        if (scheduledCalendar != null) {
            scheduledCalendar.setAssignment(this);
        }
    }

    @OneToMany(mappedBy = "assignment")
    @Cascade(CascadeType.ALL)
    public List<AdverseEventNotification> getAeNotifications() {
        return aeNotifications;
    }

    public void setAeNotifications(List<AdverseEventNotification> aeNotifications) {
        this.aeNotifications = aeNotifications;
    }

    public String getStudyId() {
        return studyId;
    }

    public void setStudyId(String studyId) {
        this.studyId = studyId;
    }

    ////// OBJECT METHODS

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final StudyParticipantAssignment that = (StudyParticipantAssignment) o;

        if (startDateEpoch != null ? !startDateEpoch.equals(that.startDateEpoch) : that.startDateEpoch != null)
            return false;
        if (studySite != null ? !studySite.equals(that.studySite) : that.studySite != null)
            return false;
        // Participant#equals calls this method, so we can't use it here
        if (!AbstractDomainObject.equalById(participant, that.participant)) return false;

        return true;
    }

    public int hashCode() {
        int result;
        result = (studySite != null ? studySite.hashCode() : 0);
        result = 29 * result + (participant != null ? participant.hashCode() : 0);
        result = 29 * result + (startDateEpoch != null ? startDateEpoch.hashCode() : 0);
        return result;
    }
}
