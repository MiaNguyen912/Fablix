
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.TreeMap;


@WebServlet(name = "MetadataServlet", urlPatterns = "/fablix/_dashboard/loggedin/api/metadata")
public class MetadataServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    // Create a dataSource which registered in web.
    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        response.setContentType("application/json"); // Response mime type

        // Output stream to STDOUT
        PrintWriter out = response.getWriter();

        // Get a connection from dataSource and let resource manager close the connection after usage.
        try (Connection conn = dataSource.getConnection()) {

            // note: the line below is replaced by: PreparedStatement statement = conn.prepareStatement(query);
            //Statement statement = conn.createStatement(); // Declare our statement

            String query = "SHOW TABLES;";
            PreparedStatement statement = conn.prepareStatement(query);
            ResultSet rs = statement.executeQuery(query);

            JsonObject dbMetadata = new JsonObject();

            ArrayList<String> tables = new ArrayList<>();
            while (rs.next()) {
                tables.add(rs.getString("Tables_in_moviedb"));
            }

            rs.close();

            for (String table : tables){
                statement = conn.prepareStatement("DESCRIBE " + table);
                rs = statement.executeQuery();
                JsonObject tableMetadata = new JsonObject();

                while (rs.next()){
                    String field = rs.getString("Field");
                    String type = rs.getString("Type");
                    tableMetadata.addProperty(field, type);
                }
                dbMetadata.add(table, tableMetadata);
                rs.close();
            }

            statement.close();



            rs.close();

            // Log to localhost log
            request.getServletContext().log("getting " + dbMetadata.size() + " results");

            // Write JSON string to output
            out.write(dbMetadata.toString());
            // Set response status to 200 (OK)
            response.setStatus(200);

        } catch (Exception e) {

            // Write error message JSON object to output
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());
            out.write(jsonObject.toString());

            // Set response status to 500 (Internal Server Error)
            response.setStatus(500);
        } finally {
            out.close();
        }

        // Always remember to close db connection after usage. Here it's done by try-with-resources

    }
}
