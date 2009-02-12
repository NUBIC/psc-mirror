package edu.northwestern.bioinformatics.studycalendar.dao;

import edu.northwestern.bioinformatics.studycalendar.domain.Population;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySubjectAssignment;
import edu.northwestern.bioinformatics.studycalendar.core.Fixtures;
import edu.northwestern.bioinformatics.studycalendar.domain.DomainObjectTools;
import static edu.nwu.bioinformatics.commons.testing.CoreTestCase.assertContains;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Rhett Sutphin
 */
public class StudyDaoTest extends ContextDaoTestCase<StudyDao> {
	public void testGetById() throws Exception {
		Study study = getDao().getById(-100);
		assertIsTestStudy100(study);
	}

	public void testGetByGridId() throws Exception {
		Study actual = getDao().getByGridId("long-GUID-string");
		assertIsTestStudy100(actual);
	}

	public void testGetByGridIdByTemplate() throws Exception {
		Study actual = getDao().getByGridId(Fixtures.setGridId("long-GUID-string", new Study()));
		assertIsTestStudy100(actual);
	}

	public void testGetByAssignedIdentifier() throws Exception {
		Study actual = getDao().getByAssignedIdentifier("NCI-IS-WATCHING");
		assertIsTestStudy100(actual);
	}

	public void testSearchByName() throws Exception {
		List<Study> studies = getDao().searchStudiesByStudyName("NCi");
		assertEquals("there must be 2 studies", 2, studies.size());
		for (Study study : studies) {
			assertTrue("study must have assigned identifer matching %nci% string", study.getName().toLowerCase().indexOf("nci") >= 0);
		}
		Collection<Integer> ids = DomainObjectTools.collectIds(studies);
		assertContains("Wrong study found", ids, -100);
		assertContains("Wrong study found", ids, -102);

		//now search with another string such that no study maatches for the given serach string
		String identifierWhichDoesNotExists = "identifier which does not exists";
		studies = getDao().searchStudiesByStudyName(identifierWhichDoesNotExists);
		assertEquals("there must be 3 studies", 3, studies.size());
		for (Study study : studies) {
			assertTrue("study must not have assigned identifer matching %nci identifier which does not exists% string", study.getName().toLowerCase().indexOf(identifierWhichDoesNotExists) < 0);
		}
		ids = DomainObjectTools.collectIds(studies);
		assertContains("Wrong study found", ids, -100);
		assertContains("Wrong study found", ids, -101);
		assertContains("Wrong study found", ids, -102);

	}

	public void testLoadAmendments() throws Exception {
		Study study = getDao().getById(-100);
		assertNotNull("Missing current amendment", study.getAmendment());
		assertNotNull("Missing current amendment is default (not loaded)", study.getAmendment().getId());
		assertEquals("Wrong current amendment", -45, (int) study.getAmendment().getId());
		assertNotNull("Missing dev amendment", study.getDevelopmentAmendment());
		assertEquals("Wrong dev amendment", -55, (int) study.getDevelopmentAmendment().getId());
	}

	public void testLoadPopulations() throws Exception {
		Study loaded = getDao().getById(-100);
		assertEquals("Wrong number of populations", 2, loaded.getPopulations().size());
		Collection<Integer> ids = DomainObjectTools.collectIds(loaded.getPopulations());
		assertContains("Missing expected population", ids, -64);
		assertContains("Missing expected population", ids, -96);
	}

	public void testGetAll() throws Exception {
		List<Study> actual = getDao().getAll();
		assertEquals(3, actual.size());
		Collection<Integer> ids = DomainObjectTools.collectIds(actual);
		assertContains("Wrong study found", ids, -100);
		assertContains("Wrong study found", ids, -101);
		assertContains("Wrong study found", ids, -102);

	}

	public void testSaveNewStudy() throws Exception {
		Integer savedId;
		{
			Study study = new Study();
			study.setName("New study");
			study.setLongTitle("New study");
			getDao().save(study);
			savedId = study.getId();
			assertNotNull("The saved study didn't get an id", savedId);
		}

		interruptSession();

		{
			Study loaded = getDao().getById(savedId);
			assertNotNull("Could not reload study with id " + savedId, loaded);
			assertEquals("Wrong name", "New study", loaded.getName());
			assertNotNull("Grid ID not automatically added", loaded.getGridId());
		}
	}

	public void testSaveNewStudyWithPopulation() throws Exception {
		Integer savedId;
		{
			Study study = new Study();
			study.setName("New study");
			study.setLongTitle("New study");
			Population population = new Population();
			population.setName("pop1");
			population.setAbbreviation("p1");
			study.addPopulation(population);
			getDao().save(study);
			savedId = study.getId();
			assertNotNull("The saved study didn't get an id", savedId);
			assertFalse("must load populations", study.getPopulations().isEmpty());

		}

		interruptSession();

		{
			Study loaded = getDao().getById(savedId);
			assertNotNull("Could not reload study with id " + savedId, loaded);
			assertEquals("Wrong name", "New study", loaded.getName());
			assertNotNull("Grid ID not automatically added", loaded.getGridId());
			Population population = loaded.getPopulations().iterator().next();
			assertNotNull("Could not reload study with id " + population);
			assertNotNull("Grid ID not automatically added", population.getGridId());
			assertEquals("Wrong name", "pop1", population.getName());


		}
	}

	public void testSaveNewStudyIsAudited() throws Exception {
		Integer savedId;
		{
			Study study = new Study();
			study.setName("New study");
			study.setLongTitle("New study");
			getDao().save(study);
			savedId = study.getId();
			assertNotNull("The saved study didn't get an id", savedId);
		}

		interruptSession();

		// List<gov.nih.nci.cabig.ctms.audit.domain.DataAuditEvent> trail = getAuditDao().getAuditTrail(
		// new DataReference(Study.class, savedId));
		// assertEquals("Wrong number of events in trail", 1, trail.size());
		// DataAuditEvent event = trail.get(0);
		// assertEquals("Wrong operation", Operation.CREATE, event.getElementRoles());
	}

	public void testGetStudySubjectAssigments() throws Exception {
		List<StudySubjectAssignment> actual = getDao().getAssignmentsForStudy(-100);
		assertEquals("Wrong number of assigments", 2, actual.size());
		List<Integer> ids = new ArrayList<Integer>(DomainObjectTools.collectIds(actual));

		assertContains("Missing expected assignment", ids, -10);
		assertContains("Missing expected assignment", ids, -11);
		assertEquals("Assignments in wrong order", -10, (int) ids.get(0));
		assertEquals("Assignments in wrong order", -11, (int) ids.get(1));
	}

	private void assertIsTestStudy100(final Study actual) {
		assertNotNull("Could not locate", actual);
		assertEquals("Wrong id", -100, (int) actual.getId());
		assertEquals("Wrong grid ID", "long-GUID-string", actual.getGridId());
		assertEquals("Wrong protocol auth id", "NCI-IS-WATCHING", actual.getAssignedIdentifier());
	}

}