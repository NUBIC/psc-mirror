package edu.northwestern.bioinformatics.studycalendar.web;

import java.util.List;
import java.util.LinkedList;

/**
 * @author Rhett Sutphin
 */
public class NewStudyCommand {
    private String studyName;
    private List<String> armNames = new LinkedList<String>();
    private boolean arms = false;

    public String getStudyName() {
        return studyName;
    }

    public void setStudyName(String studyName) {
        this.studyName = studyName;
    }

    public boolean getArms() {
        return arms;
    }

    public void setArms(boolean arms) {
        this.arms = arms;
    }

    public List<String> getArmNames() {
        return armNames;
    }
}
