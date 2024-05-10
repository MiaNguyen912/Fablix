package LoginServlet;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import javax.net.ssl.HttpsURLConnection;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;

public class RecaptchaVerifyUtils {

    public static final String SITE_VERIFY_URL = "https://www.google.com/recaptcha/api/siteverify";

    public static void verify(String gRecaptchaResponse) throws Exception {
        System.out.println("Starting verify function");
        URL verifyUrl = new URL(SITE_VERIFY_URL);



        // Open Connection to URL
        HttpsURLConnection conn = (HttpsURLConnection) verifyUrl.openConnection();
        System.out.println("Connection made");

        // Add Request Header
        conn.setRequestMethod("POST");
        System.out.println("1");
        conn.setRequestProperty("Utility.User-Agent", "Mozilla/5.0");
        System.out.println("2");
        conn.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
        System.out.println("3");


        // Data will be sent to the server.
        String postParams = "secret=" + RecaptchaConstants.SECRET_KEY + "&response=" + gRecaptchaResponse;


        System.out.println("Sending Request");
        // Send Request
        conn.setDoOutput(true);



        System.out.println("Getting output stream of connection");
        // Get the output stream of Connection
        // Write data in this stream, which means to send data to Server.
        OutputStream outStream = conn.getOutputStream();
        System.out.println("1");
        outStream.write(postParams.getBytes());
        System.out.println("2");

        outStream.flush();
        System.out.println("3");
        outStream.close();
        System.out.println("4");

        System.out.println("Getting input stream of connection");
        // Get the InputStream from Connection to read data sent from the server.
        InputStream inputStream = conn.getInputStream();
        System.out.println("1");
        InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
        System.out.println("2");

        JsonObject jsonObject = new Gson().fromJson(inputStreamReader, JsonObject.class);
        System.out.println("5");

        inputStreamReader.close();
        System.out.println("6");

        if (jsonObject.get("success").getAsBoolean()) {
            // verification succeed
            return;
        }

        throw new Exception("recaptcha verification failed: response is " + jsonObject);
    }

}
