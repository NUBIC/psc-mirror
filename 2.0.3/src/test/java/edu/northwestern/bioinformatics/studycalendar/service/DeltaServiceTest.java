package edu.northwestern.bioinformatics.studycalendar.service;

import edu.northwestern.bioinformatics.studycalendar.dao.StudySegmentDao;
import edu.northwestern.bioinformatics.studycalendar.dao.EpochDao;
import edu.northwestern.bioinformatics.studycalendar.dao.StaticDaoFinder;
import edu.northwestern.bioinformatics.studycalendar.dao.delta.DeltaDao;
import edu.northwestern.bioinformatics.studycalendar.dao.delta.ChangeDao;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySegment;
import edu.northwestern.bioinformatics.studycalendar.domain.Epoch;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.*;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedCalendar;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.PlanTreeNode;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Add;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Amendment;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Delta;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Remove;
import edu.northwestern.bioinformatics.studycalendar.service.delta.MutatorFactory;
import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;
import static org.easymock.classextension.EasyMock.expect;
import static org.easymock.classextension.EasyMock.*;
import org.easymock.classextension.EasyMock;

/**
 * Note that some tests here are more like integration tests in that they test the full
 * DeltaService/MutatorFactory/Mutator stack.
 *
 * @author Rhett Sutphin
 */
public class DeltaServiceTest extends StudyCalendarTestCase {
    private Study study;
    private PlannedCalendar calendar;
    private Epoch e1;
    private Epoch e2;
    private StudySegment e1a0;

    private DeltaService service;
    private TemplateService mockTemplateService;

    private DeltaDao deltaDao;
    private ChangeDao changeDao;
    private EpochDao epochDao;
    private StudySegmentDao studySegmentDao;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        study = setGridId("STUDY-GRID", setId(300, createBasicTemplate()));
        calendar = setGridId("CAL-GRID", setId(400, study.getPlannedCalendar()));
        e1 = setGridId("E1-GRID", setId(1, calendar.getEpochs().get(1)));
        e2 = setGridId("E2-GRID", setId(2, calendar.getEpochs().get(2)));
        e1a0 = setGridId("E1A0-GRID",
            setId(10, calendar.getEpochs().get(1).getStudySegments().get(0)));

        Amendment a3 = createAmendments("A0", "A1", "A2", "A3");
        Amendment a2 = a3.getPreviousAmendment();
        study.setAmendment(a3);

        a2.addDelta(Delta.createDeltaFor(calendar, createAddChange(2, null)));
        a3.addDelta(Delta.createDeltaFor(e1, createAddChange(10, 0)));

        changeDao = registerDaoMockFor(ChangeDao.class);
        deltaDao = registerDaoMockFor(DeltaDao.class);
        epochDao = registerDaoMockFor(EpochDao.class);
        studySegmentDao = registerDaoMockFor(StudySegmentDao.class);
        expect(epochDao.getById(2)).andReturn(e2).anyTimes();
        expect(studySegmentDao.getById(10)).andReturn(e1a0).anyTimes();

        StaticDaoFinder daoFinder = new StaticDaoFinder(epochDao, studySegmentDao);
        MutatorFactory mutatorFactory = new MutatorFactory();
        mutatorFactory.setDaoFinder(daoFinder);
        TestingTemplateService templateService = new TestingTemplateService();
        templateService.setDaoFinder(daoFinder);

        service = new DeltaService();
        service.setMutatorFactory(mutatorFactory);
        service.setTemplateService(templateService);
        service.setChangeDao(changeDao);
        service.setDeltaDao(deltaDao);
        service.setDaoFinder(daoFinder);

        mockTemplateService = registerMockFor(TemplateService.class);
    }

    public void testRevise() throws Exception {
        assertEquals("Wrong number of epochs to start with", 3, calendar.getEpochs().size());

        Amendment inProgress = new Amendment();
        Epoch newEpoch = setGridId("E-NEW", setId(8, Epoch.create("Long term")));
        inProgress.addDelta(Delta.createDeltaFor(calendar, createAddChange(8, null)));

        expect(epochDao.getById(8)).andReturn(newEpoch).anyTimes();

        replayMocks();
        Study revised = service.revise(study, inProgress);
        verifyMocks();

        assertEquals("Epoch not added", 4, revised.getPlannedCalendar().getEpochs().size());
        assertEquals("Epoch not added in the expected location", 8,
            (int) revised.getPlannedCalendar().getEpochs().get(3).getId());

        assertEquals("Original calendar modified", 3, calendar.getEpochs().size());
    }

    /* TODO: This should be corrected.  For now, #saveRevision is tested in StudyServiceIntegratedTest
    public void testSaveRevision() throws Exception {
        PlannedCalendarDelta delta = new PlannedCalendarDelta(calendar);
        Epoch added = new Epoch();
        Add add = new Add();
        add.setChild(added);
        delta.addChange(add);

        Amendment revision = new Amendment("Rev to save");
        revision.addDelta(delta);

        epochDao.save(added);
        deltaDao.save(delta);
        amendmentDao.save(revision);
        replayMocks();
        service.saveRevision(revision);
        verifyMocks();
    }
    */

    public void testUpdateRevisionOnNewlyAdded() throws Exception {
        Amendment rev = new Amendment();
        Epoch epoch = setId(4, Epoch.create("New"));
        Delta<?> pcDelta = Delta.createDeltaFor(study.getPlannedCalendar(),
            Add.create(epoch, 3));
        rev.addDelta(pcDelta);
        StudySegment studySegment = createNamedInstance("N", StudySegment.class);

        assertEquals("Wrong number of study segments initially", 1, epoch.getStudySegments().size());

        service.updateRevision(rev, epoch, Add.create(studySegment));

        assertEquals("Study segment not directly applied", 2, epoch.getStudySegments().size());
    }

    public void testUpdateRevisionWhenThereIsAlreadyAnApplicableDelta() throws Exception {
        Amendment rev = new Amendment();
        Epoch epoch = setId(4, createNamedInstance("New", Epoch.class));
        Delta<?> pcDelta = Delta.createDeltaFor(study.getPlannedCalendar(),
            Add.create(epoch, 3));
        rev.addDelta(pcDelta);

        assertEquals("Wrong number of changes in delta initially", 1, pcDelta.getChanges().size());

        service.updateRevision(rev, study.getPlannedCalendar(),
            Remove.create(study.getPlannedCalendar().getEpochs().get(2)));

        assertEquals("New change not merged into delta", 2, pcDelta.getChanges().size());
    }
    
    public void testUpdateRevisionWhenThereIsNoApplicableDelta() throws Exception {
        Amendment rev = new Amendment();
        Epoch epoch = setId(4, createNamedInstance("New", Epoch.class));
        Delta<?> pcDelta = Delta.createDeltaFor(study.getPlannedCalendar(),
            Add.create(epoch, 3));
        rev.addDelta(pcDelta);

        assertEquals("Wrong number of deltas initially", 1, rev.getDeltas().size());

        Epoch expectedTarget = study.getPlannedCalendar().getEpochs().get(1);
        PlanTreeNode<?> newChild = new StudySegment();
        Add expectedChange = Add.create(newChild, 2);
        service.updateRevision(rev, expectedTarget, expectedChange);

        assertEquals("Wrong number of deltas", 2, rev.getDeltas().size());
        Delta<?> added = rev.getDeltas().get(1);
        assertSame("Added delta is not for correct node", expectedTarget, added.getNode());
        assertEquals("Wrong number of changes in new delta", 1, added.getChanges().size());
        assertEquals("Wrong change in new delta", expectedChange, added.getChanges().get(0));
    }

    public void testDeleteDeltaWithRealizedChildInAdd() throws Exception {
        service.setTemplateService(mockTemplateService);

        Epoch addedEpoch = new Epoch();
        Delta<?> pcDelta = Delta.createDeltaFor(calendar, Add.create(addedEpoch), Remove.create(e2));
        mockTemplateService.delete(addedEpoch);
        deltaDao.delete(pcDelta);
        changeDao.delete(pcDelta.getChanges().get(0));
        changeDao.delete(pcDelta.getChanges().get(1));

        replayMocks();
        service.delete(pcDelta);
        verifyMocks();
    }

    public void testDeleteDeltaWithChildIdOnlyInAdd() throws Exception {
        service.setTemplateService(mockTemplateService);

        Epoch addedEpoch = setId(4, new Epoch());
        Delta<?> pcDelta = Delta.createDeltaFor(calendar, createAddChange(4, null), Remove.create(e2));
        expect(epochDao.getById(4)).andReturn(addedEpoch);
        mockTemplateService.delete(addedEpoch);
        deltaDao.delete(pcDelta);
        changeDao.delete(pcDelta.getChanges().get(0));
        changeDao.delete(pcDelta.getChanges().get(1));

        replayMocks();
        service.delete(pcDelta);
        verifyMocks();
    }
}
