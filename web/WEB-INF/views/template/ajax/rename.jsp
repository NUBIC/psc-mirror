<%@ page contentType="text/javascript;charset=UTF-8" language="java" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="jsgen" uri="http://bioinformatics.northwestern.edu/taglibs/studycalendar/jsgenerator"%>
<%@taglib prefix="templ" tagdir="/WEB-INF/tags/template"%>
<c:if test="${not empty command.arm}">
    <jsgen:replaceHtml targetElement="arm-${command.arm.id}">${command.revisedArm.name}</jsgen:replaceHtml>
</c:if>
<c:if test="${not empty command.epoch}">
    <jsgen:replaceHtml targetElement="epoch-${command.epoch.id}-name">${command.revisedEpoch.name}</jsgen:replaceHtml>
</c:if>
<c:if test="${not empty command.study}">
    <jsgen:replaceHtml targetElement="study-name">${command.study.name}</jsgen:replaceHtml>
</c:if>
<templ:updateChanges changes="${revisionChanges}" revision="${developmentRevision}" />
