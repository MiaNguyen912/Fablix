## CS 122B Project 1

### Demo Video Link: 
https://drive.google.com/file/d/1IjAYOmAGrMq2puDwuztdXVVTQuwHT7x6/view?usp=drive_link

### Contributions: 
Mia:
- 

  
Daniel:
- Fix single star page to show movies
- Add year of birth for each star
- Created single movie page and updated movie links to go to correct movie page
- Add links to go back to main page from each single star/movie page

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

- `...Servlet.java` files: are Java servlets that talk to the database and return information in the JSON format.

- `index.js` is the main Javascript file that initiates an HTTP GET request to the `Top20MoviesServlet`. After the response is returned, `index.js` populates the table using the data it gets.

- `index.html` is the main HTML file that contains the initial skeleton for the table.


### DataSource
- For project 1, you are recommended to use tomcat to manage your DataSource instead of manually define MySQL connection in each of the servlet.

- `WebContent/META-INF/context.xml` contains a DataSource, with database information stored in it.
`WEB-INF/web.xml` registers the DataSource to name jdbc/moviedb, which could be referred to anywhere in the project.

- In each `...Servlet.java`, a private DataSource reference dataSource is created with `@Resource` annotation. It is a reference to the DataSource `jdbc/moviedb` we registered in `web.xml`

- To use DataSource, you can create a new connection to it by `dataSource.getConnection()`, and you can use the connection as previous examples.
