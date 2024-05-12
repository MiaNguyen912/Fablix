
jQuery.ajax({
    dataType: "json", // Setting return data type
    method: "GET",
    url: "api/metadata",
    success: (resultData) => handleMetadataDisplay(resultData)
});


function handleMetadataDisplay(resultData) {
    console.log("Display database metadata");

    let metadata_div = document.getElementById("metadata_grid");



    for (let table_name in resultData){

        let rowHTML = "<li class=\"col-span-1 divide-y divide-gray-200 rounded-lg bg-white shadow\">" +
            "           <div class=\"w-full max-w-md p-4 bg-white rounded-lg sm:p-8 dark:bg-gray-800\">\n" +
            "                <div class=\"flex items-center justify-between mb-4\">\n" +
            "                     <h5 class=\"text-xl font-bold leading-none text-gray-900\">" + table_name + "</h5>\n" +
            "                     <a href=\"#\" class=\"text-sm font-medium text-orange-600 hover:font-semibold hover:text-lg hover:no-underline hover:text-orange-400 dark:text-orange-500\">\n" +
            "                         View data\n" +
            "                     </a>\n" +
            "                </div>\n" +

            "                <div class=\"flow-root\">\n" +
            "                    <ul role=\"list\" class=\"divide-y divide-gray-200 dark:divide-gray-700\">\n";


        let table_metadata = resultData[table_name];
        for (let field in table_metadata) {
            let type = table_metadata[field];
            rowHTML += "               <li class=\"py-3 sm:py-4\">\n" +
                "                             <div class=\"flex items-center\">\n" +
                "                                <div class=\"flex-1 min-w-0 ms-4\">\n" +
                "                                   <p class=\"text-sm font-medium text-gray-500 truncate\">" + field + "</p>\n" +
                "                                </div>\n" +
                "                                <div class=\"inline-flex items-center text-base font-semibold text-gray-500\">\n" + type + "</div>\n" +
                "                             </div>\n" +
                "                       </li>\n" ;
        }
        rowHTML += "             </ul>\n" +
            "                </div>\n" +
            "           </div>\n" +
            "       </li>\n";

        metadata_div.innerHTML += rowHTML;
    }

}


