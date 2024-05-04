
let add_form = $("#add_star_form");

add_form.submit(addData);


function addData(addDataEvent) {
    addDataEvent.preventDefault();

    // Get the input values from the form fields
    let starName = document.getElementById('starName').value.trim();
    let birthYear = document.getElementById('birthYear').value.trim();

    let postData = {
        star_name: starName,
        birth_year: birthYear,
    };

    // make HTTP Post request
    jQuery.ajax({
        dataType: "json", // Setting return data type
        method: "POST", // Setting request method
        url: "api/add-star",
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
    console.log("Star has been successfully added")
    alert("Star has been successfully added");
}
function showInvalid(response){
    console.log("Cannot add star: star already exist")
    $("#error_message").text(response["message"]);
}
