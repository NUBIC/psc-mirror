package edu.northwestern.bioinformatics.studycalendar.web;

import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.dao.UserDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.User;
import edu.northwestern.bioinformatics.studycalendar.service.TemplateService;
import edu.northwestern.bioinformatics.studycalendar.service.presenter.DevelopmentTemplate;
import edu.northwestern.bioinformatics.studycalendar.service.presenter.ReleasedTemplate;
import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.ApplicationSecurityManager;
import edu.northwestern.bioinformatics.studycalendar.utils.breadcrumbs.DefaultCrumb;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Rhett Sutphin
 */
public class StudyListController extends PscAbstractController {
    private StudyDao studyDao;
    private TemplateService templateService;
    private UserDao userDao;

    public StudyListController() {
        setCrumb(new DefaultCrumb("Studies"));
    }

    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
        List<Study> studies = studyDao.getAll();
        log.debug("{} studies found total", studies.size());
        String userName = ApplicationSecurityManager.getUser();
        User user = userDao.getByName(userName);

        List<DevelopmentTemplate> inDevelopmentTemplates = templateService.getInDevelopmentTemplates(studies, user);
        List<ReleasedTemplate> releasedTemplates = templateService.getReleasedTemplates(studies, user);
        List<ReleasedTemplate> pendingTemplates = templateService.getPendingTemplates(studies, user);
        List<ReleasedTemplate> releasedAndAssignedTemplates = templateService.getReleasedAndAssignedTemplates(studies, user);

        log.debug("{} released templates visible to {}", releasedTemplates.size(), userName);
        log.debug("{} studies open for editing by {}", inDevelopmentTemplates.size(), userName);

        Map<String, Object> model = new HashMap<String, Object>();
        model.put("pendingTemplates", pendingTemplates);
        model.put("releasedAndAssignedTemplate", releasedAndAssignedTemplates);
        model.put("releasedAndAssignedTemplatesSize", releasedAndAssignedTemplates.size());

        model.put("releasedTemplates", releasedTemplates);
        model.put("inDevelopmentTemplates", inDevelopmentTemplates);

        return new ModelAndView("studyList", model);
    }


    ////// CONFIGURATION

    @Required
    public void setStudyDao(StudyDao studyDao) {
        this.studyDao = studyDao;
    }
    
    @Required
    public void setTemplateService(TemplateService templateService) {
        this.templateService = templateService;
    }

    @Required
    public void setUserDao(UserDao userDao) {
        this.userDao = userDao;
    }
}