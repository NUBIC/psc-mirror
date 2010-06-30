package edu.northwestern.bioinformatics.studycalendar.grid;

import static org.easymock.EasyMock.expect;
import edu.northwestern.bioinformatics.studycalendar.domain.*;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.AmendmentApproval;
import edu.northwestern.bioinformatics.studycalendar.dao.*;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscUser;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscUserDetailsService;
import edu.northwestern.bioinformatics.studycalendar.service.*;
import edu.northwestern.bioinformatics.studycalendar.xml.writers.StudySubjectAssignmentXmlSerializer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.easymock.classextension.EasyMock;

import org.springframework.test.AbstractTransactionalSpringContextTests;
import org.springframework.beans.factory.annotation.Required;
import gov.nih.nci.cabig.ctms.audit.domain.DataAuditInfo;
import gov.nih.nci.cabig.ctms.suite.authorization.SuiteRole;
import gov.nih.nci.cabig.ctms.suite.authorization.SuiteRoleMembership;
import gov.nih.nci.cabig.ccts.domain.Registration;
import gov.nih.nci.cagrid.common.Utils;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.io.InputStream;
import java.io.Reader;
import java.io.FileReader;
import java.io.InputStreamReader;

/**
 * Test class added to validate the clean scripts that were added for CCTS roll-back script requirement
 *
 * @author Saurabh Agrawal
 */
public class PSCRegistrationConsumerTest extends AbstractTransactionalSpringContextTests {

	public static final Log logger = LogFactory.getLog(PSCRegistrationConsumerTest.class);

	private PSCRegistrationConsumer registrationConsumer;
	private String regFile;
	private StudyService studyService;

	private String assignedIdentifier = "TEST_STUDY";
	private String nciCode = "SITE_01";
	private SiteDao siteDao;
	private String shortTitle = "SMOTE_TEST";
	private String longTitle = "Test long title";
	private String assignmentGridId = "6115c43c-851e-425c-8312-fd78367aaef3"; 
	private String subjectGridId = "91dd4580-801b-4874-adeb-a174361bacea";
	
	private StudySubjectAssignmentDao studySubjectAssignmentDao;
	private SubjectService subjectService;
	private StudyDao studyDao;
	private SubjectDao subjectDao;
	private StudySubjectAssignmentXmlSerializer studySubjectAssignmentXmlSerializer;
	private SiteService siteService;
	private AmendmentService amendmentService;
	private Study study;
	private StudySite studySite;

	private RegistrationGridServiceAuthorizationHelper gridServicesAuthorizationHelper;
	private PscUserDetailsService pscUserDetailsService;
	private PscUser user;
	
	protected void onSetUpInTransaction() throws Exception {

		DataAuditInfo.setLocal(new DataAuditInfo("test", "localhost", new Date(), "/wsrf-psc/services/cagrid/RegistrationConsumer"));
		regFile = System.getProperty("psc.test.sampleRegistrationFile");
		study = studyDao.getByAssignedIdentifier(assignedIdentifier);
		if (study == null) {
			logger.error(String.format("no study found for given identifier %s", assignedIdentifier));
			createStudy(); //create study and re-run the test case..
		}

		gridServicesAuthorizationHelper=EasyMock.createMock(RegistrationGridServiceAuthorizationHelper.class);
		pscUserDetailsService=EasyMock.createMock(PscUserDetailsService.class);

		SuiteRoleMembership suiteRoleMembership = new SuiteRoleMembership(SuiteRole.REGISTRAR, null, null);
		suiteRoleMembership.addSite("SITE_01");
		suiteRoleMembership.addStudy("TEST_STUDY");
		Map<SuiteRole,SuiteRoleMembership> expectedMemberships = Collections.singletonMap(SuiteRole.REGISTRAR,
				suiteRoleMembership);

		user = new PscUser(null, expectedMemberships);
	}

	protected void onTearDownAfterTransaction() throws Exception {

		DataAuditInfo.setLocal(null);
	}


	public void testCreateRegistrationLocal() throws Exception {
		logger.info("### Running test create Registration local method");
		Registration registration = getRegistration();
		registrationConsumer.setGridServicesAuthorizationHelper(gridServicesAuthorizationHelper);
		registrationConsumer.setPscUserDetailsService(pscUserDetailsService);

		expect(gridServicesAuthorizationHelper.getCurrentUsername()).andReturn("John");
		expect(pscUserDetailsService.loadUserByUsername("John")).andReturn(user);

		EasyMock.replay(gridServicesAuthorizationHelper);
		EasyMock.replay(pscUserDetailsService);

		try {
			registrationConsumer.register(registration);
			StudySubjectAssignment assignment = subjectDao.getAssignment(subjectService.findSubjectByPersonId("TEST_MRN"), study, studySite.getSite());

			EasyMock.verify(gridServicesAuthorizationHelper);
			EasyMock.verify(pscUserDetailsService);

			assertNotNull("must create assignment", assignment);
			assertNotNull("must create assignment", assignment.getId());  
		}
		catch (Exception ex) {
			ex.printStackTrace();
			fail("Error creating registration: " + ex.getMessage());
		}
	}

	public void testRollbackRegistrationLocal() throws Exception {
		logger.info("### Running test rollback Registration local method");
		Registration registration = getRegistration();
		registrationConsumer.setGridServicesAuthorizationHelper(gridServicesAuthorizationHelper);
		registrationConsumer.setPscUserDetailsService(pscUserDetailsService);

		expect(gridServicesAuthorizationHelper.getCurrentUsername()).andReturn("John");
		expect(pscUserDetailsService.loadUserByUsername("John")).andReturn(user);

		EasyMock.replay(gridServicesAuthorizationHelper);
		EasyMock.replay(pscUserDetailsService);
		try {           
			registrationConsumer.register(registration);
			StudySubjectAssignment assignment = subjectDao.getAssignment(subjectService.findSubjectByPersonId("TEST_MRN"), study, studySite.getSite());

			EasyMock.verify(gridServicesAuthorizationHelper);
			EasyMock.verify(pscUserDetailsService);

			assertNotNull("must create assignment", assignment);
			assertNotNull("must create assignment", assignment.getId());
			registrationConsumer.rollback(registration);

			Subject subject =  subjectDao.findSubjectByPersonId("TEST_MRN");
			assertNull("Subject not deleted", subject);
		}
		catch (Exception ex) {
			ex.printStackTrace();
			fail("Error creating/rollback registration: " + ex.getMessage());
		}
	}



	public void setStudySubjectAssignmentXmlSerializer(StudySubjectAssignmentXmlSerializer studySubjectAssignmentXmlSerializer) {
		this.studySubjectAssignmentXmlSerializer = studySubjectAssignmentXmlSerializer;
	}

	public void createStudy() throws Exception {
		if (studyDao.getByAssignedIdentifier(assignedIdentifier) == null) {
			logger.debug("creating study for given identifer:" + assignedIdentifier);
			study = TemplateSkeletonCreatorImpl.createBase(shortTitle);
			study.setAssignedIdentifier(assignedIdentifier);
			study.setLongTitle(longTitle);

			Site site = siteDao.getByAssignedIdentifier(nciCode);
			if (site == null) {
				String message = "No site exists for given assigned identifier" + nciCode;
				logger.error(message);
				site = new Site();
				site.setAssignedIdentifier(nciCode);
				site.setName(nciCode);
				siteService.createOrUpdateSite(site);
			}
			studySite = new StudySite();
			studySite.setSite(site);
			studySite.setStudy(study);
			study.addStudySite(studySite);

			TemplateSkeletonCreatorImpl.addEpoch(study, 0, Epoch.create("Treatment"));
			Epoch epoch = new Epoch();
			epoch.setName("Treatment");
			StudySegment child = new StudySegment();
			child.setName("Arm A");
			epoch.addChild(child);
			study.getPlannedCalendar().addEpoch(epoch);

			studyService.save(study);
			amendmentService.amend(study);

			AmendmentApproval approvals = new AmendmentApproval();
			approvals.setAmendment(study.getAmendment());
			approvals.setDate(new Date());

			amendmentService.approve(studySite, approvals);
			logger.info("Created the study :" + study.getId());
		} else {
			logger.debug("study already exists for given identifier : " + assignedIdentifier);
		}
	}


	protected String[] getConfigLocations() {
		String[] configs = {"classpath:applicationContext-grid.xml"};
		return configs;
	}


	private Registration getRegistration() {
		Registration reg = null;
		try {
			InputStream config = Thread.currentThread().getContextClassLoader().getResourceAsStream("gov/nih/nci/ccts/grid/client/client-config.wsdd");
			Reader reader = null;
			if (regFile != null){
				reader = new FileReader(regFile);
			}else{
				reader = new InputStreamReader(Thread.currentThread().getContextClassLoader().getResourceAsStream(
				"SampleRegistrationMessage.xml"));
			}
			reg = (Registration) Utils.deserializeObject(reader, Registration.class, config);
		}
		catch (Exception ex) {
			ex.printStackTrace();
			fail("Error deserializing Registration object: " + ex.getMessage());
		}
		return reg;
	}


	public void setStudySubjectAssignmentDao(StudySubjectAssignmentDao studySubjectAssignmentDao) {
		this.studySubjectAssignmentDao = studySubjectAssignmentDao;
	}

	public void setSubjectService(SubjectService subjectService) {
		this.subjectService = subjectService;
	}

	public void setSubjectDao(SubjectDao subjectDao) {
		this.subjectDao = subjectDao;
	}

	@Required
	public void setAmendmentService(AmendmentService amendmentService) {
		this.amendmentService = amendmentService;
	}

	@Required
	public void setStudyService(StudyService studyService) {
		this.studyService = studyService;
	}

	@Required
	public void setSiteDao(SiteDao siteDao) {
		this.siteDao = siteDao;
	}

	@Required
	public void setSiteService(SiteService siteService) {
		this.siteService = siteService;
	}

	@Required
	public void setStudyDao(StudyDao studyDao) {
		this.studyDao = studyDao;
	}

	@Required
	public void setRegistrationConsumer(PSCRegistrationConsumer registrationConsumer) {
		this.registrationConsumer = registrationConsumer;
	}
}