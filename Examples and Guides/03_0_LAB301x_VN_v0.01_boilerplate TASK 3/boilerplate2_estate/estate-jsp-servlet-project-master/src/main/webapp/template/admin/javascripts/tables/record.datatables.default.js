(function ($) {

    'use strict';

    var datatableInit = function () {

        $('#datatable-default').DataTable({
            // tai edited here
            searching: false,
            paging: false,
            info: false,
            /*bAutoWidth: false,
            aoColumns : [
                { sWidth: '15%' },
                { sWidth: '15%' },
                { sWidth: '15%' },
                { sWidth: '15%' },
                { sWidth: '15%' },
                { sWidth: '15%' },
                { sWidth: '10%' }
            ]*/
        });

    };

    $(function () {
        datatableInit();
    });

}).apply(this, [jQuery]);