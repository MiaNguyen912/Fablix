package XMLParser;

import Utility.Star;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.sql.DataSource;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.sql.*;
import java.util.*;



public class StarDomParser {
    private static final String STARS_TABLE = "stars"; // may create a stars_backup table for testing
    private static final String STARS_IN_MOVIES_TABLE = "stars_in_movies"; // may create a stars_in_movies_backup table for testing
    private static final String MOVIES_TABLE = "movies"; // may create a movies_backup table for testing

    public static int lastStarID;

    HashMap<String, Integer> existing_stars_map = new HashMap<>();
    ArrayList<String> existing_movies_list = new ArrayList<>();
    HashMap<String, HashSet<String>> stars_in_movies = new HashMap<>();
    public static Map<String, Star> starName_starObject_map = new HashMap<>();
    Document dom;
    // ------------------------------------------------------------------------------------------------------------

    public static String getLastStarID() {
        String lastStarID_string = "";
        try {
            try(Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/moviedb", "mytestuser", "My6$Password")){;
            String query = "select max(id) from " + STARS_TABLE;
                PreparedStatement statement = conn.prepareStatement(query);
                ResultSet rs = statement.executeQuery();
                if (rs.next()) {
                    lastStarID_string = rs.getString(1);
                    if (lastStarID_string == null)
                        lastStarID_string = "nm0000000";
                    // System.out.println(lastStarID_string);
                }
                rs.close();
                statement.close();
            }
            catch (Exception e) {
                System.out.println("errorMessage" + e.getMessage());
            }
        }
        catch (Exception e) {e.printStackTrace();}
        return lastStarID_string;
    }

    public void runParser(String filename) {
        parseXmlFile(filename); // parse the xml file and get the dom object
        if (filename.equals("XML/actors63.xml")){
            parseActorXML();
        } else if (filename.equals("XML/casts124.xml")){
            parseCastXML();
        }
    }

    private void parseXmlFile(String filename) {
        // get the factory
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        try {
            // using factory get an instance of document builder
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();

            // parse using builder to get DOM representation of the XML file
            dom = documentBuilder.parse(filename);
        } catch (ParserConfigurationException | SAXException | IOException error) {
            error.printStackTrace();
        }
    }

    private void parseActorXML() {
        // get the document root Element
        Element documentElement = dom.getDocumentElement();

        // get a nodelist of star Elements, parse each into Utility.Star object
        NodeList actorsList = documentElement.getElementsByTagName("actor");
        for (int i = 0; i < actorsList.getLength(); i++) {
            Element element = (Element) actorsList.item(i); // get the star element
            Star star = parseStar(element); // get the Utility.Star object

            // only add to starName_starObject_map if the star has not existed in database
            if (existing_stars_map.containsKey(star.getName())){
                if (existing_stars_map.get(star.getName()) != star.getBirthYear()){
                    starName_starObject_map.put(star.getName(), star); // add it to list
                }
                else {
                    System.out.println("Error parsing star, this star has already existed in the Stars table --- star name: " + star.getName());
                }
            } else {
                starName_starObject_map.put(star.getName(), star); // add it to list
            }
        }
    }

    private void parseCastXML() {
        // get the document root Element
        Element documentElement = dom.getDocumentElement();

        NodeList films = documentElement.getElementsByTagName("filmc");
        for (int i = 0; i < films.getLength(); i++) {
            Element element = (Element) films.item(i);
            HashMap<String, HashSet<String>> stars_in_this_movie = parseStarInMovies(element);
            stars_in_movies.putAll(stars_in_this_movie);
        }
    }

    private Star parseStar(Element element) {
        // for each <actor> element get text or int values of stagename and dob
        String name = getTextValue(element, "stagename");
        int birthYear = getIntValue(element, "dob", name);

        // create a new Utility.Star with the value read from the xml nodes
        Star newStar = new Star("nm" + (++lastStarID), name);
        if (birthYear != -1) {
            newStar.setBirthYear(birthYear);
        }
        return newStar;
    }

    private HashMap<String, HashSet<String>> parseStarInMovies(Element element) {
        HashMap<String, HashSet<String>> stars_in_movie = new HashMap<>();
        HashSet<String> stars = new HashSet<>();
        String movieId = "";

        NodeList movies = element.getElementsByTagName("m");
        for (int i = 0; i < movies.getLength(); i++) {
            Element movieElement = (Element) movies.item(i);
            if (i == 0){
                movieId = getTextValue(movieElement, "f");

                // if movie ID has not exist in the movies table, ignore this movie (by returning an empty map)
                if (!existing_movies_list.contains(movieId)){
                    System.out.println("Error parsing movie ID for stars_in_movies, this ID does not exist in the Movies table --- tag <f> --- value: " + movieId);
                    return stars_in_movie;
                }
            }

            String actor_name = getTextValue(movieElement, "a");
            if (actor_name == null || actor_name.isEmpty() || actor_name.trim().isEmpty()){
                System.out.println("Error parsing actor name --- tag <a> --- value: (null) --- movie ID: " + movieId);
                continue; // skip this star
            }

            Star actor_object = starName_starObject_map.get(actor_name);
            if (actor_object == null){
                Star newStar = new Star("nm" + (++lastStarID), actor_name);
                starName_starObject_map.put(actor_name, newStar);
            }

            String actorId = starName_starObject_map.get(actor_name).getId();

            stars.add(actorId);
//            stars.add(actor_name);
        }
        stars_in_movie.put(movieId, stars);
        return stars_in_movie;
    }
    // ------------------------------------------------------------------------------------------------------------

    private String getTextValue(Element element, String tagName) {
        String textVal = null;
        NodeList nodeList = element.getElementsByTagName(tagName);
        if (nodeList.getLength() > 0) {
            // here we expect each <tagName> only presents once in <actor>
            // Note: some <tagName> has no innerHTML -> need to check if nodeList.item(0).getFirstChild() != null
            if (nodeList.item(0).getFirstChild() != null){
                textVal = (nodeList.item(0).getFirstChild().getNodeValue()).trim();
            } else {
                textVal = "";
            }
        }
        return textVal;
    }

    private int getIntValue(Element ele, String tagName, String starName) {
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
            System.out.println("Error parsing integer value for tag <" + tagName + "> --- value: " + textVal + " --- actor: " + starName);
            // NOTE: <actor> with blank or invalid <dob> will have textVal for dob as "", thus cause exception
            // those invalid dob will have intVal == -1 instead
            // A lot of error message will be printed out but it's ok
        } catch (Exception e) {
            // Handle other exceptions (e.g., unexpected XML structure)
            System.out.println("Unexpected error processing tag " + tagName + ": " + e.getMessage());
        }
        return intVal;
    }
    // ------------------------------------------------------------------------------------------------------------

    private void print_stars() {
        System.out.println("Total parsed " + starName_starObject_map.size() + " stars");
        for (String starName : starName_starObject_map.keySet()) {
            System.out.println("\t" + starName_starObject_map.get(starName).toString());
        }
        System.out.println("Total: " + starName_starObject_map.size());

    }

    private void print_starsInMovies() {
        System.out.println("Total parsed " + stars_in_movies.size() + " movies");
        for (HashMap.Entry<String, HashSet<String>> entry : stars_in_movies.entrySet()) {
            String movieId = entry.getKey();
            HashSet<String> stars = entry.getValue();
            System.out.println("\tMovie: " + movieId + ", stars: " + stars);
        }
    }
    // ------------------------------------------------------------------------------------------------------------

    private void check_existing_stars(){
        try {
            String loginUser = "mytestuser";
            String loginPasswd = "My6$Password";
            String loginUrl = "jdbc:mysql://localhost:3306/moviedb";

            Class.forName("com.mysql.jdbc.Driver").newInstance();
            Connection connection = DriverManager.getConnection(loginUrl, loginUser, loginPasswd);
            PreparedStatement statement = null;
            int[] iNoRows = null;
            String query = "SELECT * FROM " + STARS_TABLE;
            statement = connection.prepareStatement(query);
            ResultSet rs = statement.executeQuery(query);

            while (rs.next()){
                String stars_name = rs.getString("name");
                int birth_year = rs.getInt("birthYear");
                existing_stars_map.put(stars_name, birth_year);
            }

            try {
                if (statement!=null) statement.close();
                if(connection!=null) connection.close();
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
        catch (Exception e) {e.printStackTrace();}
    }

    private void check_existing_movies(){
        try {
            String loginUser = "mytestuser";
            String loginPasswd = "My6$Password";
            String loginUrl = "jdbc:mysql://localhost:3306/moviedb";

            Class.forName("com.mysql.jdbc.Driver").newInstance();
            Connection connection = DriverManager.getConnection(loginUrl, loginUser, loginPasswd);
            PreparedStatement statement = null;
            int[] iNoRows = null;
            String query = "SELECT * FROM " + MOVIES_TABLE;
            statement = connection.prepareStatement(query);
            ResultSet rs = statement.executeQuery(query);

            while (rs.next()){
                String movieId = rs.getString("id");
                existing_movies_list.add(movieId);
            }
            try {
                if (statement!=null) statement.close();
                if(connection!=null) connection.close();
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
        catch (Exception e) {e.printStackTrace();}
    }

    // ------------------------------------------------------------------------------------------------------------
    private void fillin_stars_in_movies_with_unknown_star() {
        // (((function not completed)))
        Star unknownStar = new Star("nm" + (++lastStarID), "Unknown");
        String unknownStarID = unknownStar.getId();
        int unknownStarYOB = unknownStar.getBirthYear();
        HashSet<String> unknownSet = new HashSet<>();
        unknownSet.add(unknownStarID);

        List<String> all_movie_ids = new ArrayList<>();
        try {
            String loginUser = "mytestuser";
            String loginPasswd = "My6$Password";
            String loginUrl = "jdbc:mysql://localhost:3306/moviedb";

            Class.forName("com.mysql.jdbc.Driver").newInstance();
            Connection connection = DriverManager.getConnection(loginUrl, loginUser, loginPasswd);
            PreparedStatement statement = null;
            String insertQuery = "INSERT INTO " + STARS_TABLE + "(id, name, birthYear) VALUES (?, ?, ?)";
            statement = connection.prepareStatement(insertQuery);
            statement.setString(1, unknownStarID);
            statement.setString(2, "Unknown");
            statement.setInt(3, unknownStarYOB);
            statement.executeUpdate();
            System.out.println("Add an Unknown star");

            String query = "SELECT id FROM " + MOVIES_TABLE;
            statement = connection.prepareStatement(query);
            ResultSet rs = statement.executeQuery();
            while(rs.next()){
                all_movie_ids.add(rs.getString("id"));
            }
            statement.close();
            connection.close();
        }
        catch (Exception e) {e.printStackTrace();}

        for (String movieID : all_movie_ids){
            if (!stars_in_movies.containsKey(movieID)){
                System.out.println("Movie " + movieID + " has no actor");
                stars_in_movies.put(movieID, unknownSet);
            }
        }
    }

    
    private void insert_into_stars(){
        try {
            String loginUser = "mytestuser";
            String loginPasswd = "My6$Password";
            String loginUrl = "jdbc:mysql://localhost:3306/moviedb";

            Class.forName("com.mysql.jdbc.Driver").newInstance();
            Connection connection = DriverManager.getConnection(loginUrl, loginUser, loginPasswd);
            PreparedStatement statement = null;
            int[] iNoRows = null;
            String query = "INSERT INTO " + STARS_TABLE + "(id, name, birthYear) VALUES (?, ?, ?)";

            try {
                connection.setAutoCommit(false);
                statement = connection.prepareStatement(query);
                System.out.println("Adding new stars");
                for (String starName : starName_starObject_map.keySet())
                {
                    Star s = starName_starObject_map.get(starName);
                    String id = s.getId();
                    String name = s.getName();
                    int birthYear = s.getBirthYear();
                    statement.setString(1, id);
                    statement.setString(2, name);
                    if (birthYear != 0){
                        statement.setInt(3, birthYear);
                    } else {
                        statement.setNull(3, java.sql.Types.INTEGER); // set value of birthYear to NULL if birthYear data from the xml file doesn't exist
                    }
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
            System.out.println("Inserting new stars completed, " + iNoRows.length + " rows affected");
        }
        catch (Exception e) {e.printStackTrace();}
    }

    private void insert_into_stars_in_movies(){
        try {
            String loginUser = "mytestuser";
            String loginPasswd = "My6$Password";
            String loginUrl = "jdbc:mysql://localhost:3306/moviedb";

            Class.forName("com.mysql.jdbc.Driver").newInstance();
            Connection connection = DriverManager.getConnection(loginUrl, loginUser, loginPasswd);
            PreparedStatement insertStatement = null;
            int[] rowsAffected = null;
            String query = "INSERT INTO " + STARS_IN_MOVIES_TABLE + "(starId, movieId) VALUES (?, ?)";

            System.out.println("Start inserting into " + STARS_IN_MOVIES_TABLE + " table");
            try {
                connection.setAutoCommit(false);
                insertStatement = connection.prepareStatement(query);

                for (HashMap.Entry<String, HashSet<String>> entry : stars_in_movies.entrySet()) {
                    String movieId = entry.getKey();
                    HashSet<String> stars = entry.getValue();
                    for (String starId : stars){
                        insertStatement.setString(1, starId);
                        insertStatement.setString(2, movieId);
                        insertStatement.addBatch();
                    }
                }
                rowsAffected = insertStatement.executeBatch(); // Each int in the array represents the update count for each command in the batch.
                connection.commit();
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                if (insertStatement!=null) insertStatement.close();
                if(connection!=null) connection.close();
            } catch(Exception e) {
                e.printStackTrace();
            }

            System.out.println("Updating stars_in_movies completed, " + rowsAffected.length + " rows affected");
        }
        catch (Exception e) {e.printStackTrace();}
    }
    // ------------------------------------------------------------------------------------------------------------


    public static void main(String[] args) {
        // connect to DB and get the current max ID (last ID) of star in the stars table
        String lastStarID_string = getLastStarID(); // format: "nm9423080"
        lastStarID = Integer.parseInt(lastStarID_string.substring(2)); // format: 9423080

        // create a parser instance
        StarDomParser domParser = new StarDomParser();

        // check existing data in table
        domParser.check_existing_stars();
        domParser.check_existing_movies();

        // start parsing xml
        domParser.runParser("XML/actors63.xml");
        domParser.runParser("XML/casts124.xml");
        //domParser.print_stars();
        //domParser.print_starsInMovies();

        // fill in stars_in_movies with unknown_star for movies that have no actor
        domParser.fillin_stars_in_movies_with_unknown_star();

        // insert stars into the stars table
        domParser.insert_into_stars();


        // insert into DB
        domParser.insert_into_stars_in_movies();

    }

}
