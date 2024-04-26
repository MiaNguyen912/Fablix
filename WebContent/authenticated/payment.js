
/** Sends post request with the inputted credit card details,
 *
 * The transaction succeeds only if customers can provide correct payment information which matches a record in the credit cards table (not those in the customers table).
 * If succeeds, record in the sales table and show confirmation page
 * If failed, show error message
 *  */
function placeOrder() {
    // Get the input values from the form fields
    let cardNumber = document.getElementById('card_number').value.trim();
    let firstName = document.getElementById('first_name').value.trim();
    let lastName = document.getElementById('last_name').value.trim();
    let expirationDate = document.getElementById('expiration_date').value.trim();

    if (cardNumber === '' || firstName === '' || lastName === '' || expirationDate === '') {
        alert('Please fill in all required fields.');
        return;
    }

    let cartData = JSON.parse(sessionStorage.getItem('cart'));
    // Now HTTP Post request to the payment servlet to check with database if credit card is valid
    let postData = {
        card_number: cardNumber,
        first_name: firstName,
        last_name: lastName,
        expiration_date: expirationDate,
        cartData: cartData
    };

    jQuery.ajax({
        dataType: "json", // Setting return data type
        method: "POST", // Setting request method
        url: "api/payment",
        data: postData,
        success: function(response) {
            if (response.success) {
                showConfirmation();
            } else {
                showInvalid();
            }
        }
    });

}

function showConfirmation(){

}
function showInvalid(){

}

/**
 * Once this .js is loaded, following scripts will be executed by the browser
 */

// Get the current cart total and display it
let cartTotal = sessionStorage.getItem('cart_total');
document.getElementById('payment_amount').textContent = cartTotal;
