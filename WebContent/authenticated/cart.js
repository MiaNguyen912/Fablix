/** Handles data returned by API, read the jsonObject and populate data into html elements
 * * @param resultData jsonObject
 *  */
function showCartData(resultData){
    console.log("showCartData: populating cart table from sessionStorage");

    // Populate the cart table
    let cartTableBodyElement = jQuery("#cart_table_body")

    let cartData = JSON.parse(sessionStorage.getItem('cart')) || {};

    // Iterate through all the movies in the cart,
    for (let i = 0; i < resultData.length; i++) {
        // Concatenate the html tags with resultData jsonObject
        let movie_id = resultData[i]["movie_id"];
        let movie_title = resultData[i]["movie_title"];
        let price = resultData[i]["movie_price"];

        let amount = cartData[movie_id];

        let rowHTML = "";
        rowHTML += "<tr>";

        rowHTML += "<td>" + movie_title + "</td>";

        rowHTML += "<td>";
        rowHTML += '<button onclick="decreaseAmount(this)" data-movie-id="' + movie_id + '">-</button>';
        rowHTML += "<span id='amount_" + movie_id + "'>" + amount + "</span>";
        rowHTML += '<button onclick="increaseAmount(this)" data-movie-id="' + movie_id + '">+</button>';
        rowHTML += "</td>";

        rowHTML += "<td>";
        rowHTML += '<button onclick="deleteMovie(this)" data-movie-id="' + movie_id + '">Delete</button>';
        rowHTML += "</td>";

        rowHTML += "<td>$";
        rowHTML += "<span id='price_" + movie_id + "'>" + price + "</span>";
        rowHTML += "</td>";

        rowHTML += "<td>$";
        rowHTML += "<span id='total_" + movie_id + "'>" + (price*amount) + "</span>";
        rowHTML += "</td>";

        cartTableBodyElement.append(rowHTML);
    }

    // Now update cart total
    
}

function decreaseAmount(button){
    let movie_id = button.getAttribute('data-movie-id');
    let cartData = JSON.parse(sessionStorage.getItem('cart')) || {};
    let amount = cartData[movie_id] || 0;

    if (amount > 0) {
        amount--;
        cartData[movie_id] = amount;
        sessionStorage.setItem('cart', JSON.stringify(cartData));

        // Update the displayed amount
        document.getElementById('amount_' + movie_id).textContent = amount;
        // Update Total as well
        let price = document.getElementById('price_' + movie_id).textContent;
        document.getElementById('total_' + movie_id).textContent = (price * amount);
    }
}

function increaseAmount(button) {
    let movie_id = button.getAttribute('data-movie-id');
    let cartData = JSON.parse(sessionStorage.getItem('cart')) || {};
    let amount = cartData[movie_id] || 0;

    amount++;
    cartData[movie_id] = amount;
    sessionStorage.setItem('cart', JSON.stringify(cartData));

    // Update the displayed amount
    document.getElementById('amount_' + movie_id).textContent = amount;
    // Update Total as well
    let price = document.getElementById('price_' + movie_id).textContent;
    document.getElementById('total_' + movie_id).textContent = (price * amount);
}

function deleteMovie(button){

}

/**
 * Once this .js is loaded, following scripts will be executed by the browser
 */

// Get the current cart data and turn it into a list of movie IDs
let cartDataJson = JSON.parse(sessionStorage.getItem('cart'));

// Extract the movie IDs from the cartDataJson object
let movieIdsString = Object.keys(cartDataJson).join(',');

// URL to pass into the api
let url = `../api/cart?movieIds=${movieIdsString}`;

// Makes the HTTP GET request and registers on success callback function showCartData
jQuery.ajax({
    dataType: "json", // Setting return data type
    method: "GET", // Setting request method
    url: url,
    success: (resultData) => showCartData(resultData)
});