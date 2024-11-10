<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
    <title>File Content</title>
</head>
<body>
    <h1>File Content</h1>

    <%
        // Retrieve the file content from the request attribute
        String fileContent = (String) request.getAttribute("fileContent");
        if (fileContent != null && !fileContent.isEmpty()) {
    %>
        <pre><%= fileContent %></pre>
    <%
        } else {
    %>
        <p>No file content available.</p>
    <%
        }
    %>

    <br>
    <a href="<%= request.getContextPath() + "/FileUploadServlet" %>">Back to Main Page</a>
</body>
</html>
