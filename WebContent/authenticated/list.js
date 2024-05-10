function getParameterByName(target) {
    // Get request URL
    let url = window.location.href;
    // Encode target parameter name to url encoding
    target = target.replace(/[\[\]]/g, "\\$&");

    // Ues regular expression to find matched parameter value
    let regex = new RegExp("[?&]" + target + "(=([^&#]*)|&|#|$)"),
        results = regex.exec(url);
    if (!results) return null;
    if (!results[2]) return '';

    // Return the decoded parameter value
    return decodeURIComponent(results[2].replace(/\+/g, " "));
}


/**
 * Adds the specified movieId into the cart that's saved in sessionStorage
 * @param button button that was clicked
 */
function addToCart(button){
    let movie_id = button.getAttribute('data-movie-id');
    let movie_title = button.getAttribute('data-movie-title');
    alert(movie_title + " has been added to your cart");


    let cartData = JSON.parse(sessionStorage.getItem('cart')) || {};

    // Check if the movie_id already exists in cartData
    if (cartData[movie_id]) {
        // Increment the quantity if the movie_id is already in the cart
        cartData[movie_id]++;
    } else {
        // Initialize the quantity to 1 if the movie_id is not yet in the cart
        cartData[movie_id] = 1;
    }

    // Save updated cart data back to sessionStorage
    sessionStorage.setItem('cart', JSON.stringify(cartData));
}

/**
 * Handles the data returned by the API, read the jsonObject and populate data into html elements
 * @param resultData jsonObject
 */
function handleMoviesByGenreResult(resultData) {
    console.log("Handle movies by title results");

    // --------- display page number
    let page_number = document.getElementById("page-number");
    let page = sessionStorage.getItem('page');
    if (page == null) page = 1;
    page_number.innerHTML = page

    // ----------- populate movie table
    let movieTableBodyElement = document.getElementById("movie_table_body");
    let movieGridElement = document.getElementById("movie_grid");
    movieTableBodyElement.innerHTML = ''; // Clear the content
    movieGridElement.innerHTML = '';

    for (let i = 0; i < resultData.length; i++) {
        let movie_id = resultData[i]["movie_id"];
        let movie_title = resultData[i]["movie_title"];
        let movie_year = resultData[i]["movie_year"];
        let movie_director = resultData[i]["movie_director"];
        let movie_rating = resultData[i]["movie_rating"];
        if (movie_rating === 0 || movie_rating === null){
            movie_rating = "N/A"
        }
        let stars = resultData[i]["stars"];
        let genres = resultData[i]["genres"];

        let genres_id = [];
        let genres_name = [];
        let count = 0;
        for (const key in genres){
            if (count < 3) {
                genres_id.push(key);
                genres_name.push(genres[key]);
                count++;
            } else {
                break;
            }
        }
        // for (const key in genres){
        //     genres_id.push(key);
        //     genres_name.push(genres[key]);
        // }

        let stars_id = [];
        let stars_name = [];
        count = 0;
        for (const key in stars){
            if (count < 3) {
                stars_id.push(key);
                stars_name.push(stars[key]);
                count++;
            } else {
                break;
            }
        }
        // for (const key in stars){
        //     stars_id.push(key);
        //     stars_name.push(stars[key]);
        // }

        // Concatenate the html tags with resultData jsonObject
        let rowHTML = "";
        rowHTML += "<tr>";
        rowHTML += "<td><a href='single-movie.html?id=" + movie_id  + "'> " + movie_title + "</a></td>";
        rowHTML += "<td>" + movie_year + "</td>";
        rowHTML += "<td>" + movie_director + "</td>";

        // genres
        let genresText = "";
        for (let i = 0; i<genres_id.length; i++){
            if (i < genres_id.length -1 )
                genresText += "<a href='list.html?type=genre&name=" + genres_name[i] + "'> " + genres_name[i] + "</a>, ";
            else
                genresText += "<a href='list.html?type=genre&name=" + genres_name[i] + "'> " + genres_name[i] + "</a>";
        }
        rowHTML += "<td>" + genresText + "</td>";

        // stars
        let starsText = "";
        for (let i = 0; i<stars_id.length; i++){
            if (i < stars_id.length -1 )
                starsText += "<a href='single-star.html?id=" + stars_id[i] + "'> " + stars_name[i] + "</a>, ";
            else
                starsText += "<a href='single-star.html?id=" + stars_id[i] + "'> " + stars_name[i] + "</a> ...";
        }
        rowHTML += "<td>" + starsText + "</td>";


        rowHTML += "<td>" + movie_rating + "</td>";

        rowHTML +=  "<td><button onclick='addToCart(this)' class='rounded-md px-3 py-2.5 text-lg font-semibold text-gray-50 shadow-sm ring-1 ring-inset ring-gray-300 bg-orange-500 hover:bg-orange-700' data-movie-id='" + movie_id + "' data-movie-title='" + movie_title + "'>Add</button></td>";





        // Append the row created to the table body, which will refresh the page
        movieTableBodyElement.innerHTML += rowHTML;


        //------------------------------------------------
        let gridItemHTML = '        <li class="col-span-1 divide-y divide-gray-200 rounded-lg bg-white shadow">\n' +
            '            <div class="rounded-lg bg-gray-50 shadow-sm ring-1 ring-gray-900/5">\n' +
            '                <dl class="flex flex-wrap">\n' +
            '                    <div class="flex-auto pl-6 pt-6">\n' +
            '                        <dt class="text-base leading-6 text-gray-900"><strong><a href=single-movie.html?id=' + movie_id + '>' + movie_title + '</a></strong> <span class="text-sm text-gray-500">(' + movie_year + ')</span></dt>\n' +
            '                        <dd class="mt-1 text-sm font-semibold leading-6 text-gray-900">' + genresText + '</dd>\n' +
            '                    </div>\n' +
            '                    <div class="flex-none self-end px-6 pt-4">\n' +
            '                        <dt class="sr-only">Index</dt>\n' +
            '                        <dd class="inline-flex items-center rounded-md bg-green-50 px-2 py-1 text-xs font-medium text-green-700 ring-1 ring-inset ring-green-600/20">'+
            '                           <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 20 20" fill="currentColor" class="w-3 h-3">\n' +
            '                               <path fill-rule="evenodd" d="M10.868 2.884c-.321-.772-1.415-.772-1.736 0l-1.83 4.401-4.753.381c-.833.067-1.171 1.107-.536 1.651l3.62 3.102-1.106 4.637c-.194.813.691 1.456 1.405 1.02L10 15.591l4.069 2.485c.713.436 1.598-.207 1.404-1.02l-1.106-4.637 3.62-3.102c.635-.544.297-1.584-.536-1.65l-4.752-.382-1.831-4.401Z" clip-rule="evenodd" />\n' +
            '                           </svg> ' + movie_rating +
            '                        </dd>\n' +
            '                    </div>\n' +
            '                    <div class="mt-6 flex w-full flex-none gap-x-4 border-t border-gray-900/5 px-6 pt-6">\n' +
            '                        <dt class="flex-none">\n' +
            '                            <span class="sr-only">Detail</span>\n' +
            '                            <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="1.5"\n stroke="currentColor" class="w-6 h-6">\n' +
            '                               <path stroke-linecap="round" stroke-linejoin="round" d="M3.375 19.5h17.25m-17.25 0a1.125 1.125 0 0 1-1.125-1.125M3.375 19.5h1.5C5.496 19.5 6 18.996 6 18.375m-3.75 0V5.625m0 12.75v-1.5c0-.621.504-1.125 1.125-1.125m18.375 2.625V5.625m0 12.75c0 .621-.504 1.125-1.125 1.125m1.125-1.125v-1.5c0-.621-.504-1.125-1.125-1.125m0 3.75h-1.5A1.125 1.125 0 0 1 18 18.375M20.625 4.5H3.375m17.25 0c.621 0 1.125.504 1.125 1.125M20.625 4.5h-1.5C18.504 4.5 18 5.004 18 5.625m3.75 0v1.5c0 .621-.504 1.125-1.125 1.125M3.375 4.5c-.621 0-1.125.504-1.125 1.125M3.375 4.5h1.5C5.496 4.5 6 5.004 6 5.625m-3.75 0v1.5c0 .621.504 1.125 1.125 1.125m0 0h1.5m-1.5 0c-.621 0-1.125.504-1.125 1.125v1.5c0 .621.504 1.125 1.125 1.125m1.5-3.75C5.496 8.25 6 7.746 6 7.125v-1.5M4.875 8.25C5.496 8.25 6 8.754 6 9.375v1.5m0-5.25v5.25m0-5.25C6 5.004 6.504 4.5 7.125 4.5h9.75c.621 0 1.125.504 1.125 1.125m1.125 2.625h1.5m-1.5 0A1.125 1.125 0 0 1 18 7.125v-1.5m1.125 2.625c-.621 0-1.125.504-1.125 1.125v1.5m2.625-2.625c.621 0 1.125.504 1.125 1.125v1.5c0 .621-.504 1.125-1.125 1.125M18 5.625v5.25M7.125 12h9.75m-9.75 0A1.125 1.125 0 0 1 6 10.875M7.125 12C6.504 12 6 12.504 6 13.125m0-2.25C6 11.496 5.496 12 4.875 12M18 10.875c0 .621-.504 1.125-1.125 1.125M18 10.875c0 .621.504 1.125 1.125 1.125m-2.25 0c.621 0 1.125.504 1.125 1.125m-12 5.25v-5.25m0 5.25c0 .621.504 1.125 1.125 1.125h9.75c.621 0 1.125-.504 1.125-1.125m-12 0v-1.5c0-.621-.504-1.125-1.125-1.125M18 18.375v-5.25m0 5.25v-1.5c0-.621.504-1.125 1.125-1.125M18 13.125v1.5c0 .621.504 1.125 1.125 1.125M18 13.125c0-.621.504-1.125 1.125-1.125M6 13.125v1.5c0 .621-.504 1.125-1.125 1.125M6 13.125C6 12.504 5.496 12 4.875 12m-1.5 0h1.5m-1.5 0c-.621 0-1.125.504-1.125 1.125v1.5c0 .621.504 1.125 1.125 1.125M19.125 12h1.5m0 0c.621 0 1.125.504 1.125 1.125v1.5c0 .621-.504 1.125-1.125 1.125m-17.25 0h1.5m14.25 0h1.5" />\n' +
            '                            </svg>' +
            '                        </dt>\n' +
            '                        <dd class="text-sm leading-6 text-gray-500">' + movie_director + '</dd>\n' +
            '                    </div>\n' +
            '                    <div class="mt-4 flex w-full flex-none gap-x-4 px-6 pb-6">\n' +
            '                        <dt class="flex-none">\n' +
            '                            <span class="sr-only">Cast</span>\n' +
            '                            <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="1.5" stroke="currentColor" class="w-6 h-6">\n' +
            '                               <path stroke-linecap="round" stroke-linejoin="round" d="M11.48 3.499a.562.562 0 0 1 1.04 0l2.125 5.111a.563.563 0 0 0 .475.345l5.518.442c.499.04.701.663.321.988l-4.204 3.602a.563.563 0 0 0-.182.557l1.285 5.385a.562.562 0 0 1-.84.61l-4.725-2.885a.562.562 0 0 0-.586 0L6.982 20.54a.562.562 0 0 1-.84-.61l1.285-5.386a.562.562 0 0 0-.182-.557l-4.204-3.602a.562.562 0 0 1 .321-.988l5.518-.442a.563.563 0 0 0 .475-.345L11.48 3.5Z" />' +
            '                            </svg>\n' +
            '                        </dt>\n' +
            '                        <dd class="text-sm leading-6 text-gray-900">' + starsText + '</dd>\n' +
            '                    </div>\n' +
            '                </dl>\n' +
            '            </div>\n' +
            '        </li>\n';

        movieGridElement.innerHTML += gridItemHTML;

    }
}

/**
 * Handles the data returned by the API, read the jsonObject and populate data into html elements
 * @param resultData jsonObject
 */
function handleSearchResult(resultData) {
    console.log("Handle movies by title results");

    // --------- display page number
    let page_number = document.getElementById("page-number");
    page_number.innerHTML = sessionStorage.getItem('page');

    // ----------- populate movie table
    let movieTableBodyElement = document.getElementById("movie_table_body");
    let movieGridElement = document.getElementById("movie_grid");
    movieTableBodyElement.innerHTML = ''; // Clear the content
    movieGridElement.innerHTML = '';

    // Iterate through resultData, no more than 10 entries
    for (let i = 0; i < resultData.length; i++) {
        let movie_id = resultData[i]["movie_id"];
        let movie_title = resultData[i]["movie_title"];
        let movie_year = resultData[i]["movie_year"];
        let movie_director = resultData[i]["movie_director"];
        let movie_rating = resultData[i]["movie_rating"];
        if (movie_rating === 0 || movie_rating === null){
            movie_rating = "N/A"
        }
        let stars = resultData[i]["stars"];
        let genres = resultData[i]["genres"];

        let genres_id = [];
        let genres_name = [];
        let count = 0;
        for (const key in genres){
            if (count < 3) {
                genres_id.push(key);
                genres_name.push(genres[key]);
                count++;
            } else {
                break;
            }
        }


        let stars_id = [];
        let stars_name = [];
        count = 0;
        for (const key in stars){
            if (count < 3) {
                stars_id.push(key);
                stars_name.push(stars[key]);
                count++;
            } else {
                break;
            }
        }

        // for (const key in genres){
        //     genres_id.push(key);
        //     genres_name.push(genres[key]);
        // }

        // let stars_id = [];
        // let stars_name = [];
        // for (const key in stars){
        //     stars_id.push(key);
        //     stars_name.push(stars[key]);
        // }

        // Concatenate the html tags with resultData jsonObject
        let rowHTML = "";
        rowHTML += "<tr>";
        rowHTML += "<td><a href='single-movie.html?id=" + movie_id  + "'> " + movie_title + "</a></td>";
        rowHTML += "<td>" + movie_year + "</td>";
        rowHTML += "<td>" + movie_director + "</td>";

        // genres
        let genresText = "";
        for (let i = 0; i<genres_id.length; i++){
            if (i < genres_id.length -1 )
                genresText += "<a href='list.html?type=genre&name=" + genres_name[i] + "'> " + genres_name[i] + "</a>, ";
            else
                genresText += "<a href='list.html?type=genre&name=" + genres_name[i] + "'> " + genres_name[i] + "</a>";
        }
        rowHTML += "<td>" + genresText + "</td>";

        // stars
        let starsText = "";
        for (let i = 0; i<stars_id.length; i++){
            if (i < stars_id.length -1 )
                starsText += "<a href='single-star.html?id=" + stars_id[i] + "'> " + stars_name[i] + "</a>, ";
            else
                starsText += "<a href='single-star.html?id=" + stars_id[i] + "'> " + stars_name[i] + "</a> ...";
        }
        rowHTML += "<td>" + starsText + "</td>";
        rowHTML += "<td>" + movie_rating + "</td>";
        rowHTML +=  "<td><button onclick='addToCart(this)' class='rounded-md px-3 py-2.5 text-lg font-semibold text-gray-50 shadow-sm ring-1 ring-inset ring-gray-300 bg-orange-500 hover:bg-orange-700' data-movie-id='" + movie_id + "' data-movie-title='" + movie_title + "'>Add</button></td>";

        // Append the row created to the table body, which will refresh the page
        movieTableBodyElement.innerHTML += rowHTML;

        //------------------------------------------------
        let gridItemHTML = '        <li class="col-span-1 divide-y divide-gray-200 rounded-lg bg-white shadow">\n' +
            '            <div class="rounded-lg bg-gray-50 shadow-sm ring-1 ring-gray-900/5">\n' +
            '                <dl class="flex flex-wrap">\n' +
            '                    <div class="flex-auto pl-6 pt-6">\n' +
            '                        <dt class="text-base leading-6 text-gray-900"><strong><a href=single-movie.html?id=' + movie_id + '>' + movie_title + '</a></strong> <span class="text-sm text-gray-500">(' + movie_year + ')</span></dt>\n' +
            '                        <dd class="mt-1 text-sm font-semibold leading-6 text-gray-900">' + genresText + '</dd>\n' +
            '                    </div>\n' +
            '                    <div class="flex-none self-end px-6 pt-4">\n' +
            '                        <dt class="sr-only">Index</dt>\n' +
            '                        <dd class="inline-flex items-center rounded-md bg-green-50 px-2 py-1 text-xs font-medium text-green-700 ring-1 ring-inset ring-green-600/20">'+
            '                           <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 20 20" fill="currentColor" class="w-3 h-3">\n' +
            '                               <path fill-rule="evenodd" d="M10.868 2.884c-.321-.772-1.415-.772-1.736 0l-1.83 4.401-4.753.381c-.833.067-1.171 1.107-.536 1.651l3.62 3.102-1.106 4.637c-.194.813.691 1.456 1.405 1.02L10 15.591l4.069 2.485c.713.436 1.598-.207 1.404-1.02l-1.106-4.637 3.62-3.102c.635-.544.297-1.584-.536-1.65l-4.752-.382-1.831-4.401Z" clip-rule="evenodd" />\n' +
            '                           </svg> ' + movie_rating +
            '                        </dd>\n' +
            '                    </div>\n' +
            '                    <div class="mt-6 flex w-full flex-none gap-x-4 border-t border-gray-900/5 px-6 pt-6">\n' +
            '                        <dt class="flex-none">\n' +
            '                            <span class="sr-only">Detail</span>\n' +
            '                            <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="1.5"\n stroke="currentColor" class="w-6 h-6">\n' +
            '                               <path stroke-linecap="round" stroke-linejoin="round" d="M3.375 19.5h17.25m-17.25 0a1.125 1.125 0 0 1-1.125-1.125M3.375 19.5h1.5C5.496 19.5 6 18.996 6 18.375m-3.75 0V5.625m0 12.75v-1.5c0-.621.504-1.125 1.125-1.125m18.375 2.625V5.625m0 12.75c0 .621-.504 1.125-1.125 1.125m1.125-1.125v-1.5c0-.621-.504-1.125-1.125-1.125m0 3.75h-1.5A1.125 1.125 0 0 1 18 18.375M20.625 4.5H3.375m17.25 0c.621 0 1.125.504 1.125 1.125M20.625 4.5h-1.5C18.504 4.5 18 5.004 18 5.625m3.75 0v1.5c0 .621-.504 1.125-1.125 1.125M3.375 4.5c-.621 0-1.125.504-1.125 1.125M3.375 4.5h1.5C5.496 4.5 6 5.004 6 5.625m-3.75 0v1.5c0 .621.504 1.125 1.125 1.125m0 0h1.5m-1.5 0c-.621 0-1.125.504-1.125 1.125v1.5c0 .621.504 1.125 1.125 1.125m1.5-3.75C5.496 8.25 6 7.746 6 7.125v-1.5M4.875 8.25C5.496 8.25 6 8.754 6 9.375v1.5m0-5.25v5.25m0-5.25C6 5.004 6.504 4.5 7.125 4.5h9.75c.621 0 1.125.504 1.125 1.125m1.125 2.625h1.5m-1.5 0A1.125 1.125 0 0 1 18 7.125v-1.5m1.125 2.625c-.621 0-1.125.504-1.125 1.125v1.5m2.625-2.625c.621 0 1.125.504 1.125 1.125v1.5c0 .621-.504 1.125-1.125 1.125M18 5.625v5.25M7.125 12h9.75m-9.75 0A1.125 1.125 0 0 1 6 10.875M7.125 12C6.504 12 6 12.504 6 13.125m0-2.25C6 11.496 5.496 12 4.875 12M18 10.875c0 .621-.504 1.125-1.125 1.125M18 10.875c0 .621.504 1.125 1.125 1.125m-2.25 0c.621 0 1.125.504 1.125 1.125m-12 5.25v-5.25m0 5.25c0 .621.504 1.125 1.125 1.125h9.75c.621 0 1.125-.504 1.125-1.125m-12 0v-1.5c0-.621-.504-1.125-1.125-1.125M18 18.375v-5.25m0 5.25v-1.5c0-.621.504-1.125 1.125-1.125M18 13.125v1.5c0 .621.504 1.125 1.125 1.125M18 13.125c0-.621.504-1.125 1.125-1.125M6 13.125v1.5c0 .621-.504 1.125-1.125 1.125M6 13.125C6 12.504 5.496 12 4.875 12m-1.5 0h1.5m-1.5 0c-.621 0-1.125.504-1.125 1.125v1.5c0 .621.504 1.125 1.125 1.125M19.125 12h1.5m0 0c.621 0 1.125.504 1.125 1.125v1.5c0 .621-.504 1.125-1.125 1.125m-17.25 0h1.5m14.25 0h1.5" />\n' +
            '                            </svg>' +
            '                        </dt>\n' +
            '                        <dd class="text-sm leading-6 text-gray-500">' + movie_director + '</dd>\n' +
            '                    </div>\n' +
            '                    <div class="mt-4 flex w-full flex-none gap-x-4 px-6 pb-6">\n' +
            '                        <dt class="flex-none">\n' +
            '                            <span class="sr-only">Cast</span>\n' +
            '                            <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="1.5" stroke="currentColor" class="w-6 h-6">\n' +
            '                               <path stroke-linecap="round" stroke-linejoin="round" d="M11.48 3.499a.562.562 0 0 1 1.04 0l2.125 5.111a.563.563 0 0 0 .475.345l5.518.442c.499.04.701.663.321.988l-4.204 3.602a.563.563 0 0 0-.182.557l1.285 5.385a.562.562 0 0 1-.84.61l-4.725-2.885a.562.562 0 0 0-.586 0L6.982 20.54a.562.562 0 0 1-.84-.61l1.285-5.386a.562.562 0 0 0-.182-.557l-4.204-3.602a.562.562 0 0 1 .321-.988l5.518-.442a.563.563 0 0 0 .475-.345L11.48 3.5Z" />' +
            '                            </svg>\n' +
            '                        </dt>\n' +
            '                        <dd class="text-sm leading-6 text-gray-900">' + starsText + '</dd>\n' +
            '                    </div>\n' +
            '                </dl>\n' +
            '            </div>\n' +
            '        </li>\n';

        movieGridElement.innerHTML += gridItemHTML;
    }
}


/**
 * Handles the data returned by the API, read the jsonObject and populate data into html elements
 * @param resultData jsonObject
 */
function handleMoviesByTitleResult(resultData) {
    console.log("Handle movies by title results");

    // --------- display page number
    let page_number = document.getElementById("page-number");
    let page = sessionStorage.getItem('page');
    if (page == null) page = 1;
    page_number.innerHTML = page

    // ----------- populate movie table
    let movieTableBodyElement = document.getElementById("movie_table_body");
    let movieGridElement = document.getElementById("movie_grid");
    movieTableBodyElement.innerHTML = ''; // Clear the content
    movieGridElement.innerHTML = '';

    for (let i = 0; i < resultData.length; i++) {
        let movie_id = resultData[i]["movie_id"];
        let movie_title = resultData[i]["movie_title"];
        let movie_year = resultData[i]["movie_year"];
        let movie_director = resultData[i]["movie_director"];
        let movie_rating = resultData[i]["movie_rating"];
        if (movie_rating === 0 || movie_rating === null){
            movie_rating = "N/A"
        }
        let stars = resultData[i]["stars"];
        let genres = resultData[i]["genres"];

        let genres_id = [];
        let genres_name = [];
        let count = 0;
        for (const key in genres){
            if (count < 3) {
                genres_id.push(key);
                genres_name.push(genres[key]);
                count++;
            } else {
                break;
            }
        }
        // for (const key in genres){
        //     genres_id.push(key);
        //     genres_name.push(genres[key]);
        // }

        let stars_id = [];
        let stars_name = [];
        count = 0;
        for (const key in stars){
            if (count < 3) {
                stars_id.push(key);
                stars_name.push(stars[key]);
                count++;
            } else {
                break;
            }
        }
        // for (const key in stars){
        //     stars_id.push(key);
        //     stars_name.push(stars[key]);
        // }


        // Concatenate the html tags with resultData jsonObject
        let rowHTML = "";
        rowHTML += "<tr>";
        rowHTML += "<td><a href='single-movie.html?id=" + movie_id  + "'> " + movie_title + "</a></td>";
        rowHTML += "<td>" + movie_year + "</td>";
        rowHTML += "<td>" + movie_director + "</td>";

        // genres
        let genresText = "";
        for (let i = 0; i<genres_id.length; i++){
            if (i < genres_id.length -1 )
                genresText += "<a href='list.html?type=genre&name=" + genres_name[i] + "'> " + genres_name[i] + "</a>, ";
            else
                genresText += "<a href='list.html?type=genre&name=" + genres_name[i] + "'> " + genres_name[i] + "</a>";
        }
        rowHTML += "<td>" + genresText + "</td>";

        // stars
        let starsText = "";
        for (let i = 0; i<stars_id.length; i++){
            if (i < stars_id.length -1 )
                starsText += "<a href='single-star.html?id=" + stars_id[i] + "'> " + stars_name[i] + "</a>, ";
            else
                starsText += "<a href='single-star.html?id=" + stars_id[i] + "'> " + stars_name[i] + "</a> ...";
        }
        rowHTML += "<td>" + starsText + "</td>";
        rowHTML += "<td>" + movie_rating + "</td>";
        rowHTML +=  "<td><button onclick='addToCart(this)' class='rounded-md px-3 py-2.5 text-lg font-semibold text-gray-50 shadow-sm ring-1 ring-inset ring-gray-300 bg-orange-500 hover:bg-orange-700' data-movie-id='" + movie_id + "' data-movie-title='" + movie_title + "'>Add</button></td>";


        // Append the row created to the table body, which will refresh the page
        movieTableBodyElement.innerHTML += rowHTML;

        //------------------------------------------------
        let gridItemHTML = '        <li class="col-span-1 divide-y divide-gray-200 rounded-lg bg-white shadow">\n' +
            '            <div class="rounded-lg bg-gray-50 shadow-sm ring-1 ring-gray-900/5">\n' +
            '                <dl class="flex flex-wrap">\n' +
            '                    <div class="flex-auto pl-6 pt-6">\n' +
            '                        <dt class="text-base leading-6 text-gray-900"><strong><a href=single-movie.html?id=' + movie_id + '>' + movie_title + '</a></strong> <span class="text-sm text-gray-500">(' + movie_year + ')</span></dt>\n' +
            '                        <dd class="mt-1 text-sm font-semibold leading-6 text-gray-900">' + genresText + '</dd>\n' +
            '                    </div>\n' +
            '                    <div class="flex-none self-end px-6 pt-4">\n' +
            '                        <dt class="sr-only">Index</dt>\n' +
            '                        <dd class="inline-flex items-center rounded-md bg-green-50 px-2 py-1 text-xs font-medium text-green-700 ring-1 ring-inset ring-green-600/20">'+
            '                           <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 20 20" fill="currentColor" class="w-3 h-3">\n' +
            '                               <path fill-rule="evenodd" d="M10.868 2.884c-.321-.772-1.415-.772-1.736 0l-1.83 4.401-4.753.381c-.833.067-1.171 1.107-.536 1.651l3.62 3.102-1.106 4.637c-.194.813.691 1.456 1.405 1.02L10 15.591l4.069 2.485c.713.436 1.598-.207 1.404-1.02l-1.106-4.637 3.62-3.102c.635-.544.297-1.584-.536-1.65l-4.752-.382-1.831-4.401Z" clip-rule="evenodd" />\n' +
            '                           </svg> ' + movie_rating +
            '                        </dd>\n' +
            '                    </div>\n' +
            '                    <div class="mt-6 flex w-full flex-none gap-x-4 border-t border-gray-900/5 px-6 pt-6">\n' +
            '                        <dt class="flex-none">\n' +
            '                            <span class="sr-only">Detail</span>\n' +
            '                            <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="1.5"\n stroke="currentColor" class="w-6 h-6">\n' +
            '                               <path stroke-linecap="round" stroke-linejoin="round" d="M3.375 19.5h17.25m-17.25 0a1.125 1.125 0 0 1-1.125-1.125M3.375 19.5h1.5C5.496 19.5 6 18.996 6 18.375m-3.75 0V5.625m0 12.75v-1.5c0-.621.504-1.125 1.125-1.125m18.375 2.625V5.625m0 12.75c0 .621-.504 1.125-1.125 1.125m1.125-1.125v-1.5c0-.621-.504-1.125-1.125-1.125m0 3.75h-1.5A1.125 1.125 0 0 1 18 18.375M20.625 4.5H3.375m17.25 0c.621 0 1.125.504 1.125 1.125M20.625 4.5h-1.5C18.504 4.5 18 5.004 18 5.625m3.75 0v1.5c0 .621-.504 1.125-1.125 1.125M3.375 4.5c-.621 0-1.125.504-1.125 1.125M3.375 4.5h1.5C5.496 4.5 6 5.004 6 5.625m-3.75 0v1.5c0 .621.504 1.125 1.125 1.125m0 0h1.5m-1.5 0c-.621 0-1.125.504-1.125 1.125v1.5c0 .621.504 1.125 1.125 1.125m1.5-3.75C5.496 8.25 6 7.746 6 7.125v-1.5M4.875 8.25C5.496 8.25 6 8.754 6 9.375v1.5m0-5.25v5.25m0-5.25C6 5.004 6.504 4.5 7.125 4.5h9.75c.621 0 1.125.504 1.125 1.125m1.125 2.625h1.5m-1.5 0A1.125 1.125 0 0 1 18 7.125v-1.5m1.125 2.625c-.621 0-1.125.504-1.125 1.125v1.5m2.625-2.625c.621 0 1.125.504 1.125 1.125v1.5c0 .621-.504 1.125-1.125 1.125M18 5.625v5.25M7.125 12h9.75m-9.75 0A1.125 1.125 0 0 1 6 10.875M7.125 12C6.504 12 6 12.504 6 13.125m0-2.25C6 11.496 5.496 12 4.875 12M18 10.875c0 .621-.504 1.125-1.125 1.125M18 10.875c0 .621.504 1.125 1.125 1.125m-2.25 0c.621 0 1.125.504 1.125 1.125m-12 5.25v-5.25m0 5.25c0 .621.504 1.125 1.125 1.125h9.75c.621 0 1.125-.504 1.125-1.125m-12 0v-1.5c0-.621-.504-1.125-1.125-1.125M18 18.375v-5.25m0 5.25v-1.5c0-.621.504-1.125 1.125-1.125M18 13.125v1.5c0 .621.504 1.125 1.125 1.125M18 13.125c0-.621.504-1.125 1.125-1.125M6 13.125v1.5c0 .621-.504 1.125-1.125 1.125M6 13.125C6 12.504 5.496 12 4.875 12m-1.5 0h1.5m-1.5 0c-.621 0-1.125.504-1.125 1.125v1.5c0 .621.504 1.125 1.125 1.125M19.125 12h1.5m0 0c.621 0 1.125.504 1.125 1.125v1.5c0 .621-.504 1.125-1.125 1.125m-17.25 0h1.5m14.25 0h1.5" />\n' +
            '                            </svg>' +
            '                        </dt>\n' +
            '                        <dd class="text-sm leading-6 text-gray-500">' + movie_director + '</dd>\n' +
            '                    </div>\n' +
            '                    <div class="mt-4 flex w-full flex-none gap-x-4 px-6 pb-6">\n' +
            '                        <dt class="flex-none">\n' +
            '                            <span class="sr-only">Cast</span>\n' +
            '                            <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="1.5" stroke="currentColor" class="w-6 h-6">\n' +
            '                               <path stroke-linecap="round" stroke-linejoin="round" d="M11.48 3.499a.562.562 0 0 1 1.04 0l2.125 5.111a.563.563 0 0 0 .475.345l5.518.442c.499.04.701.663.321.988l-4.204 3.602a.563.563 0 0 0-.182.557l1.285 5.385a.562.562 0 0 1-.84.61l-4.725-2.885a.562.562 0 0 0-.586 0L6.982 20.54a.562.562 0 0 1-.84-.61l1.285-5.386a.562.562 0 0 0-.182-.557l-4.204-3.602a.562.562 0 0 1 .321-.988l5.518-.442a.563.563 0 0 0 .475-.345L11.48 3.5Z" />' +
            '                            </svg>\n' +
            '                        </dt>\n' +
            '                        <dd class="text-sm leading-6 text-gray-900">' + starsText + '</dd>\n' +
            '                    </div>\n' +
            '                </dl>\n' +
            '            </div>\n' +
            '        </li>\n';

        movieGridElement.innerHTML += gridItemHTML;
    }
}


/**
 * Once this .js is loaded, following scripts will be executed by the browser
 */

// Execute different code for different types
let type = getParameterByName('type');
sessionStorage.setItem('search_type', type);


if (type === "genre"){
    // Get params from URL and session storage
    let genre = getParameterByName('name');
    sessionStorage.setItem('genre', genre);

    let goback = getParameterByName('goback');
    let limit;
    let sort;
    let page

    if (goback == null){
        limit= "10"; // default initial value
        sort = "title_asc_rating_asc"; // default initial value
        page = "1"; // default initial value
        sessionStorage.setItem('limit', limit);
        sessionStorage.setItem('sort', sort);
        sessionStorage.setItem('page', page);
    } else {
        limit= sessionStorage.getItem('limit');
        sort = sessionStorage.getItem('sort');
        page = sessionStorage.getItem('page');
    }


    // clear session variable of title first letter and search result
    sessionStorage.setItem('titleFirstLetter', null);
    sessionStorage.setItem('searchTitle', null);
    sessionStorage.setItem('searchYear', null);
    sessionStorage.setItem('searchDirector', null);
    sessionStorage.setItem('searchStar', null);


    // display selected sort type
    const sort_dropdownlist = document.getElementById("dropdown-sort-by");
    for (let option of sort_dropdownlist.options) {
        if (option.value === sort) {
            option.selected = true;
            break;
        }
    }

    // display selected movies-per-page
    const moviesperpage_dropdownlist = document.getElementById("dropdown-movies-per-page");
    for (let option of moviesperpage_dropdownlist.options) {
        if (option.value === limit) {
            option.selected = true;
            break;
        }
    }

    // Makes the HTTP GET request
    jQuery.ajax({
        dataType: "json", // Setting return data type
        method: "GET", // Setting request method
        url: "api/list?type=genre&name=" + genre + "&sort-style=" + sort + "&limit=" + limit + "&page=" + page,
        success: (resultData) => handleMoviesByGenreResult(resultData) // Setting callback function to handle data returned successfully by the StarsServlet
    });
}
else if (type === "title"){
    // Get params from URL and session storage
    let firstLetter = getParameterByName('start');
    sessionStorage.setItem('titleFirstLetter', firstLetter);

    let goback = getParameterByName('goback');
    let limit;
    let sort;
    let page

    if (goback == null){
        limit= "10"; // default initial value
        sort = "title_asc_rating_asc"; // default initial value
        page = "1"; // default initial value
        sessionStorage.setItem('limit', limit);
        sessionStorage.setItem('sort', sort);
        sessionStorage.setItem('page', page);
    } else {
        limit= sessionStorage.getItem('limit');
        sort = sessionStorage.getItem('sort');
        page = sessionStorage.getItem('page');
    }

    // clear session variable of genre and search result
    sessionStorage.setItem('genre', null);
    sessionStorage.setItem('searchTitle', null);
    sessionStorage.setItem('searchYear', null);
    sessionStorage.setItem('searchDirector', null);
    sessionStorage.setItem('searchStar', null);

    // display selected sort type
    const sort_dropdownlist = document.getElementById("dropdown-sort-by");
    for (let option of sort_dropdownlist.options) {
        if (option.value === sort) {
            option.selected = true;
            break;
        }
    }

    // display selected movies-per-page
    const moviesperpage_dropdownlist = document.getElementById("dropdown-movies-per-page");
    for (let option of moviesperpage_dropdownlist.options) {
        if (option.value === limit) {
            option.selected = true;
            break;
        }
    }

    // Makes the HTTP GET request
    jQuery.ajax({
        dataType: "json", // Setting return data type
        method: "GET", // Setting request method
        url: "api/list?type=title&start=" + firstLetter + "&sort-style=" + sort + "&limit=" + limit + "&page=" + page,
        success: (resultData) => handleMoviesByTitleResult(resultData) // Setting callback function to handle data returned successfully by the StarsServlet
    });
}
else if (type === "search"){
    /**
     * Once this .js is loaded, following scripts will be executed by the browser
     */
    // Get params from URL and session storage
    let searchTitle = getParameterByName('title');
    let searchYear = getParameterByName('year');
    let searchDirector = getParameterByName('director');
    let searchStar = getParameterByName('star');
    sessionStorage.setItem('searchTitle', searchTitle);
    sessionStorage.setItem('searchYear', searchYear);
    sessionStorage.setItem('searchDirector', searchDirector);
    sessionStorage.setItem('searchStar', searchStar);

    let goback = getParameterByName('goback');
    let limit;
    let sort;
    let page

    if (goback == null){
        limit= "10"; // default initial value
        sort = "title_asc_rating_asc"; // default initial value
        page = "1"; // default initial value
        sessionStorage.setItem('limit', limit);
        sessionStorage.setItem('sort', sort);
        sessionStorage.setItem('page', page);
    } else {
        limit= sessionStorage.getItem('limit');
        sort = sessionStorage.getItem('sort');
        page = sessionStorage.getItem('page');
    }

    // clear session variable of genre and title
    sessionStorage.setItem('genre', null);
    sessionStorage.setItem('titleFirstLetter', null);

    // display selected sort type
    const sort_dropdownlist = document.getElementById("dropdown-sort-by");
    for (let option of sort_dropdownlist.options) {
        if (option.value === sort) {
            option.selected = true;
            break;
        }
    }

    // display selected movies-per-page
    const moviesperpage_dropdownlist = document.getElementById("dropdown-movies-per-page");
    for (let option of moviesperpage_dropdownlist.options) {
        if (option.value === limit) {
            option.selected = true;
            break;
        }
    }

    // Makes the HTTP GET request
    jQuery.ajax({
        dataType: "json", // Setting return data type
        method: "GET", // Setting request method
        url: "api/list?type=search&title=" + searchTitle + "&year=" + searchYear + "&director=" + searchDirector + "&star=" + searchStar + "&sort-style=" + sort + "&limit=" + limit + "&page=" + page,
        success: (resultData) => handleSearchResult(resultData) // Setting callback function to handle data returned successfully by the StarsServlet
    });
}

// -------------------------

function handleSort(selectElement) {
    let selectedValue = selectElement.value;
    console.log("Sort by:", selectedValue);

    // save/retrieve data from session storage
    let genre = sessionStorage.getItem('genre');
    let titleFirstLetter = sessionStorage.getItem('titleFirstLetter');
    let searchTitle = sessionStorage.getItem('searchTitle');
    let searchYear = sessionStorage.getItem('searchYear');
    let searchDirector = sessionStorage.getItem('searchDirector');
    let searchStar = sessionStorage.getItem('searchStar');

    let limit = sessionStorage.getItem('limit');
    let sort = selectedValue;
    let page = "1"; // refresh page number
    sessionStorage.setItem('sort', sort);
    sessionStorage.setItem('page', page); // refresh page number


    if (titleFirstLetter === "null" && genre !== "null") {
        // do genre sort
        console.log("Sort on movies-by-genre page");
        jQuery.ajax({
            dataType: "json", // Setting return data type
            method: "GET", // Setting request method
            url: "api/list?type=genre&name=" + genre + "&sort-style=" + sort + "&limit=" + limit + "&page=" + page,
            success: (resultData) => handleMoviesByGenreResult(resultData) // Setting callback function to handle data returned successfully by the StarsServlet
        });
    } else if (genre === "null" && titleFirstLetter !== "null"){
        // do title sort
        console.log("Sort on movies-by-title page");
        jQuery.ajax({
            dataType: "json", // Setting return data type
            method: "GET", // Setting request method
            url: "api/list?type=title&start=" + titleFirstLetter + "&sort-style=" + sort + "&limit=" + limit + "&page=" + page,
            success: (resultData) => handleMoviesByTitleResult(resultData) // Setting callback function to handle data returned successfully by the StarsServlet
        });

    } else {
        // do search-result sort
        console.log("Sort on search results page");
        jQuery.ajax({
            dataType: "json", // Setting return data type
            method: "GET", // Setting request method
            url: "api/list?type=search&title=" + searchTitle + "&year=" + searchYear + "&director=" + searchDirector + "&star=" + searchStar + "&sort-style=" + sort + "&limit=" + limit + "&page=" + page,
            success: (resultData) => handleSearchResult(resultData) // Setting callback function to handle data returned successfully by the StarsServlet
        });
    }



}

function handleMoviesPerPage(selectElement) {
    let selectedValue = selectElement.value;
    console.log("Page limit:", selectedValue);

    // save/retrieve data from session storage
    let genre = sessionStorage.getItem('genre');
    let titleFirstLetter = sessionStorage.getItem('titleFirstLetter');
    let searchTitle = sessionStorage.getItem('searchTitle');
    let searchYear = sessionStorage.getItem('searchYear');
    let searchDirector = sessionStorage.getItem('searchDirector');
    let searchStar = sessionStorage.getItem('searchStar');

    let limit = selectedValue;
    let sort = sessionStorage.getItem('sort');
    let page = "1"; // refresh page number
    sessionStorage.setItem('limit', limit);
    sessionStorage.setItem('page', page); // refresh page number


    // Makes the HTTP GET request
    if (titleFirstLetter === "null" && genre !== "null") {
        // do genre sort
        console.log("Change number of movies per page on movies-by-genre page");
        jQuery.ajax({
            dataType: "json", // Setting return data type
            method: "GET", // Setting request method
            url: "api/list?type=genre&name=" + genre + "&sort-style=" + sort + "&limit=" + limit + "&page=" + page,
            success: (resultData) => handleMoviesByGenreResult(resultData) // Setting callback function to handle data returned successfully by the StarsServlet
        });
    } else if (genre === "null" && titleFirstLetter !== "null"){
        // do title sort
        console.log("Change number of movies per page on movies-by-title page");
        jQuery.ajax({
            dataType: "json", // Setting return data type
            method: "GET", // Setting request method
            url: "api/list?type=title&start=" + titleFirstLetter + "&sort-style=" + sort + "&limit=" + limit + "&page=" + page,
            success: (resultData) => handleMoviesByTitleResult(resultData) // Setting callback function to handle data returned successfully by the StarsServlet
        });

    } else {
        // do search-result sort
        console.log("Change number of movies per page on search results page");
        jQuery.ajax({
            dataType: "json", // Setting return data type
            method: "GET", // Setting request method
            url: "api/list?type=search&title=" + searchTitle + "&year=" + searchYear + "&director=" + searchDirector + "&star=" + searchStar + "&sort-style=" + sort + "&limit=" + limit + "&page=" + page,
            success: (resultData) => handleSearchResult(resultData) // Setting callback function to handle data returned successfully by the StarsServlet
        });
    }
}

function handlePreviousPage(){
    console.log("Go to previous page");

    // save/retrieve data from session storage
    let genre = sessionStorage.getItem('genre');
    let titleFirstLetter = sessionStorage.getItem('titleFirstLetter');
    let searchTitle = sessionStorage.getItem('searchTitle');
    let searchYear = sessionStorage.getItem('searchYear');
    let searchDirector = sessionStorage.getItem('searchDirector');
    let searchStar = sessionStorage.getItem('searchStar');

    let limit = sessionStorage.getItem('limit');
    let sort = sessionStorage.getItem('sort');
    let page = sessionStorage.getItem('page');

    let new_page = parseInt(page) - 1;
    if (new_page <= 0) new_page = 1;
    sessionStorage.setItem('page', new_page + "");



    // Makes the HTTP GET request
    if (titleFirstLetter === "null" && genre !== "null") {
        // do genre sort
        console.log("Go to previous page on movies-by-genre page");
        jQuery.ajax({
            dataType: "json", // Setting return data type
            method: "GET", // Setting request method
            url: "api/list?type=genre&name=" + genre + "&sort-style=" + sort + "&limit=" + limit + "&page=" + new_page,
            success: (resultData) => handleMoviesByGenreResult(resultData) // Setting callback function to handle data returned successfully by the StarsServlet
        });
    } else if (genre === "null" && titleFirstLetter !== "null"){
        // do title sort
        console.log("Go to previous page on movies-by-title page");
        jQuery.ajax({
            dataType: "json", // Setting return data type
            method: "GET", // Setting request method
            url: "api/list?type=title&start=" + titleFirstLetter + "&sort-style=" + sort + "&limit=" + limit + "&page=" + new_page,
            success: (resultData) => handleMoviesByTitleResult(resultData) // Setting callback function to handle data returned successfully by the StarsServlet
        });

    } else {
        // do search-result sort
        console.log("Go to previous page on search results page");
        jQuery.ajax({
            dataType: "json", // Setting return data type
            method: "GET", // Setting request method
            url: "api/list?type=search&title=" + searchTitle + "&year=" + searchYear + "&director=" + searchDirector + "&star=" + searchStar + "&sort-style=" + sort + "&limit=" + limit + "&page=" + new_page,
            success: (resultData) => handleSearchResult(resultData) // Setting callback function to handle data returned successfully by the StarsServlet
        });
    }
}

function handleNextPage(){
    console.log("Go to next page");

    // save/retrieve data from session storage
    let genre = sessionStorage.getItem('genre');
    let titleFirstLetter = sessionStorage.getItem('titleFirstLetter');
    let searchTitle = sessionStorage.getItem('searchTitle');
    let searchYear = sessionStorage.getItem('searchYear');
    let searchDirector = sessionStorage.getItem('searchDirector');
    let searchStar = sessionStorage.getItem('searchStar');

    let limit = sessionStorage.getItem('limit');
    let sort = sessionStorage.getItem('sort');
    let page = sessionStorage.getItem('page');

    let new_page = parseInt(page) + 1;
    sessionStorage.setItem('page', new_page + "");

    // Makes the HTTP GET request
    if (titleFirstLetter === "null" && genre !== "null") {
        // do genre sort
        console.log("Go to next page on movies-by-genre page");
        jQuery.ajax({
            dataType: "json", // Setting return data type
            method: "GET", // Setting request method
            url: "api/list?type=genre&name=" + genre + "&sort-style=" + sort + "&limit=" + limit + "&page=" + new_page,
            success: (resultData) => handleMoviesByGenreResult(resultData) // Setting callback function to handle data returned successfully by the StarsServlet
        });
    } else if (genre === "null" && titleFirstLetter !== "null"){
        // do title sort
        console.log("Go to next page on movies-by-title page");
        jQuery.ajax({
            dataType: "json", // Setting return data type
            method: "GET", // Setting request method
            url: "api/list?type=title&start=" + titleFirstLetter + "&sort-style=" + sort + "&limit=" + limit + "&page=" + new_page,
            success: (resultData) => handleMoviesByTitleResult(resultData) // Setting callback function to handle data returned successfully by the StarsServlet
        });

    } else {
        // do search-result sort
        console.log("Go to next page on search-result page");
        jQuery.ajax({
            dataType: "json", // Setting return data type
            method: "GET", // Setting request method
            url: "api/list?type=search&title=" + searchTitle + "&year=" + searchYear + "&director=" + searchDirector + "&star=" + searchStar + "&sort-style=" + sort + "&limit=" + limit + "&page=" + new_page,
            success: (resultData) => handleSearchResult(resultData) // Setting callback function to handle data returned successfully by the StarsServlet
        });
    }
}


