<%@taglib prefix="tags" tagdir="/WEB-INF/tags"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@attribute name="cssClass" required="false" %>
<%@attribute name="appShortName" required="true" %>
<%@attribute name="url"%>
<%@attribute name="urlTemplateProperty"%>
<%@attribute name="subjectAssigmnent"  type="edu.northwestern.bioinformatics.studycalendar.domain.StudySubjectAssignment" %>
<c:set var="href">
    <c:choose>
        <c:when test="${not empty url}">${url}</c:when>
        <c:when test="${not empty subjectAssigmnent}"><tags:urlFromTemplate subjectAssignment="${subjectAssigmnent}" property="${urlTemplateProperty}"/></c:when>
        <c:when test="${not empty urlTemplateProperty}"><tags:urlFromTemplate property="${urlTemplateProperty}"/></c:when>
        <c:otherwise>
            javascript:alert("Developer error: externalLink requires either url or urlTemplateProperty")
        </c:otherwise>
    </c:choose>
</c:set>
<a href="<c:url value="${href}"/>"
    <c:if test="${not empty cssClass}">class="${cssClass}"</c:if>
    <c:if test="${configuration.map.applicationLinksInAnotherWindow}">target="${configuration.map.applicationLinksInNewWindows ? '_blank' : appShortName}"</c:if>
    ><jsp:doBody/></a>
