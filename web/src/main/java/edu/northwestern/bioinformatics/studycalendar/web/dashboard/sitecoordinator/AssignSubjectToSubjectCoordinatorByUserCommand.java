package edu.northwestern.bioinformatics.studycalendar.web.dashboard.sitecoordinator;

import static edu.northwestern.bioinformatics.studycalendar.domain.StudySite.findStudySite;
import edu.northwestern.bioinformatics.studycalendar.domain.*;
import edu.northwestern.bioinformatics.studycalendar.dao.SubjectDao;

import java.util.List;
import java.util.ArrayList;

public class AssignSubjectToSubjectCoordinatorByUserCommand {
    private SubjectDao subjectDao;
    private List<Subject> subjects = new ArrayList<Subject>();
    private User subjectCoordinator;
    private Study study;
    private Site site;
    private User selected;

    public void assignSubjectsToSubjectCoordinator() {
        StudySite studySite = findStudySite(study, site);
        for (Subject subject : subjects) {
            List<StudySubjectAssignment> assignments = subject.getAssignments();
            for (StudySubjectAssignment assignment : assignments) {
                if (studySite.equals(assignment.getStudySite())) {
                    assignment.setSubjectCoordinator(subjectCoordinator);
                    subjectDao.save(subject);
                }
            }
        }
    }

    public void setSubjectDao(SubjectDao subjectDao) {
        this.subjectDao = subjectDao;
    }

    public void setSubjects(List<Subject> subjects) {
        this.subjects = subjects;
    }

    public void setSubjectCoordinator(User subjectCoordinator) {
        this.subjectCoordinator = subjectCoordinator;
    }

    public void setStudy(Study study) {
        this.study = study;
    }

    public void setSite(Site site) {
        this.site = site;
    }

    public void setSelected(User selected) {
        this.selected = selected;
    }

    public List<Subject> getSubjects() {
        return subjects;
    }

    public User getSubjectCoordinator() {
        return subjectCoordinator;
    }

    public Study getStudy() {
        return study;
    }

    public Site getSite() {
        return site;
    }

    public User getSelected() {
        return selected;
    }
}