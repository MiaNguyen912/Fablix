
let add_form = $("#add_star_form");

add_form.submit(addData);


function addData(addDataEvent) {
    console.log("adding star")
    addDataEvent.preventDefault();

    // Get the input values from the form fields
    let starName = document.getElementById('starName').value.trim();
    let birthYear = document.getElementById('birthYear').value.trim();
    console.log("adding " + starName + " " + birthYear);

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
                showConfirmation(response);
            } else {
                showInvalid(response);
            }
        }
    });

}

function showConfirmation(response){
    console.log("Star has been successfully added with id : " + response["new_star_id"])
    alert("Star has been successfully added with id : " + response["new_star_id"]);
}
function showInvalid(response){
    console.log(response["errorMessage"])
}
