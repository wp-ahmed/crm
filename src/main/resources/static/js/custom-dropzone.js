$(document).ready(function () {
    var csrfToken = document.querySelector('script[data-csrf-token]').getAttribute('data-csrf-token');
    var csrfHeaderName = document.querySelector('script[data-csrf-header]').getAttribute('data-csrf-header');

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
        url: home+'employee/gmail/upload',
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
                let tempFiles = [];
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
                    tempFiles.push(tempAttachment);
                    dz.emit("addedfile", mockFile);
                    dz.emit("thumbnail", mockFile, ImgUrl); // Assuming file.data is a dataURL of the file
                    dz.emit("complete", mockFile);
                    dz.files.push(mockFile); // Add the file to the files array
                });
                document.getElementById('allFiles').value = JSON.stringify(tempFiles);
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
            debouncedSaveDraft();
        });

        myDropzone.on('removedfile', function (file) {
            // Remove the file ID from the attachmentIds array when a file is removed
            debouncedSaveDraft();
        });

//    // Attach event listener to the email form
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



    function saveDraft() {
        let allFiles = myDropzone.getFilesWithStatus(Dropzone.SUCCESS);
        let allAttachment = [];
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
        let id = $('#id').val();
        let recipient = $('#recipient').val();
        let subject = $('#subject').val();
        let body = $('#body').val();
        let draftId = $('#draftId').val();
        let formData = new FormData();

        formData.append('id', id);
        formData.append('recipient', recipient);
        formData.append('subject', subject);
        formData.append('body', body);
        formData.append('draftId', draftId);
        formData.append('files',files);


        $.ajax({
            type: 'POST',
            url: home+'employee/gmail/draft/ajax',
            data: formData,
            processData: false,
            contentType: false,
                    headers: {
                        [csrfHeaderName]: csrfToken
                    },
            success: function (response) {
                draftId = response;
                //To use this draftId to remove the draft itself after sending it.
                document.getElementById('draftId').value = draftId;

                //to use them when sending the mail along side with those attachments
                document.getElementById('allFiles').value = files;
            },
            error: function (xhr, status, error) {
                console.error('Error saving draft:', error);
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

    const debouncedSaveDraft = debounce(saveDraft, 1000);

    $('#recipient, #subject').on('change keyup', function () {
        debouncedSaveDraft();
    });
    var iframeBody = $('.wysihtml5-sandbox').contents().find('body');
    iframeBody.on('blur', function() {
      debouncedSaveDraft();
    });
});