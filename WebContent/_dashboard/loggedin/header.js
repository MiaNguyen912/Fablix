let isLoggedIn = sessionStorage.getItem('isLoggedIn');

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
    let limit = sessionStorage.getItem('limit');
    let sort = sessionStorage.getItem('sort');
    let page = sessionStorage.getItem('page');


    if (search_type === "genre"){
        window.location.href = 'list.html?type=genre&name=' + genre + '&goback=true';
    } else if (search_type === "title"){
        window.location.href = 'list.html?type=title&start=' + titleFirstLetter + '&goback=true';
    } else {
        window.location.href = 'list.html?type=search&title=' + searchTitle + '&year=' + searchYear+ '&director=' + searchDirector + '&star=' + searchStar + '&goback=true';
    }
}