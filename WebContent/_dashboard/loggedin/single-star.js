function addToCart(button){
    let movie_id = button.getAttribute('data-movie-id');
    let movie_title = button.getAttribute('data-movie-title');
    alert(movie_title + " has been added to your cart");

    let cartData = JSON.parse(sessionStorage.getItem('cart')) || {};

    // Check if the movie_id already exists in cartData
    if (cartData[movie_id]) {
        // Increment the quantity if the movie_id is already in the cart
        cartData[movie_id]++;
    } else {
        // Initialize the quantity to 1 if the movie_id is not yet in the cart
        cartData[movie_id] = 1;
    }

    // Save updated cart data back to sessionStorage
    sessionStorage.setItem('cart', JSON.stringify(cartData));
}


/**
 * Retrieve parameter from request URL, matching by parameter name
 * @param target String
 * @returns {*}
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
function handleStarResult(resultData) {

    console.log("handleResult: populating star info from resultData");
    let starName = jQuery("#main_info");
    let starYOB = jQuery("#sub_info");

    let yob = resultData[0]["star_yob"];
    if (yob === null){
        yob = "N/A";
    }

    // append two html <p> created to the h3 body, which will refresh the page
    starName.append(resultData[0]["star_name"]);
    starYOB.append("Year of birth: " + yob);

    console.log("handleResult: populating star table from resultData");

    // Populate the star table
    let movieTableBodyElement = jQuery("#movie_table_body");
    let movies = resultData[0]["movies"];
    let movies_id = [];
    let movies_detail = [];

    for (const key in movies){
        movies_id.push(key);
        movies_detail.push(movies[key]);
    }


    for (let i = 0; i<movies_id.length; i++){
        let movie_id = movies_id[i];
        let movie_title = movies_detail[i]["title"]

        let rowHTML = "";
        rowHTML += "<tr>";
        rowHTML += "<td><a href='single-movie.html?id=" + movie_id + "'> " + movie_title + "</a></td>";
        rowHTML += "<td>" + movies_detail[i]["year"] + "</td>";
        rowHTML += "<td>" + movies_detail[i]["director"] + "</td>";
        rowHTML += "</tr>";

        // Append the row created to the table body, which will refresh the page
        movieTableBodyElement.append(rowHTML);

    }
}

/**
 * Once this .js is loaded, following scripts will be executed by the browser\
 */

// Get id from URL
let starId = getParameterByName('id');

// Makes the HTTP GET request and registers on success callback function handleResult
jQuery.ajax({
    dataType: "json",  // Setting return data type
    method: "GET",// Setting request method
    url: "../../authenticated/api/single-star?id=" + starId, // Setting request url, which is mapped by StarsServlet in Stars.java
    success: (resultData) => handleStarResult(resultData) // Setting callback function to handle data returned successfully by the SingleStarServlet
});