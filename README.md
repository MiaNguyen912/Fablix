## CS 122B Project 2

### Demo Video Link: 
https://drive.google.com/drive/folders/10XUPX29k_KBGcLwpegHCa0qt7Qlegs7X?usp=sharing
- It's in 2 parts as the video recording software froze near the end, but I made sure to restart the recording right where we left off with additional cart items

### Contributions: 
Mia:
- Construct the login page and login logic
- construct the Header of each page to include browsing options, search bar, shopping cart button, and logout button
- Build the search and browse movie functions (front-end and back-end)
- Build the sort/filter/pagination of the result page when user browses or searches for movies
- Modify single-movie and single-star servlets to display data in the correct order (genre by alphabetical order, stars by decreasing number of movies they play)
- Beautify the cart, checkout, and confirmation pages with tailwind css

  
Daniel:
- Build list page to reroute search results from genre, title, and substring search
- Build cart page to show items in cart as well as logic to keep track of cart contents
- Build checkout page to take in credit card information and check with backend database
- Draft confirmation page to display successful sale result and summarized information

### Before running the project

#### If you do not have USER `mytestuser` setup in MySQL, follow the below steps to create it:

 - login to mysql as a root user 
    ```
    local> mysql -u root -p
    ```

 - create a test user and grant privileges:
    ```
    mysql> CREATE USER 'mytestuser'@'localhost' IDENTIFIED BY 'My6$Password';
    mysql> GRANT ALL PRIVILEGES ON * . * TO 'mytestuser'@'localhost';
    mysql> quit;
    ```

#### prepare the database `moviedb`
 

```
local> mysql -u mytestuser -p
mysql> source <Path-to-CreatTable.sql>;
mysql> source <Path-to-Movie-date.sql>;
mysql> source <Path-to-edittable.sql>;
mysql> quit;
```

### To run this example: 
1. Clone this repository using `git clone`
2. Open IntelliJ -> Import Project -> Choose the project you just cloned (The root path must contain the pom.xml!) -> Choose Import project from external model -> choose Maven -> Click on Finish -> The IntelliJ will load automatically
3. run "npm install"
4. Load Maven Project
5. For "Root Directory", right click "cs122b-project1-api-example" -> Mark Directory as -> sources root
6. In `WebContent/META-INF/context.xml`, make sure the mysql username is `mytestuser` and password is `My6$Password`.
7. Also make sure you have the `moviedb` database.
8. Configure Tomcat Apache and add a .war file as an Artifact
9. Run the Tomcat server

### If you modify css using tailwind properties:
1. Delete the existing tailwind.css in WebContent/assets
2. In terminal, run 'npx tailwindcss -i ./WebContent/assets/style.css -o ./WebContent/assets/tailwind.css --watch' to combine the style.css with necessary tailwind property into tailwind.css file
3. Wait for the file tailwind.css to be generated inside WebContent/assets

### Brief Explanation
- This project uses `jQuery` for making HTTP requests and manipulate DOM.

- [SingleMovieServlet.java](src/SingleMovieServlet.java), [SingleStarServlet.java](src/SingleStarServlet.java), [Top20MoviesServlet.java](src/Top20MoviesServlet.java): Java servlets that handle HTTP GET request by talking to the database and return information in the JSON format.


- [listServlet.java](...): generate API result when user searches for movies based on title's starting letter, by genre, or by searching for keyword (handle HTTP GET requests)


- [User.java](...): a utility class that has variables and functions use for the login user


- [LoginServlet.java](src/LoginServlet.java) handles the login requests. It contains the following functionalities:
   - It gets the username and password from the parameters.
   - It verifies the username and password.
   - If login succeeds, it puts the `User` object in the session. Then it sends back a JSON response: `{"status": "success", "message": "success"}` .
   - If login fails, the JSON response will be: `{"status": "fail", "message": "incorrect password"}` or `{"status": "fail", "message": "user <username> doesn't exist"}`.


- [LoginFilter.java](src/LoginFilter.java) is a special `Filter` class. It serves the purpose that for each URL request, if the user is not logged in, then it redirects the user to the `login.html` page.
   - A `Filter` class intercepts all incoming requests and determines if such requests are allowed against the rules we implement. See more details about `Filter` class [here](http://tutorials.jenkov.com/java-servlets/servlet-filters.html).
   - In `Filter`, all requests will pass through the `doFilter` function.
   - `LoginFilter` first checks if the request (the URL pattern) maps to the home page or login page (which are allowed to access without login).
   - It then checks if the user has logged in to the current session. If so, it redirects the user to the requested URL and if otherwise,`login.html` .


- [CartServlet.java](...): handle HTTP POST request when user finalize their items in cart and go to checkout page


- [PaymentServet.java](src/IndexServlet.java): has method doPOST, which is invoked with HTTP POST requests, responsible for validating card information 



- Web pages that required log in to access are put in `authenticated` folder
- only the home page (index.html) and login page (login.html) are accessible without logging in

### Searching logic
- if title is specified: find 'title%' or '% title%' (title should match at the beginning or after a space)
- if year is specified: find results with the exact specified year 
- if director is specified: find '%director%' (director can match at the beginning, middle, or end)
- if star name is specified: find '%star%' (star can match at the beginning, middle, or end)
- results are arranged in alphabetical order for movie title
#### Seach query example:

    SELECT * FROM ratings r
    JOIN movies m ON r.movieid = m.id
    JOIN genres_in_movies gm ON gm.movieid = r.movieid
    JOIN genres g ON g.id = gm.genreid
    JOIN stars_in_movies sm ON sm.movieid = r.movieid
    JOIN stars s ON s.id = sm.starid
    JOIN ( SELECT starid, COUNT(movieid) AS movie_count FROM stars_in_movies GROUP BY starid) sp ON s.id = sp.starid
    JOIN (SELECT sm.movieid
        FROM stars_in_movies sm
        JOIN stars s ON s.id = sm.starid
        WHERE s.name LIKE '%tom%'
    ) as movies_of_chosen_star ON movies_of_chosen_star.movieid = m.id
    WHERE m.title LIKE 'term%' or m.title LIKE '% term%'
          AND m.year = 2004 
          AND m.director LIKE '%Steven%'
          AND
    ORDER BY m.title, sp.movie_count DESC, s.name ASC;

### DataSource
- `WebContent/META-INF/context.xml` contains a DataSource, with database information stored in it.
`WEB-INF/web.xml` registers the DataSource to name jdbc/moviedb, which could be referred to anywhere in the project.

- In each `...Servlet.java`, a private DataSource reference dataSource is created with `@Resource` annotation. It is a reference to the DataSource `jdbc/moviedb` we registered in `web.xml`

- To use DataSource, you can create a new connection to it by `dataSource.getConnection()`.

### Files with Prepared Statements
- [Top20MoviesServlet.java](src/Top20MoviesServlet.java)
- [SingleStarServlet.java](...)
- [SingleMovieServlet.java](...)
- [PaymentServlet.java](...)
- [LoginServlet.java](...)
- [ListServlet.java](...)
- [GenresServlet.java](...)
- [ConfirmationServlet.java](...)
- [CartServlet.java](...)
- [UpdatePassword.java](...)
- [UpdatePasswordStaff.java](...)



### Encrypting password: (using http://www.jasypt.org/)
(make sure to change your mysql username and password in the 2 files below)
1. Create a backup of the "customers" table:
    ####
        create table customers_backup(
          id integer auto_increment primary key,
          firstName varchar(50) not null,
          lastName varchar(50) not null,
          ccId varchar(20) not null,
          address varchar(200) not null,
          email varchar(50) not null,
          password varchar(20) not null,
          foreign key(ccId) references creditcards(id)
        );
        insert into customers_backup select * from customers;
2. Run [UpdateSecurePassword.java](src/main/java/UpdateSecurePassword.java) to update passwords in customers table from plain text to encrypted string.
3. Run [VerifyPassword.java](src/main/java/VerifyPassword.java) to verify if the email and password are valid.
4. To recover the data in the "customers" table, run the following:
   <br>`update customers C1 set password = (select password from customers_backup C2 where C2.id = C1.id);`
5. Repeat these steps for the employees table (run [UpdateSecurePasswordStaff.java](src/main/java/UpdateSecurePassword.java) instead)
    ####
        CREATE TABLE IF NOT EXISTS employees_backup(
            email varchar(50) primary key,
            password varchar(20) not null,
            fullname varchar(100)
        );
        insert into employees_backup select * from employees;
6. To recover the data in the "employees" table, run the following:
   <br>`update employees E1 set password = (select password from employees_backup E2 where E2.email = E1.email);`
