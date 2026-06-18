(function ($) {

    'use strict';

    var datatableInit = function () {

        $('#datatable-default').DataTable({
            // tai edited here
            searching: false,
            paging: false,
            info: false
        });

    };

    $(function () {
        datatableInit();
    });

}).apply(this, [jQuery]);