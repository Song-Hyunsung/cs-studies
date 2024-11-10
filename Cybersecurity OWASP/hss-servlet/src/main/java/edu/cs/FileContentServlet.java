package edu.cs;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
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
			
		      String jdbcURL = "jdbc:mysql://localhost:3306/db_repo";
		      String dbUser = "db_user";
		      String dbPassword = "1122";

		      try (Connection connection = DriverManager.getConnection(jdbcURL, dbUser, dbPassword)) {
		          String query = "SELECT fileContent FROM file_uploads WHERE id =" + fileId;
		          try (Statement statement = connection.createStatement();
		               ResultSet resultSet = statement.executeQuery(query)) {
		              if(resultSet.next()) {
		            	  byte[] fileContentBytes = resultSet.getBytes("fileContent");
		            	  String fileContent = new String(fileContentBytes, StandardCharsets.UTF_8);
		            	  
		            	  request.setAttribute("fileContent", fileContent);
		            	  RequestDispatcher dispatcher = request.getRequestDispatcher("/fileContent.jsp");
		            	  dispatcher.forward(request, response);
		              }
		          }
		      } catch (Exception e) {
		          e.printStackTrace();
		      }
		}
		
		RequestDispatcher dispatcher = request.getRequestDispatcher("/fileContent.jsp");
		dispatcher.forward(request, response);
	}
}
