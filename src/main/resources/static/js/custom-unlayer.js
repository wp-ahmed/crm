$(document).ready(function() {
    var csrfToken = document.querySelector('script[data-csrf-token]').getAttribute('data-csrf-token');
    var csrfHeaderName = document.querySelector('script[data-csrf-header]').getAttribute('data-csrf-header');
    function getMergeTags() {
        var mergeTags = {};

        for (var i = 0; i < tags.length; i++) {
            var tagName = tags[i];
            var tagValue = "{{" + tags[i] + "}}";
            mergeTags[tagName] = {
                name: tagName,
                value: tagValue
            };
        }

        return mergeTags;
    }
    unlayer.init({
        id: 'editor',
        projectId: 178963,
        mergeTags: getMergeTags()
    });
   if(emailTemplate != null){
        var design = emailTemplate.jsonDesign;
        var jsonDesign = JSON.parse(design);// template JSON
        unlayer.loadDesign(jsonDesign);
    }
    $('#saveButton').click(function() {
        var names = $("#name").val();
        if(!names.trim()) {
            window.location.href = home+"employee/email-template/create?error=name";
            return;
        }
        unlayer.exportHtml(function(data) {
            var content = data.html; // final html
            var jsonDesign = JSON.stringify(data.design);
            var dataN = JSON.parse(jsonDesign);
            if (dataN.body.rows[0].columns[0].contents.length === 0) {
                window.location.href = home+"employee/email-template/create?error=content";
                return;
            }
            $.ajax({
                type: "POST",
                url: home+"employee/email-template/create",
                processData: false,
                contentType: false,
                headers: {
                    [csrfHeaderName]: csrfToken,
                    "Content-Type": "application/json" // Set the content type to JSON
                },
                data: JSON.stringify({
                    content: content,
                    name: names,
                    jsonDesign: jsonDesign
                }),
                success: function(response) {
                    window.location.href = home+"employee/email-template/my-templates";
                },
                error: function(xhr, status, error) {
                    var errorMessage = xhr.responseText;
                    if (errorMessage) {
                        window.location.href = home+"employee/email-template/create?error=unique";
                    }
                }
            });
        });
    });

    $('#updateButton').click(function() {
        var names = $("#name").val();
        console.log(names);
        var id = emailTemplate.templateId;
        if(!names.trim()) {
            window.location.href = home+"employee/email-template/update/"+id+"?error=name";
            return;
        }
        unlayer.exportHtml(function(data) {
            var content = data.html; // final html
            var jsonDesign = JSON.stringify(data.design);
            var dataN = JSON.parse(jsonDesign);
            if (dataN.body.rows[0].columns[0].contents.length === 0)  {
                window.location.href = home+"employee/email-template/update/"+id+"?error=content";
                return;
            }
            $.ajax({
                type: "POST",
                url: home+"employee/email-template/update",
                processData: false,
                contentType: false,
                headers: {
                    [csrfHeaderName]: csrfToken,
                    "Content-Type": "application/json" // Set the content type to JSON
                },
                data: JSON.stringify({
                    id: id,
                    content: content,
                    name: names,
                    jsonDesign: jsonDesign
                }),
                success: function(response) {
                    window.location.href = home+"employee/email-template/my-templates";
                },
                error: function(xhr, status, error) {
                    var errorMessage = xhr.responseText;
                    if (errorMessage) {
                        window.location.href = home+"employee/email-template/update/"+id+"?error=unique";
                    }
                }
            });
        });
    });
});