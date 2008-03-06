package edu.northwestern.bioinformatics.studycalendar.xml;

import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.QName;

/**
 * Enum for the names of elements defined in psc.xsd.
 *
 * @author Rhett Sutphin
 */
public enum XsdElement {
    ACTIVITY,
    ACTIVITY_SOURCES("sources"),
    ACTIVITY_SOURCE("source"),
    REGISTRATION,
    REGISTRATIONS,
    SUBJECT_ASSIGNMENTS,
    SUBJECT_ASSIGNMENT,
    SUBJECT,
    SITE,
    SITES,
    STUDY_SITE_LINK,
    STUDIES,
    STUDY,
    DEVELOPMENT_AMENDMENT,
    AMENDMENT,
    BLACKOUT_DATE,
    BLACKOUT_DATES,
    SCHEDULED_CALENDARS,
    SCHEDULED_CALENDAR,
    SCHEDULED_STUDY_SEGMENT,
    SCHEDULED_ACTIVITY,
    CURRENT_SCHEDULED_ACTIVITY_STATE,
    PREVIOUS_SCHEDULED_ACTIVITY_STATE,
    AMENDMENT_APPROVAL,
    AMENDMENT_APPROVALS;

    private String elementName;

    XsdElement() {
        this(null);
    }

    XsdElement(String elementName) {
        this.elementName = elementName == null ? name().replaceAll("_", "-").toLowerCase() : elementName;
    }

    public String xmlName() {
        return elementName;
    }

    public Element create() {
        QName qNode = QName.get(xmlName(), AbstractStudyCalendarXmlSerializer.PSC_NS);
        return DocumentHelper.createElement(qNode);
    }

}
