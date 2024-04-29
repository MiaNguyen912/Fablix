
// Makes the HTTP GET request and registers on success callback function handleStarResult
jQuery.ajax({
    dataType: "json", // Setting return data type
    method: "GET", // Setting request method
    url: "api/confirmation",
    success: (resultData) => handleConfirmation(resultData)
});


function handleConfirmation(resultData) {
    //Show a confirmation page that contains order details including sale ID, movies purchased and the quantities, and total price

    // Populate the movie table
    let confirmation_panel= jQuery("#confirmation_panel");

    for (let i = 0; i < resultData.length; i++) {
        let sale_id = resultData[i]["sale_id"];
        let movie_title = resultData[i]["movie_title"];
        let quantity = resultData[i]["quantity"];

        // Concatenate the html tags with resultData jsonObject
        let rowHTML = "";
        rowHTML += "<tr>";
        rowHTML += "<td>"+ sale_id + "</td>";
        rowHTML += "<td>" + movie_title + "</td>";
        rowHTML += "<td>" + quantity + "</td>";
        rowHTML += "</tr>";
        confirmation_panel.append(rowHTML);
    }
    let total = jQuery("#total");
    let cart_total = sessionStorage.getItem("cart_total");
    total.append("Total: $" + cart_total);

    // clear shopping cart and total in sessionStorage
    sessionStorage.setItem("cart", null);
    sessionStorage.setItem("cart_total", null);

}


