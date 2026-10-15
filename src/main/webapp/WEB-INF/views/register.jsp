<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ include file="fragments/head.jspf" %>
<div class="auth"><div class="card">
  <h2>Patient registration</h2>
  <%@ include file="fragments/flash.jspf" %>
  <c:choose>
  <c:when test="${registrationOpen}">
  <form method="post" action="${ctx}/register">
    <input type="hidden" name="_csrf" value="${csrf}">
    <label>Full name<input name="name" required maxlength="100"></label>
    <label>Email<input type="email" name="email" required></label>
    <label>Phone (optional)<input name="phone"></label>
    <label>Date of birth (optional)<input type="date" name="dob"></label>
    <label>Password<input type="password" name="password" required minlength="8"></label>
    <label>Confirm password<input type="password" name="confirm" required minlength="8"></label>
    <button class="btn">Register</button>
  </form>
  </c:when>
  <c:otherwise><p>Self-registration is currently disabled by the administrator.</p></c:otherwise>
  </c:choose>
  <p class="muted"><a href="${ctx}/login">Back to sign in</a></p>
</div></div></body></html>
