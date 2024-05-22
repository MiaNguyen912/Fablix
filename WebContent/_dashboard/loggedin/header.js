let isLoggedIn = sessionStorage.getItem('isLoggedIn');
window.onload = function() {
    $("#year-search").val(''); // clear value of the year input field
    console.log("clear year");
};


console.log("User have logged in: " + isLoggedIn);
if (isLoggedIn){
    let login_btn = document.getElementById("login-btn");
    login_btn.style.display = "none";

} else {
    let logout_btn = document.getElementById("logout-btn");
    let browse_ddl = document.getElementById("browse-dropdown-list");
    let search_form = document.getElementById("seach-form");

    logout_btn.style.display = "none";
    browse_ddl.style.display = "none";
    search_form.style.display = "none";
}

document.addEventListener('DOMContentLoaded', function() {
    const menuButton = document.getElementById('menu-button');
    const dropdownMenu = document.getElementById('drop-down-menu');
    menuButton.addEventListener('click', function() {
        dropdownMenu.classList.toggle('active');
    });

});


function handleLogOut(){

}

// --------- populate search option dropdown list---------------

//------- search by title -----------
let title_letter_list = jQuery("#title-letter-list");
let links = ['A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '*'];
let baseUrl_titleSearch = 'list.html?type=title&start=';

let html = "";
for (let i = 0; i < links.length; i++) {
    if (i%6 === 0){
        html += "</tr>";
        html += "<tr class=\"divide-x divide-gray-200\">"
    }
    if (i === links.length -1){
        html += "<tr class=\"divide-x divide-gray-200\">\n" +
            "       <td colSpan=\"6\" class=\"menu-table-cell text-sm px-3\"><a href='" + baseUrl_titleSearch + links[i] + "' class='hover:no-underline hover:text-gray-900 text-gray-700'>* (Non-alphanumeric)</a></td>\n" +
            "    </tr>"
        break;
    }
    html += "<td class='menu-table-cell'><a href='" + baseUrl_titleSearch + links[i] + "' class='hover:no-underline hover:text-gray-900 text-gray-700'>" + links[i] + "</a></td>"
}
title_letter_list.append(html);

//------- search by genre -----------
function handleGenreListDisplay(resultData) {
    let genre_list = jQuery("#genre-search-list");
    let baseUrl_genreSearch = 'list.html?type=genre&name=';
    let baseUrlFromAuthenticatedPages_genreSearch = 'list.html?type=genre&name=';

    let rowHTML = "";
    for (let i = 0; i < resultData.length; i++) {
        let genre = resultData[i]["name"];
        rowHTML += "<li><a class='bg-gray-200 hover:bg-orange-300 hover:font-semibold hover:no-underline hover:text-gray-900 w-64 py-2 px-4 block whitespace-no-wrap' href='" + baseUrl_genreSearch + genre + "'>" + genre + "</a></li>";
    }
    genre_list.append(rowHTML);
}

// Makes the HTTP GET request and registers on success callback function handleStarResult
jQuery.ajax({
    dataType: "json", // Setting return data type
    method: "GET", // Setting request method
    url: "../../authenticated/api/all-genres", // Setting request url, which is mapped by StarsServlet in Stars.java
    success: (resultData) => handleGenreListDisplay(resultData) // Setting callback function to handle data returned successfully by the StarsServlet
});


// -----------------------------------
function handleListBtnClick(){
    let grid = document.getElementById("movie_grid");
    let list = document.getElementById("detail_table");

    if (!grid.classList.contains("hide")) {
        grid.classList.add("hide");
    }
    if (list.classList.contains("hide")) {
        list.classList.remove("hide");
    }

}

function handleGridBtnClick(){
    let grid = document.getElementById("movie_grid");
    let list = document.getElementById("detail_table");
    if (grid.classList.contains("hide")) {
        grid.classList.remove("hide");
    }
    if (!list.classList.contains("hide")) {
        list.classList.add("hide");
    }
}

function handleGoBackBtnClick(){
    let search_type = sessionStorage.getItem('search_type');
    let genre = sessionStorage.getItem('genre');
    let titleFirstLetter = sessionStorage.getItem('titleFirstLetter');
    let searchTitle = sessionStorage.getItem('searchTitle');
    let searchYear = sessionStorage.getItem('searchYear');
    let searchDirector = sessionStorage.getItem('searchDirector');
    let searchStar = sessionStorage.getItem('searchStar');


    let redirectedToSingleMovieByAutocompleteSuggestion = sessionStorage.getItem("redirectedToSingleMovieByAutocompleteSuggestion")
    if (redirectedToSingleMovieByAutocompleteSuggestion === "true") {
        // go back to home page
        window.location.href = 'home.html';
        return;
    }

    if (search_type === "genre"){
        window.location.href = 'list.html?type=genre&name=' + genre + '&goback=true';
    } else if (search_type === "title"){
        window.location.href = 'list.html?type=title&start=' + titleFirstLetter + '&goback=true';
    } else {
        window.location.href = 'list.html?type=search&title=' + searchTitle + '&year=' + searchYear+ '&director=' + searchDirector + '&star=' + searchStar + '&goback=true';
    }
}



// --------------------- handle autocomplete search box -------------------------

/*
 * This statement binds the autocomplete library with the input box element and sets necessary parameters of the library.
 * The library documentation can be found here:
 *   https://github.com/devbridge/jQuery-Autocomplete
 *   https://www.devbridge.com/sourcery/components/jquery-autocomplete/
 */
$('#autocomplete').autocomplete({
    // documentation of the lookup function can be found under the "Custom lookup function" section
    lookup: function (titleQuery, doneCallback) {
        handleLookup(titleQuery, doneCallback) // call this function when need to look up a query
    },
    onSelect: function(suggestion) {
        handleSelectSuggestion(suggestion)
    },
    // set delay time
    deferRequestBy: 300,
    noCache: false,
    minChars: 3,
});

let searchForm = document.getElementById("seach-form");
searchForm.addEventListener("submit", function(event) {
    // Prevent the form from submitting normally
    event.preventDefault();

    // Get the value of the input field
    let movieTitle = $('#autocomplete').val();

    // If the value contains a movie year in parentheses, remove it
    let indexOfYearOpeningBracket = movieTitle.lastIndexOf('(');
    let indexOfYearClosingBracket = movieTitle.lastIndexOf(')');
    let consoleLogString = "doing normal search for movie with title: "

    if (indexOfYearOpeningBracket !== -1 && indexOfYearClosingBracket-indexOfYearOpeningBracket === 5){
        // Remove the movie year if it exists
        let year = movieTitle.substring(indexOfYearOpeningBracket + 1, indexOfYearClosingBracket)
        $("#year-search").val(year)
        movieTitle = movieTitle.substring(0, indexOfYearOpeningBracket).trim();
        consoleLogString += movieTitle + " --- year: " + year
    } else {
        consoleLogString += movieTitle
    }
    // Set the input field value to the modified movie title
    $('#autocomplete').val(movieTitle);

    // Submit the form
    console.log(consoleLogString);
    this.submit();
})



/*
 * This function is called by the library when it needs to lookup a query.
 * The parameter titleQuery is the search query string.
 * The doneCallback is a callback function provided by the library, after you get the suggestion list from AJAX, you need to call this function to let the library know.
 */
function handleLookup(titleQuery, doneCallback) {
    console.log("autocomplete initiated")

    // check past query results first
    autocompleteQueries = JSON.parse(sessionStorage.getItem("autocompleteQueries"))
    if (autocompleteQueries == null){
        let autocompleteQueries = {} // create an empty dict
        sessionStorage.setItem("autocompleteQueries", JSON.stringify(autocompleteQueries))
    } else {
        if (titleQuery in autocompleteQueries){
            console.log("Autocomplete search uses cached results")
            let resultData = autocompleteQueries[titleQuery]
            handleLookupAjaxSuccess(resultData, titleQuery, doneCallback)
            return
        }
    }
    // sending the HTTP GET request
    console.log("Autocomplete search is sending ajax request to backend server")
    jQuery.ajax({
        "method": "GET",
        "url": "../../authenticated/api/autocomplete?titleQuery=" + titleQuery,
        "success": function(data) {
            handleLookupAjaxSuccess(data, titleQuery, doneCallback) // pass the data, query, and doneCallback function into the success handler
        },
        "error": function(errorData) {
            console.log("lookup ajax error")
            console.log(errorData)
        }
    })
}


/*
 * This function is used to handle the ajax success callback function.
 * It is called by our own code upon the success of the AJAX request
 * data is the JSON data string you get from your Java Servlet
 */
function handleLookupAjaxSuccess(data, titleQuery, doneCallback) {
    // parse the string into JSON
    var jsonData = JSON.parse(data);
    console.log(jsonData)

    // to cache the result into a global variable
    autocompleteQueries =  JSON.parse(sessionStorage.getItem("autocompleteQueries"))
    autocompleteQueries[titleQuery] = data
    sessionStorage.setItem("autocompleteQueries",  JSON.stringify(autocompleteQueries))

    // call the callback function provided by the autocomplete library
    // add "{suggestions: jsonData}" to satisfy the library response format according to the "Response Format" section in documentation
    doneCallback( {suggestions: jsonData} );


}


/*
 * This function is the select suggestion handler function.
 * When a suggestion is selected, this function is called by the library.
 */
function handleSelectSuggestion(suggestion) {
    // jump to the specific result page based on the selected suggestion
    console.log("You select " + suggestion["value"] + " with ID " + suggestion["data"]["id"])
    let movie_id = suggestion["data"]["id"]
    sessionStorage.setItem("redirectedToSingleMovieByAutocompleteSuggestion", "true")
    window.location.href = "single-movie.html?id=" + movie_id;
}


