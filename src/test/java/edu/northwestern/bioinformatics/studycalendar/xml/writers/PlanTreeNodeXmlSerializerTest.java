package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import edu.northwestern.bioinformatics.studycalendar.dao.EpochDao;
import edu.northwestern.bioinformatics.studycalendar.dao.PeriodDao;
import edu.northwestern.bioinformatics.studycalendar.dao.PlannedActivityDao;
import edu.northwestern.bioinformatics.studycalendar.dao.StudySegmentDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Epoch;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.createNamedInstance;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.setGridId;
import edu.northwestern.bioinformatics.studycalendar.domain.Period;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySegment;
import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarXmlTestCase;
import static edu.northwestern.bioinformatics.studycalendar.xml.writers.StudyXMLWriter.*;
import org.dom4j.Document;
import org.dom4j.Element;
import static org.easymock.EasyMock.expect;

import static java.text.MessageFormat.format;

public class PlanTreeNodeXmlSerializerTest extends StudyCalendarXmlTestCase {

    private PlanTreeNodeXmlSerializer serializer;
    private Element element;
    private Epoch epoch;
    private EpochDao epochDao;
    private StudySegment segment;
    private PeriodDao periodDao;
    private StudySegmentDao studySegmentDao;
    private PlannedActivityDao plannedActivityDao;
    private Period period;

    protected void setUp() throws Exception {
        super.setUp();

        element = registerMockFor(Element.class);
        epochDao = registerDaoMockFor(EpochDao.class);
        periodDao = registerDaoMockFor(PeriodDao.class);
        studySegmentDao = registerDaoMockFor(StudySegmentDao.class);
        plannedActivityDao = registerDaoMockFor(PlannedActivityDao.class);

        serializer = new PlanTreeNodeXmlSerializer();
        serializer.setEpochDao(epochDao);
        serializer.setPeriodDao(periodDao);
        serializer.setStudySegmentDao(studySegmentDao);
        serializer.setPlannedActivityDao(plannedActivityDao);

        epoch = setGridId("grid0", createNamedInstance("Epoch A", Epoch.class));
        segment = setGridId("grid1", createNamedInstance("Segment A", StudySegment.class));
        period = setGridId("grid2", createNamedInstance("Period A", Period.class));

    }

    public void testCreateElementEpoch() {
        Element actual = serializer.createElement(epoch);

        assertEquals("Wrong attribute size", 2, actual.attributeCount());
        assertEquals("Wrong grid id", "grid0", actual.attribute("id").getValue());
        assertEquals("Wrong epoch name", "Epoch A", actual.attribute("name").getValue());
    }

    public void testReadElementEpoch() {
        expect(element.attributeValue("id")).andReturn("grid0");
        expect(element.getName()).andReturn("epoch").times(2);
        expect(epochDao.getByGridId("grid0")).andReturn(null);
        expect(element.attributeValue("name")).andReturn("Epoch A");
        replayMocks();

        Epoch actual = (Epoch) serializer.readElement(element);
        verifyMocks();

        assertEquals("Wrong grid id", "grid0", actual.getGridId());
        assertEquals("Wrong epoch name", "Epoch A", actual.getName());
    }

    public void testReadElementExistsEpoch() {
        expect(element.attributeValue("id")).andReturn("grid0");
        expect(element.getName()).andReturn("epoch");
        expect(epochDao.getByGridId("grid0")).andReturn(epoch);
        replayMocks();

        Epoch actual = (Epoch) serializer.readElement(element);
        verifyMocks();

        assertSame("Wrong Epoch", epoch, actual);
    }

    public void testCreateDocumentEpoch() throws Exception {
        Document actual = serializer.createDocument(epoch);

        assertEquals("Element should be an epoch", "epoch", actual.getRootElement().getName());
        assertEquals("Wrong epoch grid id", "grid0", actual.getRootElement().attributeValue("id"));
        assertEquals("Wrong epoch name", "Epoch A", actual.getRootElement().attributeValue("name"));
    }

    public void testCreateDocumentStringEpoch() throws Exception {
        StringBuffer expected = new StringBuffer();
        expected.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>")
                .append(format("<epoch id=\"{0}\" name=\"{1}\"", epoch.getGridId(), epoch.getName()))
                .append(format("       {0}=\"{1}\""     , SCHEMA_NAMESPACE_ATTRIBUTE, PSC_NS))
                .append(format("       {0}=\"{1} {2}\""     , SCHEMA_LOCATION_ATTRIBUTE, PSC_NS, SCHEMA_LOCATION))
                .append(format("       {0}=\"{1}\"/>"    , XML_SCHEMA_ATTRIBUTE, XSI_NS));

        String actual = serializer.createDocumentString(epoch);
        assertXMLEqual(expected.toString(), actual);
    }

    public void testCreateElementStudySegment() {
        Element actual = serializer.createElement(segment);

        assertEquals("Wrong attribute size", 2, actual.attributeCount());
        assertEquals("Wrong grid id", "grid1", actual.attribute("id").getValue());
        assertEquals("Wrong segment name", "Segment A", actual.attribute("name").getValue());
    }

    public void testReadElementStudySegment() {
        expect(element.attributeValue("id")).andReturn("grid1");
        expect(element.getName()).andReturn("study-segment").times(2);
        expect(studySegmentDao.getByGridId("grid1")).andReturn(null);
        expect(element.attributeValue("name")).andReturn("Segment A");
        replayMocks();

        StudySegment actual = (StudySegment) serializer.readElement(element);
        verifyMocks();

        assertEquals("Wrong grid id", "grid1", actual.getGridId());
        assertEquals("Wrong segment name", "Segment A", actual.getName());
    }

    public void testReadElementExistsStudySegment() {
        expect(element.attributeValue("id")).andReturn("grid1");
        expect(element.getName()).andReturn("study-segment");
        expect(studySegmentDao.getByGridId("grid1")).andReturn(segment);
        replayMocks();

        StudySegment actual = (StudySegment) serializer.readElement(element);
        verifyMocks();

        assertSame("Wrong Epoch", segment, actual);
    }

    public void testCreateElementPeriod() {
        Element actual = serializer.createElement(period);

        assertEquals("Wrong attribute size", 2, actual.attributeCount());
        assertEquals("Wrong grid id", "grid2", actual.attribute("id").getValue());
        assertEquals("Wrong period name", "Period A", actual.attribute("name").getValue());
    }

    public void testReadElementStudyPeriod() {
        expect(element.attributeValue("id")).andReturn("grid2");
        expect(element.getName()).andReturn("period").times(2);
        expect(periodDao.getByGridId("grid2")).andReturn(null);
        expect(element.attributeValue("name")).andReturn("Period A");
        replayMocks();

        Period actual = (Period) serializer.readElement(element);
        verifyMocks();

        assertEquals("Wrong grid id", "grid2", actual.getGridId());
        assertEquals("Wrong period name", "Period A", actual.getName());
    }

    public void testReadElementExistsPeriod() {
        expect(element.attributeValue("id")).andReturn("grid2");
        expect(element.getName()).andReturn("period");
        expect(periodDao.getByGridId("grid2")).andReturn(period);
        replayMocks();

        Period actual = (Period) serializer.readElement(element);
        verifyMocks();

        assertSame("Wrong Period", period, actual);
    }



}
