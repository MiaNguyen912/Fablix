let empty_panel = document.getElementById("empty_panel");
let cart_panel = document.getElementById("cart_panel");

// Get the current cart data and turn it into a list of movie IDs
let cartDataJson = JSON.parse(sessionStorage.getItem('cart'));

if (cartDataJson == null){
    if (empty_panel.classList.contains("hide")) {
        empty_panel.classList.remove("hide");
    }
    if (!cart_panel.classList.contains("hide")) {
        cart_panel.classList.add("hide");
    }
} else {
    if (!empty_panel.classList.contains("hide")) {
        empty_panel.classList.add("hide");
    }
    if (cart_panel.classList.contains("hide")) {
        cart_panel.classList.remove("hide");
    }
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
}

let cart_list = $("#cart-list");
let order_summary_panel = $("#order-summary-panel");
let subtotal_price = 0
let tax = 0;
let total = 0;




/** Handles data returned by API, read the jsonObject and populate data into html elements
 * * @param resultData jsonObject
 *  */
function showCartData(resultData){
    console.log("showCartData: Displaying cart with information from sessionStorage");

    let cartData = JSON.parse(sessionStorage.getItem('cart')) || {};
    let cartTotal = 0; // Initialize cart total

    // Iterate through all the movies in the cart,
    for (let i = 0; i < resultData.length; i++) {
        // Concatenate the html tags with resultData jsonObject
        let movie_id = resultData[i]["movie_id"];
        let movie_title = resultData[i]["movie_title"];
        let movie_price = resultData[i]["movie_price"];
        let quantity = cartData[movie_id];
        subtotal_price += movie_price * quantity;


        let rowHTML = "      <li id='item_" + movie_id + "' class=\"flex py-6 sm:py-10\">\n" +
            "                            <div class=\"ml-4 flex flex-1 flex-col justify-between sm:ml-6\">\n" +
            "                                <div class=\"relative pr-9 sm:grid sm:grid-cols-2 sm:gap-x-6 sm:pr-0\">\n" +
            "                                    <div>\n" +
            "                                        <div class=\"flex justify-between\">\n" +
            "                                            <h3 class=\"text-sm\">\n" +
            "                                                <a href='single-movie.html?id=" + movie_id  + "' class=\"font-medium text-gray-700 hover:text-gray-800\">" + movie_title + "</a>\n" +
            "                                            </h3>\n" +
            "                                        </div>\n" +
            "                                        <p class=\"mt-1 text-sm font-medium text-gray-900\">$" + "<span id='unitprice_" + movie_id + "'>" + movie_price + "</span></p>\n" +
            "                                        <p class=\"mt-1 text-sm font-medium text-gray-900\">Total: $" + "<span id='totalunitprice_" + movie_id + "'>" + movie_price*quantity + "</span></p>\n" +
            "                                    </div>\n" +

            "                                 <div class=\"mt-4 sm:mt-0 sm:pr-9\">\n" +
            "                                        <span class=\"isolate inline-flex rounded-md shadow-sm mt-4 border border-gray-300\">\n" +
            "                                            <button onclick=\"increaseAmount(this)\" type='button' data-movie-id='" + movie_id + "' class=\"items-center font-medium leading-5 rounded-md px-2 py-2 text-gray-700 focus:border-orange-500 focus:outline-none focus:ring-1 focus:ring-orange-500 sm:text-sm \">\n" +
            "                                                +\n" +
            "                                            </button>\n" +
            "                                            <span id='quantity_" + movie_id + "' class=\"border-gray-600 relative inline-flex items-center px-4 py-2 text-sm font-semibold text-gray-700 font-medium ring-1 ring-inset ring-gray-300 focus:outline-offset-0\">"+ quantity + "</span>\n" +
            "                                            <button onclick=\"decreaseAmount(this)\" type='button' data-movie-id='" + movie_id + "' class=\"items-center font-medium leading-5 rounded-md px-2 py-2 text-gray-700 focus:border-orange-500 focus:outline-none focus:ring-1 focus:ring-orange-500 sm:text-sm \">\n" +
            "                                                -\n" +
            "                                            </button>\n" +
            "                                        </span>\n" +
            "\n" +
            "                                        <div class=\"absolute right-0 top-0\">\n" +
            "                                            <button onclick='deleteMovie(this)' type='button' data-movie-id='" + movie_id + "' class=\"-m-2 inline-flex p-2 text-gray-400 hover:text-gray-500\">\n" +
            "                                                <span class=\"sr-only\">Remove</span>\n" +
            "                                                <svg class=\"h-5 w-5\" viewBox=\"0 0 20 20\" fill=\"currentColor\" aria-hidden=\"true\">\n" +
            "                                                    <path d=\"M6.28 5.22a.75.75 0 00-1.06 1.06L8.94 10l-3.72 3.72a.75.75 0 101.06 1.06L10 11.06l3.72 3.72a.75.75 0 101.06-1.06L11.06 10l3.72-3.72a.75.75 0 00-1.06-1.06L10 8.94 6.28 5.22z\" />\n" +
            "                                                </svg>\n" +
            "                                            </button>\n" +
            "                                        </div>\n" +

            "                                    </div>\n" +
            "                                </div>\n" +
            "                            </div>\n" +
            "                        </li>\n";

        cart_list.append(rowHTML);
    }

    // -----------------------------
    tax = parseFloat((subtotal_price * 0.1).toFixed(1)); // round to one decimal place
    total = subtotal_price + tax;

    let order_summary_HTML = "<div class=\"flex items-center justify-between\">\n" +
        "                                <dt class=\"text-sm text-gray-600\">Subtotal</dt>\n" +
        "                                <dd class=\"text-sm font-medium text-gray-900\">$" + "<span id='subtotal'>" + subtotal_price + "</span></dd>\n" +
        "                            </div>\n" +
        "                        <div class=\"flex items-center justify-between border-t border-gray-200 pt-4\">\n" +
        "                            <dt class=\"flex text-sm text-gray-600\">\n" +
        "                                <span>Tax estimate</span>\n" +
        "                                <a href=\"#\" class=\"ml-2 flex-shrink-0 text-gray-400 hover:text-gray-500\">\n" +
        "                                    <span class=\"sr-only\">Learn more about how tax is calculated</span>\n" +
        "                                    <svg class=\"h-5 w-5\" viewBox=\"0 0 20 20\" fill=\"currentColor\" aria-hidden=\"true\">\n" +
        "                                        <path fill-rule=\"evenodd\" d=\"M18 10a8 8 0 11-16 0 8 8 0 0116 0zM8.94 6.94a.75.75 0 11-1.061-1.061 3 3 0 112.871 5.026v.345a.75.75 0 01-1.5 0v-.5c0-.72.57-1.172 1.081-1.287A1.5 1.5 0 108.94 6.94zM10 15a1 1 0 100-2 1 1 0 000 2z\" clip-rule=\"evenodd\" />\n" +
        "                                    </svg>\n" +
        "                                </a>\n" +
        "                            </dt>\n" +
        "                            <dd class=\"text-sm font-medium text-gray-900\">$" + "<span id='tax'>" + tax + "</span></dd>\n" +
        "                        </div>\n" +
        "                        <div class=\"flex items-center justify-between border-t border-gray-200 pt-4\">\n" +
        "                            <dt class=\"text-base font-medium text-gray-900\">Order total</dt>\n" +
        "                            <dd class=\"text-base font-medium text-gray-900\">$" + "<span id='total'>" + total + "</span></dd>\n" +
        "                        </div>";
    order_summary_panel.append(order_summary_HTML);
}

function decreaseAmount(button){
    let movie_id = button.getAttribute('data-movie-id');
    let cartData = JSON.parse(sessionStorage.getItem('cart')) || {};
    let amount = cartData[movie_id] || 0;

    if (amount > 1) {
        amount--;
        cartData[movie_id] = amount;
        sessionStorage.setItem('cart', JSON.stringify(cartData));

        // Update the displayed amount
        document.getElementById('quantity_' + movie_id).textContent = amount;

        // Update unit total
        let unitprice = parseFloat(document.getElementById('unitprice_' + movie_id).textContent);
        document.getElementById('totalunitprice_' + movie_id).textContent = (unitprice * amount) + "";

        // Update cart total
        let old_subtotal = parseFloat(document.getElementById('subtotal').textContent);
        let new_subtotal = old_subtotal - unitprice;
        document.getElementById('subtotal').textContent = (new_subtotal).toFixed(2);
        document.getElementById('tax').textContent = (new_subtotal*0.1).toFixed(2);
        document.getElementById('total').textContent = (new_subtotal + new_subtotal*0.1 ).toFixed(2);
    } else {
        deleteMovie(button);
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
    document.getElementById('quantity_' + movie_id).textContent = amount;

    // Update unit total
    let unitprice = parseFloat(document.getElementById('totalunitprice_' + movie_id).textContent);
    document.getElementById('totalunitprice_' + movie_id).textContent = (unitprice * amount) + "";

    // Update cart total
    let old_subtotal = parseFloat(document.getElementById('subtotal').textContent);
    let new_subtotal = old_subtotal + unitprice;
    document.getElementById('subtotal').textContent = (new_subtotal).toFixed(2);
    document.getElementById('tax').textContent = (new_subtotal*0.1).toFixed(2);
    document.getElementById('total').textContent = (new_subtotal + new_subtotal*0.1 ).toFixed(2);
}

function deleteMovie(button){
    // Update sessionStorage to remove that movie_id
    // Update html to remove that row
    // Update cart total by subtracting old total_movie_id
    let movie_id = button.getAttribute('data-movie-id');
    let cartData = JSON.parse(sessionStorage.getItem('cart')) || {};

    let old_totalunitprice = document.getElementById('totalunitprice_' + movie_id).textContent;
    let old_subtotal = parseFloat(document.getElementById('subtotal').textContent);
    let new_subtotal = old_subtotal - old_totalunitprice;
    document.getElementById('subtotal').textContent = (new_subtotal).toFixed(2);
    document.getElementById('tax').textContent = (new_subtotal*0.1).toFixed(2);
    document.getElementById('total').textContent = (new_subtotal + new_subtotal*0.1 ).toFixed(2);

    // Remove the movie_id from cartData
    delete cartData[movie_id]

    // update cart in sessionStorage
    sessionStorage.setItem('cart', JSON.stringify(cartData));

    // Remove the corresponding <li>
    let item_to_remove = $("#item_" + movie_id);
    item_to_remove.remove()

}

function handleCheckout(){
    sessionStorage.setItem('cart_total', total); // update cart total
    window.location.href = 'payment.html';
}

