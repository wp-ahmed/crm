
!function($) {
function convertStringToDate(stringDate) {
    var dateParts = stringDate.split("T")[0].split("-");
    var timeParts = stringDate.split("T")[1].split(":");
    var year = parseInt(dateParts[0]);
    var month = parseInt(dateParts[1]) - 1; // Months are zero-based
    var day = parseInt(dateParts[2]);
    var hour = parseInt(timeParts[0]);
    var minute = parseInt(timeParts[1]);
    var second = parseInt(timeParts[2]);

    var date = new Date(year, month, day, hour, minute, second);
    var momentDate = moment(date);
    return momentDate;
}
function initializePickers() {

        $('.clockpicker').clockpicker({
            autoclose: true,
            donetext: 'Done',
        }).find('input').change(function() {

        });

        // Date Picker
        jQuery('.mydatepicker').datepicker({
            autoclose: true,
            todayHighlight: true,
            format: 'yyyy-mm-dd'
        });
        var input = document.querySelector('#email-input');
        var tagify = new Tagify(input, {
        <!--    enforceWhitelist: true,-->
            whitelist: [], // Specify a predefined list of email suggestions if desired
            maxTags: Infinity, // Set the maximum number of email tags
            backspace: 'edit', // Allow editing of tags using the backspace key
            placeholder: 'Enter emails...', // Placeholder text for the input field
            dropdown: {
                enabled: 0, // Disable the email suggestions dropdown
            },
        });
            // Update the email input value when tags are added or removed
            tagify.on('add', e => updateInputValue());
            tagify.on('remove', e => updateInputValue());

        function updateInputValue() {
            var emails = tagify.value.map(tag => tag.value);
            document.getElementById('emails').value = emails.join(',');
        }
    }
    function updateCalendarEvent($this, info){
        var startInfo = info.start._d;
        var startInfoTime = info.start._i;
        var startInfoTimeHr;
        var startInfoTimeMin;
        var startInfoTimeDay;
        if(jQuery.isArray(startInfoTime)){
            startInfoTimeHr = startInfoTime[3];
            startInfoTimeMin = startInfoTime[4];
            startInfoTimeDay = startInfoTime[2];
        } else {
            startInfoTimeHr = startInfoTime.getHours();
            startInfoTimeMin = startInfoTime.getMinutes();
            startInfoTimeDay = startInfo.getDate();
        }
        var gmtOffsetHours = Math.abs(Math.floor(startInfo.getTimezoneOffset() / 60));
        var gmtOffsetMinutes = Math.abs(startInfo.getTimezoneOffset() % 60);
        var gmtOffsetString = (startInfo.getTimezoneOffset() < 0 ? "+" : "-") + gmtOffsetHours.toString().padStart(2, '0') + ":" + gmtOffsetMinutes.toString().padStart(2, '0');

        var realMonth = startInfo.getMonth() + 1;

        var hr = startInfoTimeHr.toString().length < 2 ? "0" + startInfoTimeHr.toString() : startInfoTimeHr.toString();
        var min = startInfoTimeMin.toString().length < 2 ? "0" + startInfoTimeMin.toString() : startInfoTimeMin.toString();
        var month = realMonth.toString().length < 2 ? "0" + realMonth.toString() : realMonth.toString();
        var day = startInfoTimeDay.toString().length < 2 ? "0" + startInfoTimeDay.toString() : startInfoTimeDay.toString();

        var startTime = startInfo.getFullYear() + "-"+month+"-"+day+"T"+hr+":"+min+":00"+gmtOffsetString;

        var endInfo = info.end._d;
        var endInfoTime = info.end._i;

        var gmtOffsetHoursEnd = Math.abs(Math.floor(endInfo.getTimezoneOffset() / 60));
        var gmtOffsetMinutesEnd = Math.abs(endInfo.getTimezoneOffset() % 60);
        var gmtOffsetStringEnd = (endInfo.getTimezoneOffset() < 0 ? "+" : "-") + gmtOffsetHoursEnd.toString().padStart(2, '0') + ":" + gmtOffsetMinutesEnd.toString().padStart(2, '0');

        var endInfoTimeHr;
        var endInfoTimeMin;
        var endInfoTimeDay;
        if(jQuery.isArray(endInfoTime)){
            endInfoTimeHr = endInfoTime[3];
            endInfoTimeMin = endInfoTime[4];
            endInfoTimeDay = endInfoTime[2];
        } else {
            endInfoTimeHr = endInfoTime.getHours();
            endInfoTimeMin = endInfoTime.getMinutes();
            endInfoTimeDay = endInfoTime.getDate();
        }

        realMonth = endInfo.getMonth() + 1;

        var hrEnd = endInfoTimeHr.toString().length < 2 ? "0" + endInfoTimeHr.toString() : endInfoTimeHr.toString();
        var minEnd = endInfoTimeMin.toString().length < 2 ? "0" + endInfoTimeMin.toString() : endInfoTimeMin.toString();
        var monthEnd = realMonth.toString().length < 2 ? "0" + realMonth.toString() : realMonth.toString();
        var dayEnd = endInfoTimeDay.toString().length < 2 ? "0" + endInfoTimeDay.toString() : endInfoTimeDay.toString();

        var endTime = startInfo.getFullYear() + "-"+monthEnd+"-"+dayEnd+"T"+hrEnd+":"+minEnd+":00"+gmtOffsetStringEnd;

        var eventId = info.id;

        let formData = new FormData();
        formData.append('id',eventId);
        formData.append('summary',info.title);
        formData.append('startTime',startTime);
        formData.append('endTime',endTime);

        $.ajax({
               type: 'POST',
               url: home+'employee/calendar/ajax-update-event',
               data: formData,
               processData: false,
               contentType: false,
                       headers: {
                           [csrfHeaderName]: csrfToken
                       },
               success: function (response) {
                    $("#summary-error").text("");
                    if($this != null){
                    $this.$calendarObj.fullCalendar('updateEvent', info);}
                    $('#my-event').modal('hide');
               },
               error: function (xhr, status, error) {
                    $("#summary-error").text(xhr.responseText)
               }
           });
    }
    function createCalendarEvent($this, fullStartDate, fullEndDate, summary, emails, offset) {
        let formData = new FormData();
        formData.append('fullStartDate',fullStartDate+offset);
        formData.append('fullEndDate',fullEndDate+offset);
        formData.append('summary',summary);
        formData.append('emails',emails);
        var x = convertStringToDate(fullStartDate);
        var y = convertStringToDate(fullEndDate);
        var id;
        $.ajax({
               type: 'POST',
               url: home+'employee/calendar/ajax-create-event',
               data: formData,
               processData: false,
               contentType: false,
                       headers: {
                           [csrfHeaderName]: csrfToken
                       },
               success: function (response) {
                    $("#summary-error").text("");
                    $("#time-error").text("");
                    $this.$calendarObj.fullCalendar('renderEvent', {
                        id: response,
                        title: summary,
                        start:x,
                        end: y,
                        className: "bg-info"
                    }, true);
                    $('#my-event').modal('hide');
               },
               error: function (xhr, status, error) {

                   var errorResponse = JSON.parse(xhr.responseText);
                   if (errorResponse.hasOwnProperty('time')) {
                        $("#time-error").text(errorResponse.time);
                   }
                   if (errorResponse.hasOwnProperty('summary')) {
                        $("#summary-error").text(errorResponse.summary);
                   }
               }
           });
        return id;
    }
    "use strict";
    var csrfToken = document.querySelector('script[data-csrf-token]').getAttribute('data-csrf-token');
    var csrfHeaderName = document.querySelector('script[data-csrf-header]').getAttribute('data-csrf-header');
    var CalendarApp = function() {
        this.$body = $("body")
        this.$calendar = $('#calendar'),
        this.$event = ('#calendar-events div.calendar-events'),
        this.$categoryForm = $('#add-new-event form'),
        this.$extEvents = $('#calendar-events'),
        this.$modal = $('#my-event'),
        this.$saveCategoryBtn = $('.save-category'),
        this.$calendarObj = null
    };

    /* on drop */
    CalendarApp.prototype.onDrop = function (eventObj, date) { 
        var $this = this;

            // retrieve the dropped element's stored Event Object
            var originalEventObject = eventObj.data('eventObject');
            var $categoryClass = eventObj.attr('data-class');
            // we need to copy it, so that multiple events don't have a reference to the same object
            var copiedEventObject = $.extend({}, originalEventObject);
            // assign it the date that was reported
            copiedEventObject.start = date;
            if ($categoryClass)
                copiedEventObject['className'] = [$categoryClass];
            // render the event on the calendar
            $this.$calendar.fullCalendar('renderEvent', copiedEventObject, true);

            // is the "remove after drop" checkbox checked?
            if ($('#drop-remove').is(':checked')) {
                // if so, remove the element from the "Draggable Events" list
                eventObj.remove();
            }
    },
    /* on click on event */
    CalendarApp.prototype.onEventClick =  function (calEvent, jsEvent, view) {
        var $this = this;
            var form = $("<form></form>");
            form.append("<label>Change event name</label>");
            form.append("<div class='input-group'><input class='form-control' type=text value='" + calEvent.title + "' /><span class='input-group-btn'><button type='submit' class='btn btn-success waves-effect waves-light'><i class='fa fa-check'></i> Save</button></span></div><div class='input-group'><span id='summary-error' class='text-danger font-weight-bold'></span></div>");
            $this.$modal.modal({
                backdrop: 'static'
            });
            $this.$modal.find('.delete-event').show().end().find('.save-event').hide().end().find('.modal-body').empty().prepend(form).end().find('.delete-event').unbind('click').click(function () {
                $this.$calendarObj.fullCalendar('removeEvents', function (ev) {
                    return (ev._id == calEvent._id);
                });
                let formData = new FormData();
                formData.append('id',calEvent.id);
                $.ajax({
                       type: 'POST',
                       url: home+'employee/calendar/ajax-delete-event',
                       data: formData,
                       processData: false,
                       contentType: false,
                               headers: {
                                   [csrfHeaderName]: csrfToken
                               },
                       success: function (response) {
                       },
                       error: function (xhr, status, error) {

                       }
                   });
                $this.$modal.modal('hide');
            });
            $this.$modal.find('form').on('submit', function () {
                calEvent.title = form.find("input[type=text]").val();
                updateCalendarEvent($this, calEvent);
                return false;
            });

    },
    /* on select */
    CalendarApp.prototype.onSelect = function (start, end) {
        var $this = this;
        let x = `
             <label class="m-t-20" for="summary">Summary:</label>
             <div class="input-group">
                 <input type="text" id="summary" name="summary" class="form-control">
             </div>
             <div class="input-group">
                <span id="summary-error" class="text-danger font-weight-bold"></span>
             </div>
             <label class="m-t-20" for="startTime">Start Time:</label>
             <div class="input-group clockpicker">
                 <input id="startTime" type="text" name="startTime" class="form-control" value="09:30">
                 <div class="input-group-append">
                     <span class="input-group-text"><i class="fa fa-clock-o"></i></span>
                 </div>
             </div>
             <label class="m-t-20" for="endTime">End Time:</label>
             <div class="input-group clockpicker">
                 <input id="endTime" type="text" name="endTime" class="form-control" value="09:30">
                 <div class="input-group-append">
                     <span class="input-group-text"><i class="fa fa-clock-o"></i></span>
                 </div>
             </div>
             <div class="input-group">
                <span id="time-error" class="text-danger font-weight-bold"></span>
             </div>
             <label for="email-input" class="m-t-20">Attendees</label>
             <div class="input-group">
                 <input id="email-input" class="form-control">
                 <input type="hidden" id="emails" name="emails">
             </div>`;

             //in case the user already choose a time slot in the calendar app, so no need to add the time input
             if(jQuery.isArray(start._i)){
                x = `<label class="m-t-20" for="summary">Summary:</label>
                     <div class="input-group">
                         <input type="text" id="summary" name="summary" class="form-control">
                     </div>
                     <div class="input-group">
                        <span id="summary-error" class="text-danger font-weight-bold"></span>
                     </div>
                     <label for="email-input" class="m-t-20">Attendees</label>
                     <div class="input-group">
                         <input id="email-input" class="form-control">
                         <input type="hidden" id="emails" name="emails">
                     </div>`;
             }
            $this.$modal.modal({
                backdrop: 'static'
            });
            var form = $("<form action='' method='post'></form>");
            form.append("<div class='row'></div>");
            form.find(".row").append(x);

            $this.$modal.find('.delete-event').hide().end().find('.save-event').show().end().find('.modal-body').empty().prepend(form).end().find('.save-event').unbind('click').click(function () {
                form.submit();
            });
            $this.$modal.find('form').on('submit', function () {
                var currentDate = new Date();
                var timezoneOffsetMinutes = currentDate.getTimezoneOffset();
                var timezoneOffsetHours = Math.abs(Math.floor(timezoneOffsetMinutes / 60));
                timezoneOffsetHours = timezoneOffsetHours.length > 1 ? timezoneOffsetHours : "0"+timezoneOffsetHours;
                var timezoneOffsetSign = timezoneOffsetMinutes > 0 ? '-' : '+';
                var currentGMTOffset = `${timezoneOffsetSign}${timezoneOffsetHours}` + ":00";

                var title = form.find("input[name='summary']").val();
                var startTime = form.find("input[name='startTime']").val();
                var endTime = form.find("input[name='endTime']").val();
                var emails = form.find("input[name='emails']").val();
                var timeDefined = false;
                if(jQuery.isArray(start._i)){
                    var startInfoTimeHr = start._i[3];
                    var startInfoTimeMin = start._i[4];
                    var hrStart = startInfoTimeHr.toString().length < 2 ? "0" + startInfoTimeHr.toString() : startInfoTimeHr.toString();
                    var minStart = startInfoTimeMin.toString().length < 2 ? "0" + startInfoTimeMin.toString() : startInfoTimeMin.toString();
                    startTime = hrStart+":"+minStart;

                    var endInfoTimeHr = end._i[3];
                    var endInfoTimeMin = end._i[4];
                    var hrEnd = endInfoTimeHr.toString().length < 2 ? "0" + endInfoTimeHr.toString() : endInfoTimeHr.toString();
                    var minEnd = endInfoTimeMin.toString().length < 2 ? "0" + endInfoTimeMin.toString() : endInfoTimeMin.toString();
                    endTime = hrEnd+":"+minEnd;

                    timeDefined = true;
                }

                var startInfo = start._d;
                var realMonth = startInfo.getMonth() + 1;
                var startDate = startInfo.getFullYear().toString() + "-" + (realMonth.toString().length > 1 ? realMonth.toString() : "0"+realMonth.toString()) +
                                "-" + (startInfo.getDate().toString().length > 1 ? startInfo.getDate().toString() : "0" +startInfo.getDate().toString());
                var fullStart = startDate + "T" + startTime.toString() + ":00";
                var fullEnd = startDate + "T" + endTime.toString() + ":00";
                var x = convertStringToDate(fullStart);
                var y = convertStringToDate(fullEnd);

                var id = createCalendarEvent($this, fullStart, fullEnd, title, emails, currentGMTOffset);
                return false;
                
            });
            $this.$calendarObj.fullCalendar('unselect');
            initializePickers();
    },
    CalendarApp.prototype.onResize = function(info){
        updateCalendarEvent(null,info);
    },
    CalendarApp.prototype.onEventDrop = function(info) {
        updateCalendarEvent(null,info);
    },
    CalendarApp.prototype.enableDrag = function() {
        //init events
        $(this.$event).each(function () {
            // create an Event Object (http://arshaw.com/fullcalendar/docs/event_data/Event_Object/)
            // it doesn't need to have a start or end
            var eventObject = {
                title: $.trim($(this).text()) // use the element's text as the event title
            };
            // store the Event Object in the DOM element so we can get to it later
            $(this).data('eventObject', eventObject);
            // make the event draggable using jQuery UI
            $(this).draggable({
                zIndex: 999,
                revert: true,      // will cause the event to go back to its
                revertDuration: 0  //  original position after the drag
            });
        });
    },
    /* Initializing */
    CalendarApp.prototype.init = function() {
        this.enableDrag();
        /*  Initialize the calendar  */
        var date = new Date();
        var d = date.getDate();
        var m = date.getMonth();
        var y = date.getFullYear();
        var form = '';
        var today = new Date($.now());
        var events = [];
        if (eventDisplays) {
            $.each(eventDisplays, function(index, eventDisplay) {
                // Access object properties and perform actions
                var startDate = new Date(eventDisplay.startDate + "T" + eventDisplay.startTime + eventDisplay.timeZone);
                var endDate = new Date(eventDisplay.endDate + "T" + eventDisplay.endTime + eventDisplay.timeZone);
                var event = {
                    id: eventDisplay.id,
                    timeZone: eventDisplay.timeZone,
                    title: eventDisplay.summary,
                    start: startDate,
                    end: endDate,
                    className: 'bg-info'
                }
                events.push(event);
                // Perform additional actions as needed
            });
        }

        var $this = this;
        $this.$calendarObj = $this.$calendar.fullCalendar({
            slotDuration: '00:15:00', /* If we want to split day time each 15minutes */
            minTime: '08:00:00',
            maxTime: '19:00:00',  
            defaultView: 'month',  
            handleWindowResize: true,   
             
            header: {
                left: 'prev,next today',
                center: 'title',
                right: 'month,agendaWeek,agendaDay'
            },
            events: events,
            editable: true,
            droppable: true, // this allows things to be dropped onto the calendar !!!
            eventLimit: true, // allow "more" link when too many events
            selectable: true,
            drop: function(date) { $this.onDrop($(this), date); },
            eventDrop: function(info){$this.onEventDrop(info);},
            select: function (start, end) { $this.onSelect(start, end); },
            eventClick: function(calEvent, jsEvent, view) { $this.onEventClick(calEvent, jsEvent, view); },
            eventResize: function(info){$this.onResize(info);}

        });

        //on new event
        this.$saveCategoryBtn.on('click', function(){
            var categoryName = $this.$categoryForm.find("input[name='category-name']").val();
            var categoryColor = $this.$categoryForm.find("select[name='category-color']").val();
            if (categoryName !== null && categoryName.length != 0) {
                $this.$extEvents.append('<div class="calendar-events" data-class="bg-' + categoryColor + '" style="position: relative;"><i class="fa fa-circle text-' + categoryColor + '"></i>' + categoryName + '</div>')
                $this.enableDrag();
            }

        });
    },

   //init CalendarApp
    $.CalendarApp = new CalendarApp, $.CalendarApp.Constructor = CalendarApp
    
}(window.jQuery),

//initializing CalendarApp
function($) {
    "use strict";
    $.CalendarApp.init()
}(window.jQuery);