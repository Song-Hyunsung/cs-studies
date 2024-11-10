<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.List, java.util.ArrayList" %>
<%@ page import="edu.cs.FileRecord" %>

<!DOCTYPE html>
<html>
<head>
    <title>File Upload</title>
</head>
<body>
    <!-- File Upload Form -->
    <form action="FileUploadServlet" method="post" enctype="multipart/form-data">
        Select File to Upload: <input type="file" name="fileName">
        <br>
        <input type="submit" value="Upload">
    </form>

    <h2>Uploaded Files</h2>
    <table border="1">
        <tr>
        	<th>File Id</th>
            <th>File Name</th>
        </tr>

        <%
            // Get the file records from the request attribute
            List<FileRecord> fileRecords = null;

            Object attribute = request.getAttribute("fileRecords");
            if (attribute instanceof List<?>) {
                fileRecords = (List<FileRecord>) attribute;
            }

            if (fileRecords != null && !fileRecords.isEmpty()) {
                for (FileRecord record : fileRecords) {
        %>
            <tr>
                <td><%= record.getPid() %></td>
                <td>
                    <a href="FileContentServlet/<%= record.getPid() %>">
                        <%= record.getFileName() %>
                    </a>
                </td>
            </tr>
        <%
                }
            } else {
        %>
            <tr>
                <td colspan="2">No files uploaded yet.</td>
            </tr>
        <%
            }
        %>

    </table>
</body>
</html>
