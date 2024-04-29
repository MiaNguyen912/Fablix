// -------- Get the current cart total and display it on the Pay button
document.getElementById('pay-button').textContent = "Pay $" + sessionStorage.getItem('cart_total');

let payment_form = $("#payment_form");

payment_form.submit(placeOrder);



/** Sends post request with the inputted credit card details,
 *
 * The transaction succeeds only if customers can provide correct payment information which matches a record in the credit cards table (not those in the customers table).
 * If succeeds, record in the sales table and show confirmation page
 * If failed, show error message
 *  */
function placeOrder(placeOrderEvent) {
    /**
     * When users click the Pay button, the browser will not direct
     * users to the url defined in HTML form. Instead, it will call this
     * event handler when the event is triggered.
     */
    placeOrderEvent.preventDefault();


    // Get the input values from the form fields
    let cardNumber = document.getElementById('card-number').value.trim();
    let firstName = document.getElementById('first-name').value.trim();
    let lastName = document.getElementById('last-name').value.trim();
    let expirationDate = new Date(document.getElementById('expiration-date').value.trim()); // format: Fri Apr 05 2024 17:00:00 GMT-0700

    let formattedExpirationDate = expirationDate.toLocaleString("en-US", {
        timeZone: "UTC", // Set the target timezone here
        month: "2-digit",
        day: "2-digit",
        year: "numeric"
    }); // format: 04/06/2024

    let [month, day, year] = formattedExpirationDate.split('/');
    formattedExpirationDate = `${year}-${month.padStart(2, '0')}-${day.padStart(2, '0')}`;


    // if (cardNumber === '' || firstName === '' || lastName === '' || expirationDate === '') {
    //     alert('Please fill in all required fields.');
    //     return;
    // }

    // let cartData = JSON.parse(sessionStorage.getItem('cart'));
    let cartData = sessionStorage.getItem('cart');


    // Now HTTP Post request to the payment servlet to check with database if credit card is valid
    let postData = {
        card_number: cardNumber,
        first_name: firstName,
        last_name: lastName,
        expiration_date: formattedExpirationDate,
        cart_data: cartData // format: {"tt0395642":2,"tt0424773":1}
    };

    jQuery.ajax({
        dataType: "json", // Setting return data type
        method: "POST", // Setting request method
        url: "api/payment",
        data: postData,
        success: function(response) {
            if (response["status"] === "success") {
                showConfirmation();
            } else {
                showInvalid(response);
            }
        }
    });

}

function showConfirmation(){
    console.log("success")
    window.location.replace("confirmation.html");

}
function showInvalid(response){
    console.log("fail")
    $("#error_message").text(response["message"]);
}
