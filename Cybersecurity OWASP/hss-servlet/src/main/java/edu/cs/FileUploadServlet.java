package edu.cs;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

@WebServlet("/FileUploadServlet")
@MultipartConfig(fileSizeThreshold=1024*1024*10, // 10 MB, sets the threshold where file larger than 10MB will be stored on disk instead of memory
               maxFileSize=1024*1024*50,      	// 50 MB, maximum allowed size for each file, bigger than this will throw error
               maxRequestSize=1024*1024*100)   	// 100 MB, maximum allowed size for the entire request, including form fields and files
public class FileUploadServlet extends HttpServlet {

  private static final long serialVersionUID = 205242440643911308L; // TODO - find out more, required since it extends Serializable class
	
  /**
   * Directory where uploaded files will be saved, its relative to
   * the web application directory.
   */
  private static final String UPLOAD_DIR = "uploads";
  
  protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
      // gets absolute path of the web application
      String applicationPath = request.getServletContext().getRealPath("");

      // constructs path of the directory to save uploaded file
      String uploadFilePath = applicationPath + File.separator + UPLOAD_DIR;
       
      // creates the save directory if it does not exists
      File fileSaveDir = new File(uploadFilePath);
      if (!fileSaveDir.exists()) {
          fileSaveDir.mkdirs();
      }
      System.out.println("Upload File Directory="+fileSaveDir.getAbsolutePath());
      
      String fileName = "";
      //Get all the parts from request and write it to the file on server
      for (Part part : request.getParts()) {
          fileName = getFileName(part);
          fileName = fileName.substring(fileName.lastIndexOf("\\") + 1);
          part.write(uploadFilePath + File.separator + fileName);
          System.out.println("File Name: " + fileName + " was written to " + uploadFilePath);
      }
    
    String message = "Result";
    String content = new Scanner(new File(uploadFilePath + File.separator + fileName)).useDelimiter("\\Z").next(); 
    
    String jdbcURL = "jdbc:mysql://localhost:3306/db_repo?allowMultiQueries=true";
    String dbUser = "db_user";
    String dbPassword = "1122";
    
    try(Connection connection = DriverManager.getConnection(jdbcURL, dbUser, dbPassword)){
    	String query = "INSERT INTO file_uploads (fileName, fileContent) VALUES ('" + fileName + "', '" + content + "')";
    	
    	try (Statement statement = connection.createStatement()) {
    		int insertCount = statement.executeUpdate(query);
    		if(insertCount > 0) {
    			System.out.println("File saved into the database successfully");
    		}
    	}
    } catch (Exception e) {
    	e.printStackTrace();
    }
    
    response.setContentType("text/html");
    response.getWriter().write(message + "<BR>" + content);
  }
  
  // Handle GET requests
  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
      // Your custom logic for processing GET requests
      String jdbcURL = "jdbc:mysql://localhost:3306/db_repo";
      String dbUser = "db_user";
      String dbPassword = "1122";

      List<FileRecord> fileRecords = new ArrayList<>();
      try (Connection connection = DriverManager.getConnection(jdbcURL, dbUser, dbPassword)) {
          String query = "SELECT id, fileName FROM file_uploads";
          try (Statement statement = connection.createStatement();
               ResultSet resultSet = statement.executeQuery(query)) {
              while (resultSet.next()) {
            	  Integer pid = resultSet.getInt("id");
                  String fileName = resultSet.getString("fileName");
                  fileRecords.add(new FileRecord(pid, fileName));
                  System.out.println("HERE IS FILE NAME" + fileName);
              }
          }
      } catch (Exception e) {
          e.printStackTrace();
      }

      // Set the records as a request attribute and forward to index.jsp
      request.setAttribute("fileRecords", fileRecords);
      request.getRequestDispatcher("/index.jsp").forward(request, response);
  }

  /**
   * Utility method to get file name from HTTP header content-disposition
   */
  private String getFileName(Part part) {
      String contentDisp = part.getHeader("content-disposition");
      System.out.println("content-disposition header= "+contentDisp);
      String[] tokens = contentDisp.split(";");
      for (String token : tokens) {
          if (token.trim().startsWith("filename")) {
              return token.substring(token.indexOf("=") + 2, token.length()-1);
          }
      }
      return "";
  }
}
