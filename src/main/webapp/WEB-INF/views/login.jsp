<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ include file="fragments/head.jspf" %>
<div class="auth"><div class="card">
  <h2><c:out value="${applicationScope.siteName}"/></h2>
  <p class="muted">Online Health Consultation Platform &mdash; sign in</p>
  <%@ include file="fragments/flash.jspf" %>
  <form method="post" action="${ctx}/login">
    <input type="hidden" name="_csrf" value="${csrf}">
    <label>Email<input type="email" name="email" required autofocus></label>
    <label>Password<input type="password" name="password" required></label>
    <button class="btn">Sign in</button>
  </form>
  <p class="muted">New patient? <a href="${ctx}/register">Create an account</a></p>
</div></div></body></html>
