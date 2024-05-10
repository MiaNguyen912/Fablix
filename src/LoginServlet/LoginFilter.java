package LoginServlet;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Servlet Filter implementation class LoginServlet.LoginFilter
 */
@WebFilter(filterName = "LoginServlet.LoginFilter", urlPatterns = "/*")
public class LoginFilter implements Filter {
    private final ArrayList<String> allowedURIs = new ArrayList<>();

    /**
     * @see Filter#doFilter(ServletRequest, ServletResponse, FilterChain)
     */
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        System.out.println("Login Filtering");
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        System.out.println("LoginServlet.LoginFilter: " + httpRequest.getRequestURI());

        // Check if this URL is allowed to access without logging in
        if (this.isUrlAllowedWithoutLogin(httpRequest.getRequestURI())) {
            // Keep default action: pass along the filter chain
            System.out.println("LoginServlet.LoginFilter: Passed loginfilter");
            chain.doFilter(request, response);
            return;
        }

        // Redirect to login page if the "user" attribute doesn't exist in session
        System.out.println(httpRequest.getSession().getAttribute("user"));
        if (httpRequest.getSession().getAttribute("user") == null) {
            System.out.println("LoginServlet.LoginFilter: Redirecting back to login user doesn't exist");
            httpResponse.sendRedirect(httpRequest.getContextPath() + "/login.html");

        } else {
            System.out.println("LoginServlet.LoginFilter: Passed loginfilter");
            chain.doFilter(request, response);
        }
    }

    private boolean isUrlAllowedWithoutLogin(String requestURI) {
        /*
         Setup your own rules here to allow accessing some resources without logging in
         Always allow your own login related requests(html, js, servlet, etc..)
         You might also want to allow some CSS files, etc..
         */


        if (requestURI.contains("assets/")){
            return true;
        }
        //        if (requestURI.endsWith(".css") || requestURI.endsWith(".png")) {
        //            return true; // Allow access to CSS and png files without login
        //        }

        // allow all files/url specified in allowedURIs
        return allowedURIs.stream().anyMatch(requestURI.toLowerCase()::endsWith);
    }

    public void init(FilterConfig fConfig) {
        allowedURIs.add("login.html"); // login page
        allowedURIs.add("login.js");
        allowedURIs.add("api/login");
        allowedURIs.add("index.html"); // home page
        allowedURIs.add("index.js");
        allowedURIs.add("api/20movies");

        allowedURIs.add("_dashboard");
        allowedURIs.add("_dashboard/");
        allowedURIs.add("_dashboard/login.html"); // login page
        allowedURIs.add("_dashboard/login.js");
        allowedURIs.add("_dashboard/api/staff-login");
    }

    public void destroy() {
        // ignored.
    }

}
