$(document).ready(function () {

    var csrfToken = document.querySelector('script[data-csrf-token]').getAttribute('data-csrf-token');
    var csrfHeaderName = document.querySelector('script[data-csrf-header]').getAttribute('data-csrf-header');

    var input = document.querySelector('#email-input');
    var tagify = new Tagify(input, {
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

    $(".dropdown-item.delete").on("click",function(e) {
        e.preventDefault();
        var id = $(this).data("id");
        console.log(id);
        let formData = new FormData();
        formData.append("id",id);
        $.ajax({
            type: 'POST',
            url: home+'employee/drive/ajax-delete',
            data: formData,
            processData: false,
            contentType: false,
                    headers: {
                        [csrfHeaderName]: csrfToken
                    },
            success: function (response) {
                $("#file-"+id).remove();
            },
            error: function (xhr, status, error) {
                console.error('Error saving draft:', error);
            }
        });
    });
    $(".dropdown-item.share").on("click",function(e) {
        $("#id").val($(this).data("id"));
        $("#exampleModalCenter").modal({
             backdrop: 'static'
         });
    });
    let preLoad = `
        <div class="preloader" style="position: absolute !important;">
            <div class="loader">
                <div class="loader__figure"></div>
                <p class="loader__label">Your files are being shared, Please wait!</p>
            </div>
        </div>`;

    $("#exampleModalCenter").on("click",function(e) {
        e.stopPropagation();
    });
    $("#share").on("click",function(e) {
        e.preventDefault();
        e.stopPropagation();
        var id = $("#id").val();
        var role = $("#role").val();
        var emails = $("#emails").val();
        let formData = new FormData();
        formData.append("id",id);
        formData.append("role",role);
        formData.append("emails",emails);
        $('#exampleModalCenter .modal-content').append(preLoad);
        $.ajax({
            type: 'POST',
            url: home+'employee/drive/ajax-share',
            data: formData,
            processData: false,
            contentType: false,
                    headers: {
                        [csrfHeaderName]: csrfToken
                    },
            success: function (response) {
                $(".preloader").fadeOut();
                $('#exampleModalCenter .modal-content .preloader').remove();
                $('#exampleModalCenter').modal('hide');
                clearForm();
            },
            error: function (xhr, status, error) {
                $(".preloader").fadeOut();
                $('#exampleModalCenter .modal-content .preloader').remove();
                $("#emailErrors").text(xhr.responseText);
            }
        });

    });

    function clearForm() {
        tagify.removeAllTags();
        $("#id").val("");
        $("#role").val("");
        $("#emails").val("");
        $("#emailErrors").text("");
    }

});