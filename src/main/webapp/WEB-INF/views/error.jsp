<%@ page contentType="text/html;charset=UTF-8" language="java" isErrorPage="true" %>
<%@ include file="fragments/head.jspf" %>
<div class="auth"><div class="card">
  <h2>Something went wrong</h2>
  <p class="muted">The page you requested could not be shown (<c:out value="${pageContext.errorData.statusCode}"/>).
     You may not have permission, or the page does not exist.</p>
  <a class="btn" href="${ctx}/login">Back to home</a>
</div></div></body></html>
