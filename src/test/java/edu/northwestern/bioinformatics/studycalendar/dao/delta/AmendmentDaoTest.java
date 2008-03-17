package edu.northwestern.bioinformatics.studycalendar.dao.delta;

import edu.northwestern.bioinformatics.studycalendar.domain.delta.Amendment;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Delta;
import edu.northwestern.bioinformatics.studycalendar.testing.DaoTestCase;
import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;
import static edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase.*;
import edu.northwestern.bioinformatics.studycalendar.StudyCalendarValidationException;

import java.util.List;
import java.util.Calendar;

import gov.nih.nci.cabig.ctms.lang.DateTools;

public class AmendmentDaoTest extends DaoTestCase {
    private AmendmentDao amendmentDao;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        amendmentDao = (AmendmentDao) getApplicationContext().getBean("amendmentDao");
    }

    public void testGetById() throws Exception {
        Amendment actual = amendmentDao.getById(-100);
        assertNotNull("Amendment not found", actual);
        assertEquals("Wrong name", "abc", actual.getName());
        assertDayOfDate("Wrong date", 2006, Calendar.FEBRUARY, 1, actual.getDate());
        assertEquals("Wrong number of deltas", 1, actual.getDeltas().size());
        Delta delta = actual.getDeltas().get(0);
        assertEquals("Wrong delta", -102, (int) delta.getId());
        assertEquals("Wrong node for delta", -22, (int) delta.getNode().getId());
        assertSame("Reverse relationship not loaded", actual, delta.getRevision());
    }

    public void testLoadMandatoryFlag() throws Exception {
        assertFalse(amendmentDao.getById(-200).isMandatory());
    }

    public void testSave() throws Exception {
        int initialCount;
        {
            Amendment amendment = new Amendment();
            amendment.setName("new name");
            amendment.setDate(DateTools.createDate(2008, Calendar.FEBRUARY, 3));
            initialCount = amendmentDao.getAll().size();
            amendmentDao.save(amendment);
        }

        interruptSession();

        List<Amendment> listAfterAdding = amendmentDao.getAll();
        assertEquals("Amendment wasn't added ", initialCount + 1 , listAfterAdding.size());
    }

    public void testGetAll() throws Exception {
        List<Amendment> all = amendmentDao.getAll();
        assertEquals(7, all.size());
    }

    public void testGetByKeyWithoutNameWhenAmendmentHasNameButUniqueDate() throws Exception {
        Amendment actual = amendmentDao.getByNaturalKey("2006-02-01");
        assertNotNull("Could not find it", actual);
        assertEquals(-100, (int) actual.getId());
    }

    public void testGetByKeyWithoutNameWhenOneHasANameAndOneDoesNot() throws Exception {
        Amendment actual = amendmentDao.getByNaturalKey("2008-05-17");
        assertNotNull("Could not find it", actual);
        assertEquals(-220, (int) actual.getId());
    }

    public void testGetByKeyWithoutNameAmendmentDoesNotHaveOne() throws Exception {
        Amendment actual = amendmentDao.getByNaturalKey("2008-07-11");
        assertNotNull("Could not find it", actual);
        assertEquals(-221, (int) actual.getId());
    }

    public void testGetByKeyWithoutNameAmendmentAndDateInATimestampFormat() throws Exception {
        Amendment actual = amendmentDao.getByNaturalKey("2008-08-13");
        assertNotNull("Could not find it", actual);
        assertEquals(-224, (int) actual.getId());
    }

    public void testGetByKeyWithoutNameAmendmentAndDateInATimestampFormatAndOneDayOff() throws Exception {
        Amendment actual = amendmentDao.getByNaturalKey("2008-08-14");
        assertNull("Could not find it", actual);
    }

    public void testGetByKeyWithoutNameWhenItIsAmbiguous() throws Exception {
        try {
            amendmentDao.getByNaturalKey("2008-11-23");
            fail("Exception not thrown");
        } catch (StudyCalendarValidationException scve) {
            assertEquals("More than one amendment could match 2008-11-23: 2008-11-23~pheasant, 2008-11-23~turkey.  Please be more specific.",
                scve.getMessage());
        }
    }

    public void testGetByKeyWithName() throws Exception {
        Amendment actual = amendmentDao.getByNaturalKey("2008-11-23~pheasant");
        assertNotNull("Could not find it", actual);
        assertEquals(-222, (int) actual.getId());
    }

    public void testGetByKeyWorksWithGeneratedKeys() throws Exception {
        for (Amendment original : amendmentDao.getAll()) {
            Amendment relookedup = amendmentDao.getByNaturalKey(original.getNaturalKey());
            assertNotNull("Could not find using key " + original.getNaturalKey(), original);
            assertEquals("Mismatch: " + original + " != " + relookedup, original.getId(), relookedup.getId());
        }
    }
}