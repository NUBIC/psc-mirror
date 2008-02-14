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
    SUBJECT_ASSIGNMENTS("subject-assignments"),
    SUBJECT_ASSIGNMENT("subject-assignment"),
    SUBJECT,
    SITE,
    SITES,
    STUDY_SITE_LINK("study-site-link"),
    STUDIES,
    STUDY,
    DEVELOPMENT_AMENDMENT("development-amendment"),
    AMENDMENT,
    SCHEDULED_CALENDARS("scheduled-calendars"),
    SCHEDULED_CALENDAR("scheduled-calendar");

    private String elementName;

    XsdElement() {
        this(null);
    }

    XsdElement(String elementName) {
        this.elementName = elementName == null ? name().toLowerCase() : elementName;
    }

    public String xmlName() {
        return elementName;
    }

    public Element create() {
        QName qNode = QName.get(xmlName(), AbstractStudyCalendarXmlSerializer.PSC_NS);
        return DocumentHelper.createElement(qNode);
    }

}
