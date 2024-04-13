/**
 * This example is following frontend and backend separation.
 *
 * Before this .js is loaded, the html skeleton is created.
 *
 * This .js performs three steps:
 *      1. Get parameter from request URL so it know which id to look for
 *      2. Use jQuery to talk to backend API to get the json data.
 *      3. Populate the data to correct html elements.
 */


/**
 * Retrieve parameter from request URL, matching by parameter name
 * @param target String
 * @returns {*}
 *     title;
 *     year;
 *     director;
 *     all of the genres;
 *     all of the stars (hyperlinked);
 *     rating.
 */
function getParameterByName(target) {
    // Get request URL
    let url = window.location.href;
    // Encode target parameter name to url encoding
    target = target.replace(/[\[\]]/g, "\\$&");

    // Ues regular expression to find matched parameter value
    let regex = new RegExp("[?&]" + target + "(=([^&#]*)|&|#|$)"),
        results = regex.exec(url);
    if (!results) return null;
    if (!results[2]) return '';

    // Return the decoded parameter value
    return decodeURIComponent(results[2].replace(/\+/g, " "));
}

/**
 * Handles the data returned by the API, read the jsonObject and populate data into html elements
 * @param resultData jsonObject
 */

function handleResult(resultData) {

    console.log("handleResult: populating star info from resultData");

    // populate the star info h3
    // find the empty h3 body by id "movie_info"
    let movieInfoElement = jQuery("#main_info");
    let movie_director = resultData[0]["movie_director"];
    let movie_rating = resultData[0]["movie_rating"];
    let stars = resultData[0]["stars"];
    let genres = resultData[0]["genres"];

    let genres_id = [];
    let genres_name = [];
    for (const key in genres){
        genres_id.push(key);
        genres_name.push(genres[key]);
    }

    let stars_id = [];
    let stars_name = [];
    for (const key in stars){
        stars_id.push(key);
        stars_name.push(stars[key]);
    }
    // append two html <p> created to the h3 body, which will refresh the page
    // For title, year, director, and rating
    movieInfoElement.append("<p>" + resultData[0]["movie_title"] + " (" + resultData[0]["movie_year"] + ")</p>");

    console.log("handleResult: populating movie info table from resultData");

    // Populate the star table
    // Find the empty table body by id "movie_info_table_body"
    let movieTableBodyElement = jQuery("#movie_info_table_body");

    // Concatenate the html tags with resultData jsonObject to create table rows
    let rowHTML = "";
    rowHTML += "<tr>";
    rowHTML += "<td>" + movie_director + "</td>";
    // genres
    rowHTML += "<td>";
    for (let i = 0; i<genres_id.length; i++){
        if (i < genres_id.length -1 )
            rowHTML += genres_name[i] + ", ";
        else
            rowHTML += genres_name[i];
    }
    rowHTML += "</td>";
    // stars
    rowHTML += "<td>";
    for (let i = 0; i<stars_id.length; i++){
        if (i < stars_id.length -1 )
            rowHTML += "<a href='/cs122b_project1_api_example_war/single-star.html?id=" + stars_id[i] + "'> " + stars_name[i] + "</a>, ";
        else
            rowHTML += "<a href='/cs122b_project1_api_example_war/single-star.html?id=" + stars_id[i] + "'> " + stars_name[i] + "</a>";
    }
    rowHTML += "</td>";
    rowHTML += "<td>" + movie_rating + "</td>";
    rowHTML += "</tr>";

    // Append the row created to the table body, which will refresh the page
    movieTableBodyElement.append(rowHTML);
}

/**
 * Once this .js is loaded, following scripts will be executed by the browser\
 */

// Get id from URL
let movieId = getParameterByName('id');

// Makes the HTTP GET request and registers on success callback function handleResult
jQuery.ajax({
    dataType: "json",  // Setting return data type
    method: "GET",// Setting request method
    url: "api/single-movie?id=" + movieId, // Setting request url, which is mapped by StarsServlet in Stars.java
    success: (resultData) => handleResult(resultData) // Setting callback function to handle data returned successfully by the SingleStarServlet
});