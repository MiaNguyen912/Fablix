package XMLParser;
import Utility.Movie;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.sql.DataSource;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;

/* mains243.xml format:
<movies>
    <!-- Movie Year: 1922 - Early British Movies by Alfred Hitchcock. -->
   <directorfilms>
        <director>
            <dirid>H</dirid>
            <dirstart>@1922</dirstart>
            <dirname>Hitchcock</dirname>
            <coverage>all early British</coverage>
         </director>
         <films>
               <film>
                    <fid>H1</fid>
                    <t>Always Tell Your Wife</t>
                    <year>1922</year>
                    <dirs>
                        <dir>
                            <dirk>R</dirk>
                            <dirn>Se.Hicks</dirn>
                         </dir>
                         <dir>
                            <dirk>R</dirk>
                            <dirn>Hitchcock</dirn>
                         </dir>
                    </dirs>
                    <prods>
                        <prod>
                            <prodk>R</prodk>
                            <pname>Lasky</pname>
                        </prod>
                    </prods>
                    <studios>
                        <studio>Famous</studio>
                    </studios>
                    <prcs>
                        <prc>sbw</prc>
                    </prcs>
                    <cats>
                        <cat>Dram</cat>
                    </cats>
                    <awards></awards>
                    <loc></loc>
                    <notes/>
                </film>
                ...
         </films>
   </directorfilms>
   ...
</movies>


* */


public class MovieDomParser {

    private static final String MOVIES_TABLE = "movies"; // may create a movies_backup table for testing
    private static final String GENRES_TABLE = "genres"; // may create a genres_backup table for testing
    private static final String GENRES_IN_MOVIES_TABLE = "genres_in_movies"; // may create a genres_in_movies_backup table for testing
    private static final String RATINGS_TABLE = "ratings";
    private DataSource dataSource;
    List<Movie> movies = new ArrayList<>();
    LinkedHashSet<String> all_genres = new LinkedHashSet<>();

    LinkedHashSet<String> all_movieId = new LinkedHashSet<>();
    Map<String, Integer> all_movieTitle_year = new HashMap<>();

    Document dom;

    public void runParser() {
        parseXmlFile(); // parse the xml file and get the dom object
        parseDocument(); // get each employee element and create a Employee object
        //printData(); // iterate through the list and print the data
    }

    private void parseXmlFile() {
        // get the factory
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();


        try {

            // using factory get an instance of document builder
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();

            // parse using builder to get DOM representation of the XML file
            dom = documentBuilder.parse("XML/mains243.xml");

        } catch (ParserConfigurationException | SAXException | IOException error) {
            error.printStackTrace();
        }
    }
    private void parseDocument() {
        // get the document root Element
        Element documentElement = dom.getDocumentElement();

        // get a nodelist of star Elements, parse each into Utility.Star object
        NodeList filmsOfDirector = documentElement.getElementsByTagName("directorfilms");
        for (int i = 0; i < filmsOfDirector.getLength(); i++) {
            Element element = (Element) filmsOfDirector.item(i);
            ArrayList<Movie> movies_of_current_director = parseMovie(element);
            movies.addAll(movies_of_current_director);
        }
    }

    private ArrayList<Movie> parseMovie(Element element) {
        ArrayList<Movie> movies = new ArrayList<>();

        // for each <directorfilms> element there are one <director> and one <films>
        // parse director's data
        NodeList directorsList = element.getElementsByTagName("director");
        Element directorElement = (Element) directorsList.item(0);
        String director = getTextValue(directorElement, "dirname");

        // parse films
        NodeList filmsList = element.getElementsByTagName("film");
        for (int i = 0; i < filmsList.getLength(); i++) {
            Element filmElement = (Element) filmsList.item(i);
            String filmId = getTextValue(filmElement, "fid");
            String title = getTextValue(filmElement, "t");
            int year = getIntValue(filmElement, "year", title);

            if (filmId == null || filmId.isEmpty() || filmId.trim().isEmpty()){
                System.out.println("Error parsing string value for tag <fid> --- value: (null) --- movie:" + title);
                continue; // skip this movie
            }
            if (title == null || title.isEmpty() || title.trim().isEmpty()){
                System.out.println("Error parsing string value for tag <t> --- value: (null) --- movie: (blank movie title)");
                continue; // skip this movie
            }
            if (director == null || director.isEmpty() || director.trim().isEmpty()){
                director = "Unknown";
                System.out.println("Error parsing string value for tag <dirname> --- value: (blank, replaced by 'Unknown') --- movie: " + title);
            }

            if (year == -1){
                // error message for invalid year is already printed out in getIntValue()
                continue;
            }


            ArrayList<String> genres = new ArrayList<>();
            NodeList catsList = filmElement.getElementsByTagName("cat");
            for (int j = 0; j < catsList.getLength(); j++) {
                Element categoryElement = (Element) catsList.item(j);
                String new_genre = categoryElement.getTextContent();
                if (!new_genre.isEmpty()){
                    new_genre =  new_genre.trim();
                }
                if (!new_genre.isEmpty()){
                    // capitalize new_genre and add to genres list
                    new_genre = new_genre.substring(0, 1).toUpperCase() + new_genre.substring(1).toLowerCase();
                    genres.add(new_genre);
                    all_genres.add(new_genre);
                }
            }
            if (genres.isEmpty()){
                genres.add("Unknown");
                all_genres.add("Unknown");
            }

            // update all_movieID
            if (all_movieId.contains(filmId)){
                System.out.println("Error: duplicate value of movie ID --- <fid> --- value: " + filmId + " --- movie: " + title);
                continue; // skip this movie
            } else {
                all_movieId.add(filmId);
            }

            // update all_movieTitle (some movies might have same title but released in different years, which are valid)
            if (all_movieTitle_year.containsKey(title) && all_movieTitle_year.get(title) == year){
                System.out.println("Error: duplicate value of movie --- <t> --- value: " + title);
                continue; // skip this movie
            } else {
                all_movieTitle_year.put(title, year) ;
            }

            // Create a Random price
            Random random = new Random();
            int price = random.nextInt(60) + 1;

            // create new Movie object and add to movies list
            Movie m = new Movie(filmId, title, year, director, price);
            m.setGenres(genres);
            movies.add(m);
        }
        return movies;
    }

    private String getTextValue(Element element, String tagName) {
        String textVal = null;
        NodeList nodeList = element.getElementsByTagName(tagName);
        if (nodeList.getLength() > 0) {
            if (nodeList.item(0).getFirstChild() != null){
                textVal = (nodeList.item(0).getFirstChild().getNodeValue());
            } else {
                textVal = "";
            }
        }
        return textVal;
    }

    private int getIntValue(Element ele, String tagName, String movieTitle) {
        int intVal = -1;
        String textVal = "";
        try {
            textVal = getTextValue(ele, tagName);
            String modifiedTextVal = "";
            if (!textVal.isEmpty()){
                modifiedTextVal = textVal.replaceAll("[^a-zA-Z0-9]", ""); // remove non-alphanumeric characters
            }
            intVal = Integer.parseInt(modifiedTextVal);
        } catch (NumberFormatException e) {
            // Handle the case where the text cannot be parsed as an integer
            if (textVal.equals(""))
                textVal = "(blank)";
            System.out.println("Error parsing integer value for tag <" + tagName + "> --- value: " + textVal + " --- movie: " + movieTitle);

        } catch (Exception e) {
            // Handle other exceptions (e.g., unexpected XML structure)
            System.out.println("Unexpected error processing tag " + tagName + ": " + e.getMessage());
        }
        return intVal;
    }
    private void printData() {
        System.out.println("Total parsed " + movies.size() + " movies");
        for (Movie m : movies) {
            System.out.println("\t" + m.toString());
        }
    }

    public static int getLastGenreID() {
        int lastID = 1; //default value
        try {
            try(Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/moviedb", "mytestuser", "My6$Password")){;
                String query = "select max(id) from " + GENRES_TABLE;
                PreparedStatement statement = conn.prepareStatement(query);
                ResultSet rs = statement.executeQuery();
                if (rs.next()) {
                    lastID = rs.getInt(1);
                    // System.out.println(lastID);
                }
                rs.close();
                statement.close();
            }
            catch (Exception e) {
                System.out.println("errorMessage" + e.getMessage());
            }
        }
        catch (Exception e) {e.printStackTrace();}
        return lastID;
    }

    private void insertIntoGenresTable(){
        try {
            String loginUser = "mytestuser";
            String loginPasswd = "My6$Password";
            String loginUrl = "jdbc:mysql://localhost:3306/moviedb";

            Class.forName("com.mysql.jdbc.Driver").newInstance();
            Connection connection = DriverManager.getConnection(loginUrl, loginUser, loginPasswd);
            PreparedStatement statement = null;
            int[] iNoRows = null;
            String genreInsertQuery = "INSERT INTO " + GENRES_TABLE + "(name) VALUES (?)";
            try {
                connection.setAutoCommit(false);
                statement = connection.prepareStatement(genreInsertQuery);
                System.out.println("Updating genres");
                for (String genre : all_genres)
                {
                    statement.setString(1, genre);
                    statement.addBatch();
                }
                iNoRows = statement.executeBatch(); // Each int in the array represents the update count for each command in the batch.
                connection.commit();
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                if (statement!=null) statement.close();
                if(connection!=null) connection.close();
            } catch(Exception e) {
                e.printStackTrace();
            }
            System.out.println("Updating genres completed, " + iNoRows.length + " rows affected");
        }
        catch (Exception e) {e.printStackTrace();}
    }
    private void insertDataToBD(){
        try {
            String loginUser = "mytestuser";
            String loginPasswd = "My6$Password";
            String loginUrl = "jdbc:mysql://localhost:3306/moviedb";

            Class.forName("com.mysql.jdbc.Driver").newInstance();
            Connection connection = DriverManager.getConnection(loginUrl, loginUser, loginPasswd);
            PreparedStatement movieInsertStatement = null;
            PreparedStatement genreInMovieInsertStatement = null;


            int[] movieRowsAffected = null;
            int[] genres_in_moviesRowsAffected = null;

            String movieInsertQuery = "INSERT INTO " + MOVIES_TABLE + "(id, title, year, director, price) VALUES (?, ?, ?, ?, ?)";
            String genreInMovieInsertQuery = "INSERT INTO " + GENRES_IN_MOVIES_TABLE + "(genreId, movieId) SELECT id, ? FROM " + GENRES_TABLE + " g WHERE g.name = ?";

            try {
                connection.setAutoCommit(false);
                movieInsertStatement = connection.prepareStatement(movieInsertQuery);
                genreInMovieInsertStatement = connection.prepareStatement(genreInMovieInsertQuery);

                for(Movie m : movies)
                {
                    String movieId = m.getId();
                    String title = m.getTitle();
                    int year = m.getYear();
                    String director = m.getDirector();
                    int price = m.getPrice();
                    ArrayList<String> genres = m.getGenres();

                    // prepare movieInsertStatement
                    movieInsertStatement.setString(1, movieId);
                    movieInsertStatement.setString(2, title);
                    movieInsertStatement.setInt(3, year);
                    movieInsertStatement.setString(4, director);
                    movieInsertStatement.setInt(5, price);
                    movieInsertStatement.addBatch();

                    // prepare genreInMovieInsertStatement
                    for (String genre : genres){
                        genreInMovieInsertStatement.setString(2, genre);
                        genreInMovieInsertStatement.setString(1, movieId);
                        genreInMovieInsertStatement.addBatch();
                    }
                }
                movieRowsAffected = movieInsertStatement.executeBatch(); // Each int in the array represents the update count for each command in the batch.
                genres_in_moviesRowsAffected = genreInMovieInsertStatement.executeBatch();
                connection.commit();
            } catch (Exception e) {
                e.printStackTrace();

            }

            try {
                if (movieInsertStatement!=null) movieInsertStatement.close();
                if (genreInMovieInsertStatement!=null) genreInMovieInsertStatement.close();
                if(connection!=null) connection.close();
            } catch(Exception e) {
                e.printStackTrace();
            }
            System.out.println("Updating new movies completed, " + movieRowsAffected.length + " rows affected");
            System.out.println("Updating genres in movies completed, " + genres_in_moviesRowsAffected.length + " rows affected");

        }
        catch (Exception e) {e.printStackTrace();}
    }


    private void updateRatingsTable(){
        try {
            String loginUser = "mytestuser";
            String loginPasswd = "My6$Password";
            String loginUrl = "jdbc:mysql://localhost:3306/moviedb";

            Class.forName("com.mysql.jdbc.Driver").newInstance();
            Connection connection = DriverManager.getConnection(loginUrl, loginUser, loginPasswd);
            PreparedStatement statement = null;
            String alterTableQuery = "ALTER TABLE " + RATINGS_TABLE + " MODIFY Rating float NULL, MODIFY Numvotes integer NULL;";
            statement = connection.prepareStatement(alterTableQuery);
            statement.executeUpdate();

            String ratingsInsertQuery = "INSERT INTO " + RATINGS_TABLE + "(movieid, rating, numvotes)  SELECT id, null, null FROM " + MOVIES_TABLE + " m WHERE NOT EXISTS " +
                                        " (SELECT 1 FROM " + RATINGS_TABLE + " r WHERE r.movieid = m.id)";

            int numRowAffected = 0;
            statement = connection.prepareStatement(ratingsInsertQuery);
            numRowAffected = statement.executeUpdate();

            statement.close();
            connection.close();

            System.out.println("Updating ratings table completed, " + numRowAffected + " rows affected");

        }
        catch (Exception e) {e.printStackTrace();}
    }

    public static void main(String[] args) {
        // connect to DB and get the current max ID (last ID) of genre in the genres table
        int lastGenreID = getLastGenreID();

        // create a parser instance
        MovieDomParser domParser = new MovieDomParser();

        // start parsing xml
        domParser.runParser();

        // insert new genres into Genres table
        domParser.insertIntoGenresTable();

        // insert data into movies and genres_in_movies table
        domParser.insertDataToBD();

        // update ratings table to add null rating for newly added movies
        domParser.updateRatingsTable();
    }

}
