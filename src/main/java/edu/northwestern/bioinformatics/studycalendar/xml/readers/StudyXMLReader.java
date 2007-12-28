package edu.northwestern.bioinformatics.studycalendar.xml.readers;

import static java.lang.Boolean.valueOf;

import static edu.northwestern.bioinformatics.studycalendar.xml.writers.StudyXMLWriter.PLANNDED_CALENDAR;
import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.dao.PlannedCalendarDao;
import edu.northwestern.bioinformatics.studycalendar.dao.EpochDao;
import edu.northwestern.bioinformatics.studycalendar.dao.delta.AmendmentDao;
import edu.northwestern.bioinformatics.studycalendar.dao.delta.DeltaDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedCalendar;
import edu.northwestern.bioinformatics.studycalendar.domain.Named;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Amendment;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Delta;
import static edu.northwestern.bioinformatics.studycalendar.xml.writers.StudyXMLWriter.ASSIGNED_IDENTIFIER;
import static edu.northwestern.bioinformatics.studycalendar.xml.writers.StudyXMLWriter.ID;
import edu.northwestern.bioinformatics.studycalendar.xml.writers.StudyXMLWriter;
import edu.northwestern.bioinformatics.studycalendar.StudyCalendarError;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.util.List;
import java.util.ArrayList;
import java.text.SimpleDateFormat;

import gov.nih.nci.cabig.ctms.domain.GridIdentifiable;

public class StudyXMLReader  {
    private StudyDao studyDao;
    private DeltaDao deltaDao;
    private AmendmentDao amendmentDao;
    private PlannedCalendarDao plannedCalendarDao;
    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
    private EpochDao epochDao;

    public Study read(InputStream dataFile) throws Exception {
        //get the factory
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

        //Using factory get an instance of document builder
        DocumentBuilder db = dbf.newDocumentBuilder();

        //parse using builder to get DOM representation of the XML file
        Document dom = db.parse(dataFile);

        return parseStudy(dom);
    }

    protected Study parseStudy(Document doc) throws SAXException {
        Element element = doc.getDocumentElement();
        String gridId = element.getAttribute(ID);
        Study study = studyDao.getByGridId(gridId);
        if (study == null) {
            study = new Study();
            study.setGridId(gridId);
        }
        //TODO: Use amendments to figure out which study is newer and use that assigned identifier
        study.setAssignedIdentifier(element.getAttribute(ASSIGNED_IDENTIFIER));
                                Node e = element.getFirstChild();
        return study;
    }

    protected PlannedCalendar parsePlannedCalendar(Document doc) {
        NodeList nodes = doc.getElementsByTagName(PLANNDED_CALENDAR);

        Element element = ((Element)nodes.item(0));
        String gridId = element.getAttribute(ID);
        PlannedCalendar calendar = plannedCalendarDao.getByGridId(gridId);
        if (calendar == null) {
            calendar = new PlannedCalendar();
            calendar.setGridId(gridId);
        }

        return calendar;
    }


    protected List<Amendment> parseAmendment(Document doc, Study study) throws Exception {
        List<Amendment> amendments = new ArrayList<Amendment>();

        NodeList nodes = doc.getElementsByTagName(StudyXMLWriter.AMENDMENT);
        for (int i=0; i < nodes.getLength(); i++) {
            Element element = ((Element)nodes.item(i));

            String gridId = element.getAttribute(ID);
            Amendment amendment = amendmentDao.getByGridId(gridId);
            if (amendment == null) {
                amendment = new Amendment();
                amendment.setGridId(gridId);
                amendment.setName(element.getAttribute(StudyXMLWriter.NAME));
                amendment.setMandatory(valueOf(element.getAttribute(StudyXMLWriter.MANDATORY)));
                amendment.setDate(formatter.parse(element.getAttribute(StudyXMLWriter.DATE)));

                String prevAmendmentGridId = element.getAttribute(StudyXMLWriter.PREVIOUS_AMENDMENT_ID);
                if (prevAmendmentGridId != null) {
                    for (Amendment searchAmendment : amendments) {
                        if (searchAmendment.getGridId().equals(prevAmendmentGridId)) {
                            amendment.setPreviousAmendment(searchAmendment);
                        }
                    }
                } else {
                    study.setAmendment(amendment);
                }
            }
            amendments.add(amendment);
        }

        return amendments;
    }


    protected List<Delta> parseDeltas(Document doc, List<Amendment> amendments) {
        List<Delta> deltas = new ArrayList<Delta>();

        NodeList nodes = doc.getElementsByTagName(StudyXMLWriter.DELTA);
        for (int i=0; i < nodes.getLength(); i++) {
            Element element = ((Element)nodes.item(i));

            String gridId = element.getAttribute(ID);
            Delta delta = deltaDao.getByGridId(gridId);
            if (delta == null) {
                Amendment amendment = findParent(element, amendments);
                if (amendment == null) {
                    throw new StudyCalendarError("Cannot find parent for Delta: %s", gridId);
                }

            }

            deltas.add(delta);
        }

        return deltas;
    }

    /* Find element parent by passing XML element and list of possible parents */
    private <T extends GridIdentifiable> T findParent (Element element, List<T> possibleParents) {
        Element parent = (Element) element.getParentNode();
        String parentGridId = parent.getAttribute(ID);
        for (T possibleParent : possibleParents) {
            if (parentGridId.equals(possibleParent.getGridId())) {
                return possibleParent;
            }
        }
        return null;
    }


    /* Dao Setters */
    public void setStudyDao(StudyDao studyDao) {
        this.studyDao = studyDao;
    }

    public void setAmendmentDao(AmendmentDao amendmentDao) {
        this.amendmentDao = amendmentDao;
    }

    public void setPlannedCalendarDao(PlannedCalendarDao plannedCalendarDao) {
        this.plannedCalendarDao = plannedCalendarDao;
    }

    public void setDeltaDao(DeltaDao deltaDao) {
        this.deltaDao = deltaDao;
    }

    public void setEpochDao(EpochDao epochDao) {
        this.epochDao = epochDao;
    }
}
