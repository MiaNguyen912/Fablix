let isLoggedIn = sessionStorage.getItem('isLoggedIn');

console.log("User have logged in: " + isLoggedIn);
if (isLoggedIn){
    let login_btn = document.getElementById("login-btn");
    login_btn.style.display = "none";

} else {
    let logout_btn = document.getElementById("logout-btn");
    let cart_btn = document.getElementById("cart-btn");
    let browse_ddl = document.getElementById("browse-dropdown-list");
    let search_form = document.getElementById("seach-form");

    logout_btn.style.display = "none";
    cart_btn.style.display = "none";
    browse_ddl.style.display = "none";
    search_form.style.display = "none";
}

document.addEventListener('DOMContentLoaded', function() {
    const menuButton = document.getElementById('menu-button');
    const dropdownMenu = document.getElementById('drop-down-menu');
    menuButton.addEventListener('click', function() {
        dropdownMenu.classList.toggle('active');
    });

});


function handleLogOut(){
    // sessionStorage.setItem('isLoggedIn', 'false'); // update isLoggedIn information onto session storage


    // // Make an AJAX request to the servlet endpoint
    // jQuery.ajax({
    //     dataType: "json", // Setting return data type
    //     method: "GET", // Setting request method
    //     url: "api/logout",
    //     success: (resultData) => console.log(resultData) // Setting callback function to handle data returned successfully by the StarsServlet
    // });
}