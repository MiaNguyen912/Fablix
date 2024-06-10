## CS 122B Project 2

### Demo Video Link: 
https://drive.google.com/file/d/1ccJ6_6PSUbBA619_IHwiN-H9Sb4tBlf1/view?usp=sharing
- The throughput numbers are different than the readme as it only was able to run for a few seconds

### Jmeter throughput:
1. K8s cluster has 1 Control Plane + 3 Worker nodes + 1 master MySQL pod + 1 slave MySQL pod + 2 Fabflix pods:
- 381.792/minute
2. K8s Cluster has 1 Control Plane + 4 Worker nodes + 1 master MySQL pod + 1 slave MySQL pod + 3 Fabflix pods:
- 446.582/minute

### Contributions: 
Mia:
- created the autocomplete servlet for movie title
- upgrade to movie title search box to use full-text search and autocomplete (on both client and employee sites)
- imported Edit Distance function and added fuzzy-search feature into the search box and search result

Daniel:
- Set up kubernetes cluster on AWS
- Migrated moviedb database to mysql instances
- Deployed fablix website on Kubernetes and applied appropriate config changes in the repository and docker image

### Connection Pooling
- Include the filename/path of all code/configuration files in GitHub of using JDBC Connection Pooling.
  - WebContent\META-INF\context.xml
- Explain how Connection Pooling is utilized in the Fabflix code:
  - Connection pooling is enabled by using a factory to create and manage datasources in the connection pool. This is enabled in the context.xml file, where we also set configurations like the maximum amount of connections, how long they can be idle for, as well as how long the application would wait for a connection.
  - We also modified the URL to cache prepared statements to handle more than one connection
- Explain how Connection Pooling works with two backend SQL:
  - Connection pooling allows us to reuse connections for multiple queries instead of creating and deleting new ones for each one
  - In our project we used a single connection pool resource and relied on the load balancer servers to route to the correct master/slave instances.
  - We also used prepared statements to ensure the SQL statements are precompiled and stored in the connection pool
  - However, we could optimize further by creating separate connection pools for read and write operations, each configured to route to their respective instance.
    - This would allow us to optimize resources specific to either read and write operations, thus using computing resources more efficiently.

### Master/Slave
- Include the filename/path of all code/configuration files in GitHub of routing queries to Master/Slave SQL.
  - On each loadbalancer instance (Not github): /etc/apache2/sites-enabled/000-default.conf
- How read/write requests were routed to Master/Slave SQL?
  - Directly edited the apache configuration files to route specific pages where write requests were made
  - For example, I'd create a new proxy to route only to the master instance:
    ```
    <Proxy "balancer://master_balancer">
      BalancerMember "http://34.225.112.155:8080/cs122b-project5-TomcatPooling-example" route=1
      ProxySet stickysession=ROUTEID
    </Proxy>
    ```
  - Then in the VirtualHost tag have it route those specific queries to the master-only proxy to write properly to the database:
    ```
    ProxyPass /_dashboard/loggedin/api/add-movie balancer://master_balancer
    ProxyPassReverse /_dashboard/loggedin/api/add-movie balancer://master_balancer
    ```
  - All read requests are also routed to either slave or master instance using the default round-robin algorithm.



  
### Files with Prepared Statements
- [ConfirmationServlet.java](src/CartAndPaymentServlet/ConfirmationServlet.java)
- [CartServlet.java](src/CartAndPaymentServlet/CartServlet.java)
- [PaymentServlet.java](src/CartAndPaymentServlet/PaymentServlet.java)
- [GenresServlet.java](src/DataServlet/GenresServlet.java)
- [ListServlet.java](src/DataServlet/ListServlet.java)
- [MetadataServet.java](src/DataServlet/MetadataServet.java)
- [Top20MoviesServlet.java](src/DataServlet/Top20MoviesServlet.java)
- [SingleStarServlet.java](src/DataServlet/SingleStarServlet.java)
- [SingleMovieServlet.java](src/DataServlet/SingleMovieServlet.java)
- [LoginServlet.java](src/LoginServlet/LoginServlet.java)
- [StaffLoginServlet.java](src/LoginServlet/StaffLoginServlet.java)
- [UpdateSecurePassword.java](src/LoginServlet/UpdateSecurePassword.java)
- [UpdateSecurePasswordStaff.java](src/LoginServlet/UpdateSecurePasswordStaff.java)
- [MovieDomParser.java](src/XMLParser/MovieDomParser.java)
- [StarDomParser.java](src/XMLParser/StarDomParser.java)

### parsing time optimization strategies:
- use a batch to store multiple queries and use executeBatch() to execute all insertion at once
- In [StarDomParser.java](src/XMLParser/StarDomParser.java): parse all necessary data and save them in maps/lists to avoid querying the DB everytime we insert
- In [MovieDomParser.java](src/XMLParser/MovieDomParser.java): loop through the movies list only once and concurrently add statements into 2 batches (the 2 batches store insert statements that are used to insert data into 2 different tables), then execute 2 batches continuously to save time

### XML Parsing error messages:
- [actors_parsing_error_messages.txt](actors_parsing_error_messages.txt)
- [movies_parsing_error_messages.txt](movies_parsing_error_messages.txt)


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

- [DataServlet.SingleMovieServlet.java](src/SingleMovieServlet.java), [DataServlet.SingleStarServlet.java](src/SingleStarServlet.java), [DataServlet.Top20MoviesServlet.java](src/Top20MoviesServlet.java): Java servlets that handle HTTP GET request by talking to the database and return information in the JSON format.


- [listServlet.java](...): generate API result when user searches for movies based on title's starting letter, by genre, or by searching for keyword (handle HTTP GET requests)


- [Utility.User.java](...): a utility class that has variables and functions use for the login user


- [LoginServlet.LoginServlet.java](src/LoginServlet.java) handles the login requests. It contains the following functionalities:
   - It gets the username and password from the parameters.
   - It verifies the username and password.
   - If login succeeds, it puts the `Utility.User` object in the session. Then it sends back a JSON response: `{"status": "success", "message": "success"}` .
   - If login fails, the JSON response will be: `{"status": "fail", "message": "incorrect password"}` or `{"status": "fail", "message": "user <username> doesn't exist"}`.


- [LoginServlet.LoginFilter.java](src/LoginFilter.java) is a special `Filter` class. It serves the purpose that for each URL request, if the user is not logged in, then it redirects the user to the `login.html` page.
   - A `Filter` class intercepts all incoming requests and determines if such requests are allowed against the rules we implement. See more details about `Filter` class [here](http://tutorials.jenkov.com/java-servlets/servlet-filters.html).
   - In `Filter`, all requests will pass through the `doFilter` function.
   - `LoginServlet.LoginFilter` first checks if the request (the URL pattern) maps to the home page or login page (which are allowed to access without login).
   - It then checks if the user has logged in to the current session. If so, it redirects the user to the requested URL and if otherwise,`login.html` .


- [CartAndPaymentServlet.CartServlet.java](...): handle HTTP POST request when user finalize their items in cart and go to checkout page


- [PaymentServet.java](src/IndexServlet.java): has method doPOST, which is invoked with HTTP POST requests, responsible for validating card information 


- [UpdateSecurePassword.java](...) and [UpdateSecurePasswordStaff.java](...): encode plan-text password of customers and staffs


- [MovieDomParser.java](...) and [StarDomParser.java](...): parse the XML files


- [AddMovieServlet.java](...) and [AddStarServlet.java](...): call the stored procedures to add new movie and star


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
2. Run [LoginServlet.UpdateSecurePassword.java](src/main/java/UpdateSecurePassword.java) to update passwords in customers table from plain text to encrypted string.
3. Run [LoginServlet.VerifyPassword.java](src/main/java/VerifyPassword.java) to verify if the email and password are valid.
4. To recover the data in the "customers" table, run the following:
   <br>`update customers C1 set password = (select password from customers_backup C2 where C2.id = C1.id);`
5. Repeat these steps for the employees table (run [LoginServlet.UpdateSecurePasswordStaff.java](src/main/java/UpdateSecurePassword.java) instead)
    ####
        CREATE TABLE IF NOT EXISTS employees_backup(
            email varchar(50) primary key,
            password varchar(20) not null,
            fullname varchar(100)
        );
        insert into employees_backup select * from employees;
6. To recover the data in the "employees" table, run the following:
   <br>`update employees E1 set password = (select password from employees_backup E2 where E2.email = E1.email);`

### Config recaptcha
- Get a reCAPTCHA from Google. v3 Admin Console -> Register a new site -> Choose Challenge (v2) -> Enter both your AWS public IP and "localhost" in "Domains" section -> Click "Submit"
- In src/LoginServlet.RecaptchaConstants.java, replace YOUR_SECRET_KEY; with your own reCAPTCHA secret key.
- In WebContent/index.html, replace data-sitekey="YOUR_SITE_KEY" with your own reCAPTCHA site key.


### populating DB and do bulk import from XML files:
1. run createtable.sql
2. run movie-data.sql
3. edittables.sql
4. UpdateSecurePassword.java (with aws instance, run: mvn exec:java -Dexec.mainClass="LoginServlet.UpdateSecurePassword")
5. UpdateSecurePasswordStaff.java (with aws instance, run: mvn exec:java -Dexec.mainClass="LoginServlet.UpdateSecurePasswordStaff")
6. MovieDomParser.java (bulk import from main243.xml) (remember to check location of the XML file) 
7. StarDomParser.java (bulk import from actors63.xml and casts124.xml) (remember to check location of the XML file)
8. stored-procedure.sql
9. add the edit distance function for fuzzy-searching into database (see below)
   
### Adding edit distance UDF function into the database:
- UDF: user-defined function, in this case it's written in C, easier to implement than SQL function. 
- to use this C function in SQL (check out https://flamingo.ics.uci.edu/toolkit/ to see), we will compile the function into a shared library with extension format ".so" ->  save this library into SQL database -> n sql, create an SQL function and provide it the library name 
    - go to https://flamingo.ics.uci.edu/toolkit/ -> docs folder: to see step-by-step documentation
    - at https://flamingo.ics.uci.edu/toolkit/, download the file toolkit_2021-05-18.tgz:
    - in AWS instance terminal, copy the link address and run 'wget <link-address>' to download
    - run 'tar xvf toolkit_2021-05-18.tgz' to unzip the file, run 'ls' to see a new folder 'toolkit' exists
    - 'cd toolkit/src/udf/mysql/ed/' (ed is edit distance) to see the 'makefile' and all C programs written
    - run 'vim ed.c' to see the function
    - to use this function as a UDF, we need to download the C-compiler gcc package: 'sudo apt-get install gcc make mysql-server libmysqlclient-dev'
    - run 'make' so compile the C programs into libraries (run 'ls -ls *.so' to see the libraries)
    - copy the libraries into /usr/lib/mysql/plugin/ so that MySQL sees them: 'sudo cp libed*.so /usr/lib/mysql/plugin/'
    - restart MySQL: 'sudo /etc/init.d/mysql restart'
    - Now we can add the functions to MySQL and test them. To do that we can connect to MySQL and Copy-and-Paste the SQL statements provided for each function in their respective .sql file
      - for example, at directory 'toolkit/src/udf/mysql/ed/' run 'vim ed.sql' to see the sql statement provided for edit distance function ('CREATE FUNCTION ed RETURNS INTEGER SONAME 'libed.so';), copy and run this statement
      - now, we can test the function by running 'SELECT * from stars where ed(name, 'Paul Newman') <= 1' (note that 1 is the number of error we can tolerate)
  
