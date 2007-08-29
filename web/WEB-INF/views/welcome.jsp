<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="laf" uri="http://gforge.nci.nih.gov/projects/ctmscommons/taglibs/laf" %>

<html>
<head>
    <style type="text/css">
        #roles-swimlanes {
            display: block;
            margin: 1em auto;
        }

        p.demo-link {
            background-color: #cfc;
            border: 1px solid #9c6;
            margin: 1em 3em;
            padding: 1em;
            font-size: 1.2em;
        }

        table {
            border-collapse: collapse;
            border: 1px solid #9f6;
            margin: 1em auto;
        }

        td, th {
            border: 1px solid #9c6;
            padding: 3px;
        }

        th {
            background-color: #cfc
        }
    </style>
</head>


<body>
<laf:box title="Patient Study Calendar Phase II - Public Test Site">
    <laf:division>
<!--<h1>Patient Study Calendar - Public Test Site</h1>-->

<p>
    Thank you for testing the Patient Study Calendar.  The software you are about to test is the result of the Elaboration Phase. Your feedback will help guide our development efforts.
</p>

<p>
    Two considerations before you begin:
</p>
<ol>
    <li>This is a public instance. You may encounter sample study calendars created by other users.
        Other users will be able to see data that you enter. Please do not enter any confidential
        information.</li>
    <li>With each release, we may delete information from our database in order to prevent the site from getting cluttered with incomplete templates and abandoned calendars.</li>
</ol>

<p class="demo-link">
    Let the testing begin: <a href="<c:url value="/pages/cal/studyList"/>" target="_blank">Public Test Site - start
    page</a>
</p>

<h2>To test the creation and management of study templates:</h2>

<ol>
    <li>Login using <kbd>superuser</kbd> as both the username and password.</li>
    <li>Click "Create New Study Template" to begin the process of creating a new study.</li>
    <li>You will be presented with a blank study.  Using the on-screen buttons, rename the study, add and rename epochs, and add arms.</li>
    <li>You can also reorder the epochs and arms.</li>
    <li>When you click on an arm, you can add periods of time to that arm.  You can then select the periods to add activities.</li>
    <li>Click "Mark this template as complete" when you have finished creating the template.</li>
	<li>Because a template must be associated with a site before participants can be added, find your template in the list of completed templates and click the "Assign Sites" link next to the template.  Assign a Site to your template.</li>  
    <li>You can click on your template in the list of "Completed Templates" and then click "Assign Participants" within the Template view.</li>
    <li>Please send feedback to <a href="mailto:s-whitaker@northwestern.edu">s-whitaker@northwestern.edu</a></li>
</ol>

<h2>To test the creation and management of participant calendars:</h2>

<ol>
	<li>Find a template that is listed under "Completed templates".</li>
	<li>If "Assign sites" is your only option, select that and assign access to the template to a site.</li>
	<li>Otherwise, select "Assign participants" and follow the steps in order to generate a calendar.</li>
	<li>To view or manage calendars that already exist, simply click on the template and then select the patient's calendar from the list.</li>
	<li>Once inside a patient's calendar, try rescheduling, canceling, and marking activities as having occurred.  These actions can be done individually or to groups of activities using the "Batch reschedule" option.</li>
	<li>Please send feedback to <a href="mailto:s-whitaker@northwestern.edu">s-whitaker@northwestern.edu</a></li>
</ol>
	

<h2>New features on this site:</h2>

<ol>
	<li>The user can mark events on the template as being conditional and supply the condition.  These are then available to be scheduled on a patient's calendar.</li>
	<li>Activities added to a period are now automatically saved.</li>
	<li>The user can add a reconsent to the schedule of every patient calendar on a particular study.</li>
	<li>The user can hide or show the activities on the template.</li>
	<li>In a patient's calendar, the user can use checkboxes to select any number of activities and change their state or reschedule them.</li>
	<li>A new look and feel has been adopted to more closely match other applications within the CTMS Workspace.</li>
</ol>


<p class="demo-link">
    In case you missed the first one:
    <a href="<c:url value="/pages/cal/studyList"/>" target="_blank">Public Test Site - start page</a>
</p>

<p>
    Thank you for your participation. Please send feedback to <a
    href="mailto:s-whitaker@northwestern.edu">s-whitaker@northwestern.edu</a>.
</p>
    </laf:division>
</laf:box>
</body>
</html>