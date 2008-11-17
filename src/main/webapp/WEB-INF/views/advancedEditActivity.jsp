<%@ taglib prefix="tags" tagdir="/WEB-INF/tags" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@taglib prefix="commons" uri="http://bioinformatics.northwestern.edu/taglibs/commons" %>
<%@ taglib prefix="laf" uri="http://gforge.nci.nih.gov/projects/ctmscommons/taglibs/laf" %>

<html>
<head>
    <tags:javascriptLink name="scriptaculous/scriptaculous"/>
    <tags:javascriptLink name="activity-property/activity-property" />
    <style type="text/css">
        div.label {
            width: 35%;
        }
        div.submit {
            text-align: right;
        }
        form {
            width: 35em;
        }
        div.uriProperties {
            margin-left: 22%;
            width:100%;
        }
        .property-edit {
            float:right;
        }
    </style>
  </head>
<body>
<laf:box title="${action} Activity">
    <laf:division>
           <form:form>
            <div style="height:10px;padding-bottom:20px; width:40em; color:red;">
                <form:errors path="*"/>
            </div>
            <div class="row">
                <div class="label"><form:label path="activity.name">Activity Name</form:label></div>
                <div class="value">
                    <form:input path="activity.name" size="30"/>
                </div>
            </div>
            <div class="row">
                <div class="label"><form:label path="activity.code">Activity Code</form:label></div>
                <div class="value">
                    <form:input path="activity.code" size="30"/>
                </div>
            </div>
            <div class="row">
                <div class="label"><form:label path="activity.type">Activity Type</form:label></div>
                <div class="value">
                    <form:select path="activity.type">
                       <c:forEach items="${activityTypes}" var="activityType">
                       <c:if test="${activityType.name == activityDefaultType}">
                           <option value="${activityType.id}" selected="selected">${activityType.name}</option>
                       </c:if>
                       <c:if test="${activityType.name != activityDefaultType}">
                           <option value="${activityType.id}">${activityType.name}</option>
                       </c:if>
                    </c:forEach>
                    </form:select>
                </div>
            </div>
            <div class="row">
                <div class="label"><form:label path="activity.description">Activity Description</form:label></div>
                <div class="value">
                    <form:input path="activity.description" size="30"/>
                </div>
            </div>
            <div class="row">
            <div class="label">URI</div>
            <div class="uriProperties" id="uriProperties">
                <table width="100%" id="propertyTable" class="propertyTable">
                    <c:forEach items="${existingList}" var="list" varStatus="status">
                           <tr class=" ${commons:parity(status.count)} property" id="oldUri" width="100%">
                               <td>
                                   <input name="listKey" class="listKey" id="listKey" type="hidden" value="${list.key}" />
                                 <div class='property-content'>
                                    <span class="textName">Text: </span>
                                    <span class='textValue'>
                                        ${list.value.textValue}
                                    </span>
                                    <br />
                                    <span class="templateName">Template:</span>
                                    <span class='templateValue'>
                                        ${list.value.templateValue}
                                    </span>
                                    <a class="property-edit" id="property-edit" href="#property-edit">
                                    Edit
                                    </a>
                                  </div>
                                 </td>
                        </tr>
                    </c:forEach>
                </table>
           </div>
           <div class="value"><a class="newUriBtn" id="newUriBtn" href='#newUriBtn'>New Uri</a> </div>
           </div>
         <div class="row submit">
                <input type="submit" value="Save"/>
        </div>
        </form:form>
    </laf:division>
</laf:box>
<div id="lightbox">
    <div id="edit-property-lightbox">
        <h1>Editing Activity Uri</h1>
        <div class="row">
            <div class="label">
                <label for="edit-property-textValue">Text</label>
            </div>
            <div class="value">
                <input type="text" class="text" id="edit-property-textValue" hint="None" size="30"/>
            </div>
        </div>
        <div class="row">
            <div class="label">
                <label for="edit-property-templateValue">Template</label>
            </div>
            <div class="value">
                <input type="text" class="text" id="edit-property-templateValue" hint="None" size="30" />
            </div>
        </div>
        <div class="row">
            <div class="submit">
                <input type="button" value="Done" id="edit-property-done"/>
            </div>
        </div>
    </div>
</div>
</body>
</html>