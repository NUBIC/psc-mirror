<%@tag %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@attribute name="epoch"
             type="edu.northwestern.bioinformatics.studycalendar.domain.Epoch" %>

<c:forEach items="${epoch.studySegments}" var="studySegment">
    <ul class="row">
        <div class="row odd">
            <label>${studySegment.name}</label>
        </div>
        <c:forEach items="${studySegment.periods}" var="period">
            <div class="row even">
                <input type="checkbox" id="${period.id}" name="periodName" value="false" onclick="unselectPeriodBox(this)">
                <label>${period.displayNameWithActivities}</label>

            </div>
        </c:forEach>
    </ul>

</c:forEach>
