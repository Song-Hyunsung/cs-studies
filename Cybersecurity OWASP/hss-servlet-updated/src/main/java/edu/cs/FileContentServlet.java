package edu.cs;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;

import org.apache.commons.text.StringEscapeUtils;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Base64;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/FileContentServlet/*")
public class FileContentServlet extends HttpServlet {
	private static final long serialVersionUID = 205242440643912616L;

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String pathInfo = request.getPathInfo();
		
		if(pathInfo != null && !pathInfo.isEmpty()) {
			String fileId = pathInfo.substring(1);
			
	        try {
	            Context env = (Context) new InitialContext().lookup("java:comp/env");
	            String jdbcURL = (String) env.lookup("DB_URL");
	            String dbUser = (String) env.lookup("DB_USER");
	            String dbPassword = (String) env.lookup("DB_PW");

			      try (Connection connection = DriverManager.getConnection(jdbcURL, dbUser, dbPassword)) {
			    	    String query = "SELECT fileContent FROM file_uploads WHERE id = ?";

			    	    try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
			    	        preparedStatement.setString(1, fileId);

			    	        try (ResultSet resultSet = preparedStatement.executeQuery()) {
			    	            if (resultSet.next()) {
			    	                // Retrieve the file content (TINYBLOB in byte array format)
			    	                byte[] fileContentBytes = resultSet.getBytes("fileContent");
			    	                String fileContent = new String(fileContentBytes, StandardCharsets.UTF_8);
			    	                fileContent = StringEscapeUtils.escapeHtml4(fileContent);
			    	                request.setAttribute("fileContent", fileContent);

			    	                // Generate a nonce for Content Security Policy
			    	                SecureRandom secureRandom = new SecureRandom();
			    	                byte[] nonceBytes = new byte[16];
			    	                secureRandom.nextBytes(nonceBytes);
			    	                String nonce = Base64.getEncoder().encodeToString(nonceBytes);
			    	                response.setHeader("Content-Security-Policy", "default-src 'self'; script-src 'self' 'nonce-" + nonce + "'; style-src 'self';");

			    	                RequestDispatcher dispatcher = request.getRequestDispatcher("/fileContent.jsp");
			    	                dispatcher.forward(request, response);
			    	            }
			    	        }
			    	    }
			    	} catch (Exception e) {
			        	response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			        	response.getWriter().write("An unexpected error occurred. Please try again later.");
			    	    e.printStackTrace();
			    	}
	        } catch (NamingException e) {
	            e.printStackTrace();
	        }
		} else {
			RequestDispatcher dispatcher = request.getRequestDispatcher("/fileContent.jsp");
			dispatcher.forward(request, response);
		}
	}
}
