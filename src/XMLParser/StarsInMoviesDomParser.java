package XMLParser;
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
import java.util.*;

/* cast124.xml format:
        <casts>
            <dirfilms>
                <dirid>AA</dirid>
                <is>Asquith</is>
                <castnote>Actors and notes.</castnote>
                <filmc>
                    <m>
                        <f>AA13</f>
                        <t>Pygmalion</t>
                        <a>Leslie Howard</a>
                        <p>Sci</p>
                        <r>smug professor</r>
                        <n>Higgins</n>
                        <awards><award>AAN</award></awards>
                    </m>
                    ...
                <filmc>
                ...
            </dirfilms>
            ...
        </casts>
* */


public class StarsInMoviesDomParser {

    private static final String STARS_TABLE = "stars_backup"; // may create a stars_backup table for testing
    private static final String STARS_IN_MOVIES_TABLE = "stars_in_movies_backup"; // may create a stars_in_movies_backup table for testing

    private DataSource dataSource;
    HashMap<String, HashSet<String>> stars_in_movies = new HashMap<>();
    Document dom;

    public void runParser() {
        parseXmlFile(); // parse the xml file and get the dom object
        parseDocument(); // get each employee element and create a Employee object
        printData(); // iterate through the list and print the data
    }

    private void parseXmlFile() {
        // get the factory
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        try {
            // using factory get an instance of document builder
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();

            // parse using builder to get DOM representation of the XML file
            dom = documentBuilder.parse("XML/casts124.xml");

        } catch (ParserConfigurationException | SAXException | IOException error) {
            error.printStackTrace();
        }
    }
    private void parseDocument() {
        // get the document root Element
        Element documentElement = dom.getDocumentElement();

        NodeList films = documentElement.getElementsByTagName("filmc");
//        for (int i = 0; i < films.getLength(); i++) {
        for (int i = 0; i < 50; i++) {
            Element element = (Element) films.item(i);
            HashMap<String, HashSet<String>> stars_in_this_movie = parseStar(element);
            stars_in_movies.putAll(stars_in_this_movie);
        }
    }

    private HashMap<String, HashSet<String>> parseStar(Element element) {
        HashMap<String, HashSet<String>> stars_in_movie = new HashMap<>();
        HashSet<String> stars = new HashSet<>();
        String movieId = "";

        NodeList movies = element.getElementsByTagName("m");
        for (int i = 0; i < movies.getLength(); i++) {
            Element movieElement = (Element) movies.item(i);
            if (i == 0){
                movieId = getTextValue(movieElement, "f");
            }
            String actor = getTextValue(movieElement, "a");

            if (actor == null || actor.isEmpty() || actor.trim().isEmpty()){
                System.out.println("Error parsing actor name --- tag <a> --- value: (null) --- movie ID: " + movieId);
                continue; // skip this star
            }
            stars.add(actor);
        }

        stars_in_movie.put(movieId, stars);
        return stars_in_movie;
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
        System.out.println("Total parsed " + stars_in_movies.size() + " movies");
        for (HashMap.Entry<String, HashSet<String>> entry : stars_in_movies.entrySet()) {
            String movieId = entry.getKey();
            HashSet<String> stars = entry.getValue();
            System.out.println("\tMovie: " + movieId + ", stars: " + stars);
        }
    }


    private void insertDataToBD(){
        try {
            String loginUser = "mytestuser";
            String loginPasswd = "My6$Password";
            String loginUrl = "jdbc:mysql://localhost:3306/moviedb";

            Class.forName("com.mysql.jdbc.Driver").newInstance();
            Connection connection = DriverManager.getConnection(loginUrl, loginUser, loginPasswd);
            PreparedStatement insertStatement = null;
            int[] rowsAffected = null;
            String query = "INSERT INTO " + STARS_IN_MOVIES_TABLE + "(starId, movieId) SELECT id, ? FROM " + STARS_TABLE + " WHERE name = ?";

            System.out.println("Start inserting into database");
            try {
                connection.setAutoCommit(false);
                insertStatement = connection.prepareStatement(query);

                for (HashMap.Entry<String, HashSet<String>> entry : stars_in_movies.entrySet()) {
                    String movieId = entry.getKey();
                    HashSet<String> stars = entry.getValue();
                    for (String star : stars){
                        insertStatement.setString(1, movieId);
                        insertStatement.setString(2, star);
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




    public static void main(String[] args) {
        // create a parser instance
        StarsInMoviesDomParser domParser = new StarsInMoviesDomParser();

        // start parsing xml
        domParser.runParser();

        // insert into DB
        domParser.insertDataToBD();
    }

}
