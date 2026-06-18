$(document).ready(function () {

    PNotify.prototype.options.delay = 5000;

    String.prototype.format = function () {
        var formatted = this;
        for (var arg in arguments) {
            formatted = formatted.replace("{" + arg + "}", arguments[arg]);
        }
        return formatted;
    };

    jQuery.fn.serializeObject = function () {
        var arrayData, objectData;
        arrayData = this.serializeArray();
        objectData = {};

        $.each(arrayData, function () {
            var value;

            if (this.value != null) {
                value = this.value;
            } else {
                value = '';
            }

            if (objectData[this.name] != null) {
                if (!objectData[this.name].push) {
                    objectData[this.name] = [objectData[this.name]];
                }

                objectData[this.name].push(value);
            } else {
                objectData[this.name] = value;
            }
        });

        return objectData;
    };

    $(document).on('click', '.modal-dismiss', function (e) {
        e.preventDefault();
        $.magnificPopup.close();
    });

    $("input[name='multiselect']").each(function () {
        $(this).removeAttr("name");
    });

    bindEventCheckAllCheckbox();
    enableOrDisableDeleteAll();
    autoCheckCheckboxAll();
})

function getBase64(file) {
    return new Promise((resolve, reject) => {
        const reader = new FileReader();
        reader.readAsDataURL(file);
        reader.onload = () => resolve(reader.result);
        reader.onerror = error => reject(error);
        return Promise.resolve(reader.result)
    });
}


function bindEventCheckAllCheckbox() {
    $('#chkCheckAll').click(function () {
        $(this).closest("table").find("input[type=checkbox]").prop("checked", this.checked);
        $('#btnDeleteAll').prop('disabled', !this.checked);
    });
}

function enableOrDisableDeleteAll() {
    $('.checkbox-item').click(function () {
        if ($('.checkbox-item:checked').length == 0) {
            $('#btnDeleteAll').prop('disabled', true);
        } else {
            $('#btnDeleteAll').prop('disabled', false);
        }
    });
}

function autoCheckCheckboxAll() {
    $('.checkbox-item').change(function () {
        if ($('.checkbox-item').length == $('.checkbox-item:checked').length) {
            $('#chkCheckAll').prop("checked", true);
        } else {
            $('#chkCheckAll').prop("checked", false);
        }
    });
}