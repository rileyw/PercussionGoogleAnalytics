<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Insert title here</title>
</head>
<body>
<form action="configureGoogleAnalytics" method="POST">
        <p>
                <label for="client_id">Client ID:</label><input type="text" id="client_id" name="client_id" value=""/><br />
                <label for="client_secret">Client Secret:</label><input type="text" id="client_secret" name="client_secret" value=""/><br />
                <input type="hidden" name="sys_contentid" value="${content_id}" />
                <input type="submit" value="Continue."/>
        </p>
</form>
</body>
</html>