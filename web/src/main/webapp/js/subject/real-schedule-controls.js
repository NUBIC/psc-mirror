psc.namespace("subject");

psc.subject.RealScheduleControls = (function ($) {
  var batchResource;

  function performDelay(evt, data) {
    var params = psc.subject.RealScheduleControls.computeDelayParameters();
    executePartialScheduleUpdate(params);
  }

  function performCheckedModifications(evt, data) {
    var params = psc.subject.RealScheduleControls.computeMarkParameters();
    executePartialScheduleUpdate(params);
  }

  function executePartialScheduleUpdate(updates) {
    $('#schedule-controls .indicator').css('visibility', 'visible');
    $.ajax({
      url: batchResource,
      type: 'POST',
      data: Object.toJSON(updates),
      contentType: 'application/json',
      complete: function() {
        $('#schedule-controls .indicator').css('visibility', 'hidden');
        psc.subject.ScheduleData.refresh();
      }
    });
  }

  function checkScheduledActivitiesByClass() {
    var kind = $(this).attr('id').replace('mark-select-', '');
    var assign = $('#mark-select-assignment').val();
    var selector = "input.event";
    if ($.inArray(kind, ['all', 'none']) < 0) {
      selector += '.' + kind;
    }
    if (assign && assign !== '') {
      selector += '.assignment-' + assign.replace(/\W/g, '_');
    }
    $(selector).attr('checked', kind == 'none' ? '' : 'checked');
    return false;
  }

  function updateActivityCountMessage() {
    var count = $('input.event:checked').length;
    if (count === 0) {
      $('#mark-activities-count').
        text('There are currently no activities checked.');
    } else if (count === 1) {
      $('#mark-activities-count').
        text('There is currently 1 activity checked.');
    } else {
      $('#mark-activities-count').
        text('There are currently ' + count + ' activities checked.');
    }
  }

  function isShiftingMarkMode() {
    return $.inArray(
      $('#mark-new-mode').val(),
      ['move-date-only', 'scheduled']
    ) >= 0;
  }

  function mutateMarkForm() {
    if (isShiftingMarkMode()) {
      $('#mark-date-group').show();
    } else {
      $('#mark-date-group').hide();
    }
  }

  function shiftedDate(apiDate, shiftAmount) {
    return psc.tools.Dates.utcToApiDate(
      psc.tools.Dates.shiftByDays(
        psc.tools.Dates.apiDateToUtc(apiDate), shiftAmount));
  }

  // Map from mark modes to functions which determine the next state for an
  // SA.  If the function returns null, the SA won't be changed.
  var NEW_STATE_FNS = {
    'move-date-only': function (sa) {
      if (sa.isOpen()) {
        return sa.current_state.name;
      } else {
        return null;
      }
    },

    'scheduled': function (sa) {
      if (sa.isOpen()) {
        return 'scheduled';
      } else {
        return null;
      }
    },
    
    'canceled-or-na': function (sa) {
      if ($.inArray(sa.current_state.name, ['NA', 'conditional']) >= 0) {
        return 'NA';
      } else {
        return 'canceled';
      }
    },
    
    'occurred': function (sa) {
      return 'occurred';
    },
    
    'missed': function (sa) {
      return 'missed';
    }
  }

  return {
    init: function () {
      $('#delay-submit').click(performDelay);
      $('#mark-submit').click(performCheckedModifications);
      $('a.mark-select').click(checkScheduledActivitiesByClass).
        click(updateActivityCountMessage);
      $('input.event').live('click', updateActivityCountMessage);
      $('#mark-new-mode').change(mutateMarkForm);
      $('#toggle-plan-days').click(function () {
        $('.event-details.plan-day').toggle();
        if ($(this).text().match(/Show/)) {
          $(this).text($(this).text().replace(/Show/, 'Hide'));
        } else {
          $(this).text($(this).text().replace(/Hide/, 'Show'));
        }
        return false;
      });
    },

    batchResource: function (uri) {
      batchResource = uri;
    },

    // public for testing
    computeDelayParameters: function () {
      var delayAmount = $('#delay-amount').val() * $('#delay-or-advance').val();
      var params = {};
      var asOf = $('#delay-as-of').val() ?
        psc.tools.Dates.displayDateToUtc($('#delay-as-of').val()) :
        null;
      var onlyAssign = $('#delay-assignment').val() || null;
      $.each(psc.subject.ScheduleData.current()['days'], function (day, value) {
        if (asOf) {
          var date = psc.tools.Dates.apiDateToUtc(day);
          if (date < asOf) return true; // continue
        }
        $.each(value['activities'], function () {
          if (onlyAssign && onlyAssign !== this.assignment.id) {
            return true; // continue
          }
          if (this.isOpen()) {
            params[this['id']] = {
              state: this.current_state.name,
              date: shiftedDate(this.current_state.date, delayAmount),
              reason: $('#delay-reason').val()
            };
          }
        });
      });
      return params;
    },
    
    // public for testing
    computeMarkParameters: function () {
      var delayAmount = 
        $('#mark-delay-amount').val() * $('#mark-delay-or-advance').val();
      var params = null;
      var saIds = $('input.event:checked').
        collect(function () { return this.value; });
      $.each(psc.subject.ScheduleData.current()['days'], function (day, value) {
        $.each(value['activities'], function () {
          if ($.inArray(this.id, saIds) >= 0) {
            var newState = NEW_STATE_FNS[$('#mark-new-mode').val()](this);
            if (newState) {
              if (!params) params = {};
              params[this.id] = {
                state: newState,
                date: isShiftingMarkMode() ? 
                  shiftedDate(this.current_state.date, delayAmount) : 
                  this.current_state.date,
                reason: $('#mark-reason').val()
              }
            }
          }
        });
      });
      return params;
    }
  }
}(jQuery));