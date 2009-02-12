<%@page contentType="text/javascript" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
function createAllStudySegmentControls() {
    $$('#epochs li.studySegment').each(function(li) {
        createStudySegmentControls(li)
    })
    $$('#epochs ul').each(function(ul) {
        _updateAllStudySegmentsControlVisibility(ul.id)
    })
}

function createStudySegmentControls(studySegmentItem) {
    var studySegmentA = studySegmentItem.getElementsByTagName("A")[0];
    var studySegmentId = studySegmentA.id.substring('studySegment'.length+1)
    var controlBox = Builder.node("div", {className: 'studySegment-controls controls'});
    studySegmentItem.appendChild(controlBox)

    var renameControl = createRenameControl('studySegment', studySegmentId, null, 'study segment')
    SC.inPlaceEdit(studySegmentA, renameControl.href, {
        externalControl: renameControl,
        externalControlOnly: true,
        highlight: false,
        clickToEditText: studySegmentA.title
    })

    var deleteControl = createDeleteControl('studySegment', studySegmentId, 'study segment')
    Event.observe(deleteControl, "click", deleteHandler(function() {
            return "Are you sure you want to delete the study segment '" + studySegmentA.innerHTML +
                "'?  This will permanently remove it, all its periods, and its events.  " +
                "\n\nThis action cannot be undone."
        }, deleteControl.href
    ))

    var moveUpControl   = createMoveControl(-1, '&#9650;', 'studySegment', studySegmentId, 'study segment')
    SC.asyncLink(moveUpControl, {}, "epochs-indicator")
    var moveDownControl = createMoveControl( 1, '&#9660;', 'studySegment', studySegmentId, 'study segment')
    SC.asyncLink(moveDownControl, {}, "epochs-indicator")

    controlBox.appendChild(moveUpControl)
    controlBox.appendChild(renameControl)
    controlBox.appendChild(deleteControl)
    controlBox.appendChild(moveDownControl)

    updateStudySegmentControlVisibility('studySegment-' + studySegmentId + '-item')
}

function updateStudySegmentControlVisibility(studySegmentItem) {
    var thisStudySegment = $(studySegmentItem)
    var siblings = $A(thisStudySegment.parentNode.getElementsByTagName("LI"))

    updateMoveControlVisibility('studySegment', thisStudySegment.id.split('-')[1], thisStudySegment, siblings)
}

function updateAllStudySegmentsControlVisibility(epochId) {
    _updateAllStudySegmentsControlVisibility('epoch-' + epochId + '-studySegments')
}

function _updateAllStudySegmentsControlVisibility(eltId) {
    var studySegmentItems = $$('#' + eltId + ' li');
    studySegmentItems.each(updateStudySegmentControlVisibility)
    updateDeleteControlVisibility('studySegment', studySegmentItems, eltId);
    if (studySegmentItems.length <= 1) {
        $$('#' + eltId + ' li .studySegment-rename-control').each(function(e) { e.hide() })
    } else {
        $$('#' + eltId + ' li .studySegment-rename-control').each(function(e) { e.show() })
    }
}

function createAddEpochControl() {
    var studyId = ${param.study}
    var addEpochControl = createAddControl("Add epoch", 'study', studyId, 'study')
    SC.asyncLink(addEpochControl, {}, "epochs-indicator")
    $('addEpoch').appendChild(addEpochControl)
}

function addToBeginSentence(){
    var studyId = ${param.study}
    var controlBox = Builder.node("span", {className: 'study-controls controls'})
    var infoSentence = "To begin, "
    var renameButton = createRenameControl('study', studyId, "enter", 'study')
    var endOfInfoSentence = " protocol identifier."

    Event.observe(renameButton, "click", function(e) {
        Event.stop(e)
        SC.inPlaceEditors["study-name"].enterEditMode()
    })
    var h1BeginSentence = Builder.node("span", {})
    h1BeginSentence.innerHTML = infoSentence
    var h1EndSentence = Builder.node("span", {})
    h1EndSentence.innerHTML = endOfInfoSentence

    controlBox.appendChild(h1BeginSentence)
    controlBox.appendChild(renameButton)
    controlBox.appendChild(h1EndSentence)
    $('enterStudyNameSentence').appendChild(controlBox)
}

function hideShowReleaseTemplateButton() {
    if ($('study-name').innerHTML.startsWith('[ABC ')) {
        $('enterStudyName').show()
        $('errorMessages').hide()
        $('admin-options').hide()
    } else {
       if ($('errorMessages').empty()) {
            $('enterStudyName').hide()
            $('errorMessages').hide()
            $('admin-options').show()
       } else {
            $('errorMessages').show()
            $('admin-options').hide()
            $('enterStudyName').hide()
       }
    }
}

function createStudyControls() {
    var h1 = $$("h1")[0];
    var studyId = ${param.study}
    var controlBox = Builder.node("span", {className: 'study-controls controls'})
    h1.appendChild(controlBox)

    var renameControl = createRenameControl('study', studyId, "Set protocol identifier", 'study')
    SC.inPlaceEdit("study-name", renameControl.href, {
        externalControl: renameControl,
        clickToEditText: "Click to rename", onComplete:function() {hideShowReleaseTemplateButton()}
    })

    controlBox.appendChild(renameControl)
}

function createAllEpochControls() {
    $$("#epochs h4").each(createEpochControls)
}

function createEpochControls(epochH4) {
    var controlBox = Builder.node("div", {className: 'epoch-controls controls'});
    epochH4.appendChild(controlBox);
    var epochId = epochH4.id.split('-')[1]
    var epochName = $('epoch-' + epochId + '-name')

    var addStudySegmentControl = createAddControl("Add segment", 'epoch', epochId, 'epoch')
    SC.asyncLink(addStudySegmentControl, {}, "epochs-indicator")

    var renameControl = createRenameControl('epoch', epochId, null, 'epoch')
    SC.inPlaceEdit(epochName, renameControl.href, {
        externalControl: renameControl,
        clickToEditText: "Click to rename"
    })

    var deleteControl = createDeleteControl('epoch', epochId, 'epoch')
    Event.observe(deleteControl, "click", deleteHandler(function() {
            return "Are you sure you want to delete the epoch '" + epochName.innerHTML +
                "'?  This will permanently remove it, all its study segments, its periods, and its events. " +
                "\n\nThis action cannot be undone."
        }, deleteControl.href
    ));

    var moveUpControl   = createMoveControl(-1, '&#9668;', 'epoch', epochId, 'epoch')
    SC.asyncLink(moveUpControl,   {}, "epochs-indicator")
    var moveDownControl = createMoveControl( 1, '&#9658;', 'epoch', epochId, 'epoch')
    SC.asyncLink(moveDownControl, {}, "epochs-indicator")

    controlBox.appendChild(moveUpControl)
    controlBox.appendChild(addStudySegmentControl)
    controlBox.appendChild(renameControl)
    controlBox.appendChild(deleteControl)
    controlBox.appendChild(moveDownControl)

    updateEpochControlVisibility('epoch-' + epochId)
}

function updateEpochControlVisibility(epochElt) {
    var thisEpoch = $(epochElt)
    var siblings = $$('div.epoch')

    updateMoveControlVisibility('epoch', thisEpoch.id.split('-')[1], thisEpoch, siblings)
    updateDeleteControlVisibility('epoch', siblings, 'epochs')
}

function updateAllEpochsControlVisibility() {
    $$('div.epoch').each(updateEpochControlVisibility)
}


function createRenameControl(objectType, objectId, name, nameOfTheElement) {
    if(name == null) {
        name ="Set name"
    }
    return createControlAnchor("rename", name, "Change the name of this " + nameOfTheElement, '<c:url value="/pages/cal/template/rename"/>', objectType, objectId)
}

function createDeleteControl(objectType, objectId, nameOfTheElement) {
    return createControlAnchor("delete", "Delete", "Delete this " + nameOfTheElement, '<c:url value="/pages/cal/template/delete"/>', objectType, objectId)
}

function createAddControl(text, objectType, objectId, nameOfTheElement) {
    return createControlAnchor("add", text, "Add to this " + nameOfTheElement, '<c:url value="/pages/cal/template/addTo"/>', objectType, objectId)
}

function createMoveControl(offset, text, objectType, objectId, nameOfTheElement) {
    return createControlAnchor("move" + offset, text, "Reorder the " + nameOfTheElement + "s", '<c:url value="/pages/cal/template/move?offset="/>' + offset, objectType, objectId)
}

function createControlAnchor(controlName, text, title, baseHref, objectType, objectId) {
    var href = baseHref;
    if (href.indexOf('?') >= 0) {
        href += '&'
    } else {
        href += '?'
    }
    href += objectType + '=' + objectId
    if (objectType != 'study') href += '&study=${param.study}'
    var a = Builder.node("a", {
        className: objectType + '-' + controlName + '-control ' + objectType + '-control control',
        id: objectType + "-" + objectId + "-" + controlName,
        href: href,
        title: title
    })
    a.innerHTML = text
    return a
}

function deleteHandler(confirmMessageFn, link) {
    return function(e) {
        Event.stop(e);
        if (window.confirm(confirmMessageFn())) {
            $('epochs-indicator').reveal()
            new Ajax.Request(link, {
                onComplete: function() {
                    $('epochs-indicator').conceal()
                }
            })
        }
    }
}

function updateMoveControlVisibility(objectType, objectId, thisElement, siblings) {
    var isFirst = thisElement == siblings[0]
    var isLast = thisElement == siblings.last()

    var downControl = $(objectType + '-' + objectId + '-move1')
    if (isLast) {
        downControl.conceal();
    } else {
        downControl.reveal();
    }

    var upControl = $(objectType + '-' + objectId + '-move-1' )
    if (isFirst) {
        upControl.conceal();
    } else {
        upControl.reveal();
    }

    if (isFirst && isLast) {
        upControl.hide(); downControl.hide();
    } else {
        upControl.show(); downControl.show();
    }
}

function updateDeleteControlVisibility(objectType, elts, containerId) {
    var controls = $$('#' + containerId + ' .' + objectType + '-delete-control')
    if (elts.length <= 1) {
        controls.each(function(c) { c.hide() })
    } else {
        controls.each(function(c) { c.show() })
    }
}