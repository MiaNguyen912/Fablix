let login_form = $("#login_form");

/**
 * Handle the data returned by LoginServlet
 * @param resultDataString jsonObject
 */
function handleLoginResult(resultDataString) {
    let resultDataJson = JSON.parse(resultDataString);

    console.log("handle login response");
    console.log(resultDataJson); // format: {status: 'fail', message: "user s doesn't exist"}

    // If login succeeds, redirect the user to index.html
    if (resultDataJson["status"] === "success") {
        sessionStorage.setItem('isLoggedIn', 'true'); // store isLoggedIn information onto session storage
        window.location.replace("authenticated/home.html");
    } else {
        // If login fails, display error message
        sessionStorage.setItem('isLoggedIn', 'false');
        console.log("Error message: " + resultDataJson["message"]);
        $("#login_error_message").text(resultDataJson["message"]);
    }
}


/**
 * Submit the form content with POST method
 * @param formSubmitEvent
 */
function submitLoginForm(formSubmitEvent) {
    console.log("submit login form");
    /**
     * When users click the submit button, the browser will not direct
     * users to the url defined in HTML form. Instead, it will call this
     * event handler when the event is triggered.
     */
    formSubmitEvent.preventDefault();

    // Makes the asynchronous POST request and registers on success callback function handleLoginResult
    $.ajax(
        "api/login", {
            method: "POST",
            data: login_form.serialize(), // Serialize the login form to the data sent by POST request
            success: handleLoginResult // if success, jQuery will call handleLoginResult and pass the response data as an argument.
        }
    );
}

// Bind the submit action of the form to a handler function
login_form.submit(submitLoginForm);




