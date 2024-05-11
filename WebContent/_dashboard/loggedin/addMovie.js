
let add_form = $("#add_movie_form");

add_form.submit(addData);


function addData(addDataEvent) {
    console.log("adding movie")
    addDataEvent.preventDefault();

    // Get the input values from the form fields
    let title = document.getElementById('title').value.trim();
    let director = document.getElementById('director').value.trim();
    let year = document.getElementById('year').value.trim();
    let genre = document.getElementById('genre').value.trim();
    let starName = document.getElementById('starName').value.trim();

    console.log("adding " + title + " " + director + " " + year + " " + genre + " " + starName);

    let postData = {
        title: title,
        director: director,
        year: year,
        genre: genre,
        star_name: starName
    };

    // make HTTP Post request
    jQuery.ajax({
        dataType: "json", // Setting return data type
        method: "POST", // Setting request method
        url: "api/add-movie",
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
    console.log("Movie has been successfully added with id : " + response["movie_id"])
    alert("Movie added with movieId: " + response["movie_id"] + " genreId: " + response["genre_id"] + " starId: " + response["star_id"]);
}
function showInvalid(response){
    alert("Error: Duplicate Movie!");
}
