<%@page contentType="text/javascript" language="java" %>
<%@taglib prefix="tags" tagdir="/WEB-INF/tags"%>
<%@taglib prefix="jsgen" uri="http://bioinformatics.northwestern.edu/taglibs/studycalendar/jsgenerator"%>
<%@taglib prefix="sched" tagdir="/WEB-INF/tags" %>
<%@taglib prefix="dash" tagdir="/WEB-INF/tags/dashboard/subjectcoordinator" %>
<jsgen:replaceHtml targetElement="subject-schedule">
    <dash:subjectCoordinatorSchedule/>
</jsgen:replaceHtml>