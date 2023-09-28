var room = 1;

function education_fields() {

    room++;
    var objTo = document.getElementById('education_fields')
    var divtest = document.createElement("div");
    divtest.setAttribute("class", "form-group removeclass" + room);
    var rdiv = 'removeclass' + room;
    divtest.innerHTML = `
                                <div class="row">
                                    <div class="col-sm-3 nopadding">
                                        <div class="form-group">
                                            <input type="text" class="form-control" id="tagName" name="tagName[]"  placeholder="tag name">
                                        </div>
                                    </div>
                                    <div class="col-sm-3 nopadding">
                                        <div class="form-group">
                                            <div class="input-group">
                                                <input type="text" class="form-control" id="tagValue" name="tagValue[]" placeholder="tag value">
                                                <div class="input-group-append">
                                                    <button class="btn btn-danger" type="button" onclick="remove_education_fields(${room});"> <i class="fa fa-minus"></i> </button>                                                </div>
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                </div>
`;

    objTo.appendChild(divtest)
}

function remove_education_fields(rid) {
    $('.removeclass' + rid).remove();
}