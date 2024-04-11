
/**
 * Handles the data returned by the API, read the jsonObject and populate data into html elements
 * @param resultData jsonObject
 */
function handleStarResult(resultData) {
    console.log("handleStarResult: populating movie table from resultData");

    // Populate the movie table
    let movieTableBodyElement = jQuery("#movie_table_body");

    // Iterate through resultData, no more than 10 entries
    for (let i = 0; i < Math.min(20, resultData.length); i++) {
        let movie_id = resultData[i]["movie_id"];
        let movie_title = resultData[i]["movie_title"];
        let movie_director = resultData[i]["movie_director"];
        let movie_rating = resultData[i]["movie_rating"];
        let stars = resultData[i]["stars"];
        let genres = resultData[i]["genres"];

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

        // Concatenate the html tags with resultData jsonObject
        let rowHTML = "";
        rowHTML += "<tr>";
        rowHTML += "<td><a href='single-movie.html?id='" + movie_id  + "'>" + movie_title + "</a></td>";
        rowHTML += "<td>" + movie_title + "</td>";
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
                rowHTML += "<a href='/single-star.html?id=" + stars_id[i] + "'> " + stars_name[i] + "</a>";
        }
        rowHTML += "</td>";

        rowHTML += "<td>" + movie_rating + "</td>";



        // Append the row created to the table body, which will refresh the page
        movieTableBodyElement.append(rowHTML);
    }
}


/**
 * Once this .js is loaded, following scripts will be executed by the browser
 */

// Makes the HTTP GET request and registers on success callback function handleStarResult
jQuery.ajax({
    dataType: "json", // Setting return data type
    method: "GET", // Setting request method
    url: "api/20movies", // Setting request url, which is mapped by StarsServlet in Stars.java
    success: (resultData) => handleStarResult(resultData) // Setting callback function to handle data returned successfully by the StarsServlet
});