## CS 122B Project 1

### Demo Video Link: 
https://drive.google.com/file/d/1IjAYOmAGrMq2puDwuztdXVVTQuwHT7x6/view?usp=drive_link

### Contributions: 
Mia:
- Build the API (SingleMovieServlet, SingleStarServlet, Top20MovieServlet)
- Create createtable.sql
- Construct the home page, which is the top-20-movies page (front-end and back-end)
- Beautify top-20-movies, single-movie, single-star pages using Tailwind CSS and plain CSS
- add List view/Grid view feature on the home page

  
Daniel:
- construct the back-end of single-star and single-movie (front-end and back-end)
- Help debug and fix errors 
- Add the year of birth for each star
- Updated movie links to go to the correct movie page
- Add links to go back to the main page from each single star/movie page

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

- [SessionIndexServlet.java](src/IndexServlet.java) shows your current session information, last access time, and a list of movies added to cart. Has two methods, `doPost` and `doGet`.
   *  `doGET`: is invoked when you have HTTP GET requests through the api `/api/session-index`
      * It first gets the session ID, overrides the last access time, and writes these values in the JSON Object that is sent through `response`.
   *  `doPOST`: is invoked with HTTP POST requests, responsible for the movie cart feature.
      * First, it gets the session ID and the list of movies from the current session.
      * If there is no such array of movies, it will create an empty array and add chosen movie.
      * Sends the list of items through `cart.js`.


- Web pages that required log in to access are put in `authenticated` folder
- only the home page (index.html) and login page (login.html) are accessible without logging in



### DataSource
- `WebContent/META-INF/context.xml` contains a DataSource, with database information stored in it.
`WEB-INF/web.xml` registers the DataSource to name jdbc/moviedb, which could be referred to anywhere in the project.

- In each `...Servlet.java`, a private DataSource reference dataSource is created with `@Resource` annotation. It is a reference to the DataSource `jdbc/moviedb` we registered in `web.xml`

- To use DataSource, you can create a new connection to it by `dataSource.getConnection()`.
