package edu.northwestern.bioinformatics.studycalendar.service;

import edu.northwestern.bioinformatics.studycalendar.dao.ActivityDao;
import edu.northwestern.bioinformatics.studycalendar.dao.SourceDao;
import edu.northwestern.bioinformatics.studycalendar.domain.*;
import edu.northwestern.bioinformatics.studycalendar.xml.writers.StudyXmlSerializer;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Transactional
public class ImportTemplateService {
    private StudyXmlSerializer studyXmlSerializer;
    private ActivityDao activityDao;
    private StudyService studyService;
    private SourceDao sourceDao;

    public void importTemplate (InputStream stream) {
        Study study = studyXmlSerializer.readDocument(stream);

        for (Epoch epoch : study.getPlannedCalendar().getEpochs()) {
            for (StudySegment segment : epoch.getStudySegments()) {
                for (Period period : segment.getPeriods()) {
                    for (PlannedActivity plannedActivity : period.getPlannedActivities()) {
                        Activity activity = plannedActivity.getActivity();

                        Activity existingActivity = activityDao.getByCodeAndSourceName(activity.getCode(), activity.getSource().getName());
                        if (existingActivity != null) {
                            plannedActivity.setActivity(existingActivity);
                        } else {
                            Source existingSource = sourceDao.getByName(activity.getSource().getName());
                            if (existingSource != null) {
                                activity.setSource(existingSource);
                            }
                        }

                        sourceDao.save(plannedActivity.getActivity().getSource());
                        activityDao.save(plannedActivity.getActivity());
                    }
                }
            }
        }

        studyService.save(study);
    }


    public void setActivityDao(ActivityDao activityDao) {
        this.activityDao = activityDao;
    }

    public void setStudyXmlSerializer(StudyXmlSerializer studyXmlSerializer) {
        this.studyXmlSerializer = studyXmlSerializer;
    }

    public void setStudyService(StudyService studyService) {
        this.studyService = studyService;
    }

    public void setSourceDao(SourceDao sourceDao) {
        this.sourceDao = sourceDao;
    }
}
