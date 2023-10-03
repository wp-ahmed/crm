$(document).ready(function() {
    $('.trigger-emails').each(function() {
        if ($(this).is(':checked')) {
            var name = $(this).data('email');
//            console.log("e");
            var modifiedStr = name.replace(/\s/g, '_');
            var selectElement = $("<select name='" + modifiedStr + "_email_template' class='form-control'>");
            var optionElement = $('<option>').text("Pick email template").attr('value', '');
            selectElement.append(optionElement);

            $.each(emailTemplates, function(index, item) {
                modifiedStr = name.replace(/(?:^\w|[A-Z]|\b\w|\s+)/g, function(match, index) {
                    if (+match === 0) return ""; // Remove spaces
                    return index === 0 ? match.toLowerCase() : match.toUpperCase();
                });
                var d = modifiedStr+"EmailTemplate";
                if(emailSettings[d]== item.templateId || emailSettings[d].templateId == item.templateId){
                    optionElement = $('<option>').text(item.name).attr('value', item.templateId).attr('selected','selected');
                }else{
                    optionElement = $('<option>').text(item.name).attr('value', item.templateId);
                }
                selectElement.append(optionElement);
            });

            var html = `
                <div class="email-template m-t-10 col-md-6" style="
                    border: 1px #fb9678;
                    border-radius: 5px;
                    padding: 16px;
                    box-shadow: 2px 2px 2px 2px #fb967842;
                    ">
                    <div class="col-md-12">
                        ${selectElement.prop('outerHTML')}
                        <small>To create a new email template, use this <a href=${home}'employee/email-template/create'>link</a></small>
                    </div>
                </div>`;

            var id = $(this).attr('id');
            $(this).parent().append(html);
            $(this).parent().find(".email-template .check").attr("name", id);
        }
    });
    $('.trigger-emails').change(function() {
        if ($(this).is(':checked')) {
                var name = $(this).data('email');
                var modifiedStr = name.replace(/\s/g, '_');
                var selectElement = $("<select name='" + modifiedStr + "_email_template' class='form-control'>");
                var optionElement = $('<option>').text("Pick email template").attr('value', '');
                selectElement.append(optionElement);

                $.each(emailTemplates, function(index, item) {
                    optionElement = $('<option>').text(item.name).attr('value', item.templateId);
                    selectElement.append(optionElement);
                });

                var html = `
                    <div class="email-template m-t-10 col-md-6" style="
                        border: 1px #fb9678;
                        border-radius: 5px;
                        padding: 16px;
                        box-shadow: 2px 2px 2px 2px #fb967842;
                        ">
                        <div class="col-md-12">
                            ${selectElement.prop('outerHTML')}
                            <small>To create a new email template, use this <a href=${home}'employee/email-template/create'>link</a></small>
                        </div>
                    </div>`;

            var id = $(this).attr('id');
            $(this).parent().append(html);
            $(this).parent().find(".email-template .check").attr("name", id);
        } else {
            $(this).parent().find('.email-template').fadeOut(function() {
                $(this).remove();
            });
        }
    });
});