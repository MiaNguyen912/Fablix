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
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;

public class StarDomParser {
    private DataSource dataSource;
    static int lastStarID;
    List<Star> stars = new ArrayList<>();
    Document dom;


    public static String getLastStarID() {
        String lastStarID_string = "";
        try {
            try(Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/moviedb", "mytestuser", "My6$Password")){;
            String query = "select max(id) from stars;";
                PreparedStatement statement = conn.prepareStatement(query);
                ResultSet rs = statement.executeQuery();
                if (rs.next()) {
                    lastStarID_string = rs.getString(1);
//                    System.out.println(lastStarID_string);
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

    public void runParser() {
        parseXmlFile(); // parse the xml file and get the dom object
        parseDocument(); // get each employee element and create a Employee object
//        printData(); // iterate through the list and print the data
    }

    private void parseXmlFile() {
        // get the factory
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        try {
            // using factory get an instance of document builder
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();

            // parse using builder to get DOM representation of the XML file
            dom = documentBuilder.parse("XML/actors63.xml");
        } catch (ParserConfigurationException | SAXException | IOException error) {
            error.printStackTrace();
        }
    }

    private void parseDocument() {
        // get the document root Element
        Element documentElement = dom.getDocumentElement();

        // get a nodelist of star Elements, parse each into Utility.Star object
        NodeList actorsList = documentElement.getElementsByTagName("actor");
        for (int i = 0; i < actorsList.getLength(); i++) {
            Element element = (Element) actorsList.item(i); // get the star element
            Star star = parseStar(element); // get the Utility.Star object
            stars.add(star); // add it to list
        }
    }

    /**
     * It takes an employee Element, reads the values in, creates
     * an Employee object for return
     */
    private Star parseStar(Element element) {

        // for each <actor> element get text or int values of stagename and dob
        String name = getTextValue(element, "stagename");
//        System.out.println(name);
        int birthYear = getIntValue(element, "dob");
//        System.out.println(birthYear);

        // create a new Utility.Star with the value read from the xml nodes
        Star newStar = new Star("nm" + (lastStarID++), name);
        if (birthYear != -1) {
            newStar.setBirthYear(birthYear);
        }
        return newStar;
    }

    /**
     * It takes an XML element and the tag name, look for the tag and get the text content
     * i.e for <Utility.Star><Name>John</Name></Utility.Star> xml snippet if the Element points to Utility.Star node and tagName is name it will return John
     */
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

    /**
     * Calls getTextValue and returns a int value
     */
    private int getIntValue(Element ele, String tagName) {
        int intVal = -1;
        try {
            String textVal = getTextValue(ele, tagName);
            textVal = textVal.replaceAll("[^a-zA-Z0-9]", ""); // remove non-alphanumeric characters
            intVal = Integer.parseInt(textVal);
        } catch (NumberFormatException e) {
            // Handle the case where the text cannot be parsed as an integer
            System.out.println("Error parsing integer value for tag " + tagName + ": " + e.getMessage());
            // NOTE: <actor> with blank or invalid <dob> will have textVal for dob as "", thus cause exception
            // those invalid dob will have intVal == -1 instead
            // A lot of error message will be printed out but it's ok
        } catch (Exception e) {
            // Handle other exceptions (e.g., unexpected XML structure)
            System.out.println("Unexpected error processing tag " + tagName + ": " + e.getMessage());
        }
        return intVal;
    }
    private void printData() {
        System.out.println("Total parsed " + stars.size() + " stars");
        for (Star s : stars) {
            System.out.println("\t" + s.toString());
        }
    }

    private void insertDataToBD(){

    }




    public static void main(String[] args) {
        // connect to DB and get the current max ID (last ID) of star in the stars table
        String lastStarID_string = getLastStarID(); // format: "nm9423080"
        lastStarID = Integer.parseInt(lastStarID_string.substring(2)); // format: 9423080

        // create a parser instance
        StarDomParser domParser = new StarDomParser();

        // start parsing xml
        domParser.runParser();

        // insert data into database
        domParser.insertDataToBD();
    }

}
