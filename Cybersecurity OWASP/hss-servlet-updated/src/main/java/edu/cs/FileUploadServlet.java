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

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

@WebServlet("/FileUploadServlet")
@MultipartConfig(fileSizeThreshold=1024*1024*10, // 10 MB, sets the threshold where file larger than 10MB will be stored on disk instead of memory
                 maxFileSize=255,    	// 255 Byte, maximum allowed size for each file, bigger than this will throw error to match TINYBLOB
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
    
    try {
        Context env = (Context) new InitialContext().lookup("java:comp/env");
        String jdbcURL = (String) env.lookup("DB_URL");
        String dbUser = (String) env.lookup("DB_USER");
        String dbPassword = (String) env.lookup("DB_PW");
        
        try (Connection connection = DriverManager.getConnection(jdbcURL, dbUser, dbPassword)) {
            String query = "INSERT INTO file_uploads (fileName, fileContent) VALUES (?, ?)";

            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setString(1, fileName);
                preparedStatement.setString(2, content);

                int insertCount = preparedStatement.executeUpdate();
                if (insertCount > 0) {
                    System.out.println("File saved into the database successfully");
                }
            }
        } catch (Exception e) {
        	response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        	response.getWriter().write("An unexpected error occurred. Please try again later.");
            e.printStackTrace();
        }
    } catch (NamingException e) {
    	response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    	response.getWriter().write("An unexpected error occurred. Please try again later.");
        e.printStackTrace();
    }
    
    response.getWriter().write("File Successfully Uploaded.");
  }
  
  // Handle GET requests
  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
      List<FileRecord> fileRecords = new ArrayList<>();
      try {
          Context env = (Context) new InitialContext().lookup("java:comp/env");
          String jdbcURL = (String) env.lookup("DB_URL");
          String dbUser = (String) env.lookup("DB_USER");
          String dbPassword = (String) env.lookup("DB_PW");

          try (Connection connection = DriverManager.getConnection(jdbcURL, dbUser, dbPassword)) {
        	    String query = "SELECT id, fileName FROM file_uploads";

        	    try (PreparedStatement preparedStatement = connection.prepareStatement(query);
        	         ResultSet resultSet = preparedStatement.executeQuery()) {

        	        while (resultSet.next()) {
        	            Integer pid = resultSet.getInt("id");
        	            String fileName = resultSet.getString("fileName");

        	            fileRecords.add(new FileRecord(pid, fileName));
        	        }
        	    }
        	} catch (Exception e) {
            	response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            	response.getWriter().write("An unexpected error occurred. Please try again later.");
        	    e.printStackTrace();
        	}
      } catch (NamingException e) {
    	  response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
	      response.getWriter().write("An unexpected error occurred. Please try again later.");
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
