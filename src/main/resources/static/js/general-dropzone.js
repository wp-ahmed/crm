$(document).ready(function () {
    var csrfToken = document.querySelector('script[data-csrf-token]').getAttribute('data-csrf-token');
    var csrfHeaderName = document.querySelector('script[data-csrf-header]').getAttribute('data-csrf-header');
    let allAttachment = [];
//    let draftId = false;
    function readFileAsDataURL(file) {
        return new Promise((resolve, reject) => {
            const reader = new FileReader();

            reader.onload = function(event) {
                resolve(event.target.result);
            };

            reader.onerror = function(error) {
                reject(error);
            };

            reader.readAsDataURL(file);
        });
    }
    Dropzone.autoDiscover = false;
    const myDropzone = new Dropzone('#my-dropzone', {
        url: home+'employee/lead/upload',
        maxFilesize: 10, // MB
        parallelUploads: 2,
        addRemoveLinks: true,
        acceptedFiles: 'image/*,application/pdf,.doc,.docx,.xls,.xlsx,.ppt,.pptx,.txt,.csv,.zip,.rar',
        withCredentials: true,
        headers: {
            [csrfHeaderName]: csrfToken
        },
        init: function() {
            let dz = this;
            if(existingFiles != null){

                existingFiles.forEach(function(file) {
                    let base64 = "data:image/jpeg;base64,";
                    let ImgUrl = base64.concat(file.data);
                    let mockFile = { name: file.name, size: file.size, type: file.mimeType, data: file.data, status: Dropzone.SUCCESS };
                    let tempAttachment = {
                                        name: file.name,
                                        data: file.data,
                                        mimeType: file.mimeType,
                                        size: file.size,
                                    };
                    allAttachment.push(tempAttachment);
                    dz.emit("addedfile", mockFile);
                    dz.emit("thumbnail", mockFile, ImgUrl); // Assuming file.data is a dataURL of the file
                    dz.emit("complete", mockFile);
                    dz.files.push(mockFile); // Add the file to the files array
                });
                document.getElementById('allFiles').value = JSON.stringify(allAttachment);
            }
        }
    });
    myDropzone.on('success', function (file, response) {
        // Store the uploaded file ID in the attachmentIds array
            readFileAsDataURL(file).then(function(dataURL) {
                // Add the data URL to the file object
                let encodedData = dataURL.split(',')[1];
                file.data = encodedData;
            });
        debouncedSaveAttachments();
    });

    myDropzone.on('removedfile', function (file) {
        // Remove the file ID from the attachmentIds array when a file is removed
        var fileType = file.name;

        // Loop through the attachment array and remove the matching file
        for (var i = 0; i < allAttachment.length; i++) {
            var attachment = allAttachment[i];
            if (attachment.name == fileType) {
                console.log("sa");
                // Remove the file from the attachments array
                allAttachment.splice(i, 1);
                break; // Exit the loop since the file is found and removed
            }
        }
        debouncedSaveAttachments();
    });

    const emailForm = document.getElementById('email-form');
//    let draftId = false;

    emailForm.addEventListener('submit', function (event) {
        event.preventDefault();

        if (myDropzone.getQueuedFiles().length > 0) {
            // There are files still in the queue, process them first and then submit the form
            myDropzone.processQueue();
            myDropzone.on('queuecomplete', function () {
                emailForm.submit();
            });
        } else {
            emailForm.submit();
        }
    });

function saveAttachments() {
        let allFiles = myDropzone.getFilesWithStatus(Dropzone.SUCCESS);
        allAttachment = [];
        console.log(allFiles);
        for(var i=0; i<allFiles.length; ++i){
            let newAttachment = {
                                name: allFiles[i].name,
                                data: allFiles[i].data,
                                mimeType: allFiles[i].type,
                                size: allFiles[i].size,
                            };
            allAttachment.push(newAttachment);

        }
        let files = JSON.stringify(allAttachment);
        let formData = new FormData();
        formData.append('files',files);


        $.ajax({
            type: 'POST',
            url: home+'employee/lead/save-attachment/ajax',
            data: formData,
            processData: false,
            contentType: false,
                    headers: {
                        [csrfHeaderName]: csrfToken
                    },
            success: function (response) {
                document.getElementById('allFiles').value = files;
            },
            error: function (xhr, status, error) {
                console.error('Error saving attachments:', error);
            }
        });
    }

    function debounce(func, wait) {
        let timeout;
        return function (...args) {
            clearTimeout(timeout);
            timeout = setTimeout(() => func.apply(this, args), wait);
        };
    }

    const debouncedSaveAttachments = debounce(saveAttachments, 1000);

    var selectElement = $("<select class='form-control' name='folderId' id='selectElementId'>");
    var optionElement = $('<option>').text("Pick Google Drive Folder").attr('value', '');
    selectElement.append(optionElement);

    $.each(folders, function(index, item) {
        if(item.id == driveFolderId) {
            optionElement = $('<option>').text(item.name).attr('value', item.id).attr('selected','selected');
        } else {
            optionElement = $('<option>').text(item.name).attr('value', item.id);
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
                <small>To create a new folder, use this <a href='#' data-toggle="modal" data-target="#exampleModalCenter">link</a></small>
            </div>
        </div>`;
    if ($('.trigger-emails').is(':checked')) {
        var id = $('.trigger-emails').attr('id');
        $('.trigger-emails').parent().append(html);
    } else {
        $('.trigger-emails').parent().find('.email-template').fadeOut(function() {
            $('.trigger-emails').remove();
        });
    }
    $('.trigger-emails').change(function() {
        if ($(this).is(':checked')) {
            var id = $(this).attr('id');
            $(this).parent().append(html);
        } else {
            $(this).parent().find('.email-template').fadeOut(function() {
                $(this).remove();
            });
        }
    });


    $("#create").on("click",function(e) {
        e.preventDefault();
        e.stopPropagation();
        var folderName = $("#folderName").val();
        let formData = new FormData();
        formData.append("folderName",folderName);
        console.log("sa");
        $.ajax({
            type: 'POST',
            url: home+'employee/lead/drive/ajax-create',
            data: formData,
            processData: false,
            contentType: false,
                    headers: {
                        [csrfHeaderName]: csrfToken
                    },
            success: function (response) {
                var folderId = response.folderId;
                var folderName = response.folderName;
                var optionElement = $('<option>').text(folderName).attr('value', folderId);
                $('#selectElementId').append(optionElement);
                $('#exampleModalCenter').modal('hide');

            },
            error: function (xhr, status, error) {
                console.log(error);
            }
        });

    });
});