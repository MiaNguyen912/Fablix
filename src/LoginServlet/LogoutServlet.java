package LoginServlet;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;


@WebServlet(name = "LoginServlet.LogoutServlet", urlPatterns = "/api/logout") // LoginServlet.LoginServlet handles GET request sent to /api/logout
public class LogoutServlet extends HttpServlet {

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
//        try{
//            HttpSession session = request.getSession();
//
//            session.invalidate();
//            JsonObject responseJsonObject = new JsonObject();
//            responseJsonObject.addProperty("status", "success");
//            response.setStatus(200); // Set response status to 200 (OK)
//            response.getWriter().write(responseJsonObject.toString()); // write out response object
//        } catch (Exception e) {
//            // Write error message JSON object to output
//            JsonObject jsonObject = new JsonObject();
//            jsonObject.addProperty("errorMessage", e.getMessage());
//            request.getServletContext().log("Error:", e);  // Log error to localhost log
//            response.getWriter().write(jsonObject.toString()); // write out response object
//            response.setStatus(500);  // Set response status to 500 (Internal Server Error)
//        }

    }
}
