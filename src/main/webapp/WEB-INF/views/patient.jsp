<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ include file="fragments/head.jspf" %>
<c:set var="formAction" value="${ctx}/patient"/>
<div class="layout">
<aside class="sidebar">

    <h2>
        <i class="fa-solid fa-heart-pulse"></i>
        <c:out value="${applicationScope.siteName}"/>
    </h2>

    <p class="muted">
        <i class="fa-solid fa-user"></i>
        Patient: <c:out value="${sessionScope.user.name}"/>
    </p>

    <a class="${section eq 'book' ? 'active' : ''}"
       href="${ctx}/patient?section=book">
        <i class="fa-solid fa-calendar-plus"></i>
        Consultation Booking
    </a>

    <a class="${section eq 'advice' ? 'active' : ''}"
       href="${ctx}/patient?section=advice">
        <i class="fa-solid fa-stethoscope"></i>
        Medical Advice
    </a>

    <a class="${section eq 'records' ? 'active' : ''}"
       href="${ctx}/patient?section=records">
        <i class="fa-solid fa-file-medical"></i>
        Health Records
    </a>

    <a class="${section eq 'messages' ? 'active' : ''}"
       href="${ctx}/patient?section=messages">
        <i class="fa-solid fa-comments"></i>
        Messages
    </a>

    <a class="${section eq 'profile' ? 'active' : ''}"
       href="${ctx}/patient?section=profile">
        <i class="fa-solid fa-user-gear"></i>
        Profile
    </a>

    <a href="${ctx}/logout">
        <i class="fa-solid fa-right-from-bracket"></i>
        Logout
    </a>

</aside>
<main>
<%@ include file="fragments/flash.jspf" %>

<c:if test="${section eq 'book'}">
  <h1>Consultation Booking</h1>
  <div class="card"><h3>1. Choose a doctor and date</h3>
    <form method="get" action="${ctx}/patient" class="grid">
      <input type="hidden" name="section" value="book">
      <label>Doctor<select name="doctorId" required><option value="">-- select --</option>
        <c:forEach var="d" items="${doctors}"><option value="${d.id}" ${selectedDoctorId eq d.id ? 'selected' : ''}><c:out value="${d.name}"/> - <c:out value="${d.extra}"/></option></c:forEach></select></label>
      <label>Date<input type="date" name="date" min="${today}" value="${selectedDate}" required></label>
      <button class="btn">Check availability</button>
    </form></div>

  <c:if test="${not empty selectedDoctor}">
    <div class="card"><h3><c:out value="${selectedDoctor.name}"/> - weekly hours</h3>
      <p class="muted"><c:forEach var="s" items="${doctorSchedule}">${s.day} ${s.startTime}-${s.endTime} &nbsp;|&nbsp; </c:forEach>
      <c:if test="${empty doctorSchedule}">This doctor has not published working hours yet.</c:if></p>
      <c:if test="${not empty selectedDate}">
        <h3>2. Pick a free time on ${selectedDate}</h3>
        <c:choose>
          <c:when test="${empty slots}"><p class="muted">No free slots on this date. Try another day.</p></c:when>
          <c:otherwise>
            <form method="post" action="${ctx}/patient" class="grid">
              <input type="hidden" name="_csrf" value="${csrf}"><input type="hidden" name="action" value="bookAppointment">
              <input type="hidden" name="doctorId" value="${selectedDoctorId}"><input type="hidden" name="date" value="${selectedDate}">
              <label>Time<select name="time"><c:forEach var="t" items="${slots}"><option value="${t}">${t}</option></c:forEach></select></label>
              <label>Reason for visit<input name="reason" maxlength="255"></label>
              <button class="btn">Confirm booking</button>
            </form>
          </c:otherwise></c:choose>
      </c:if></div>
  </c:if>

  <div class="card"><h3>My appointments</h3><table>
    <tr><th>Date</th><th>Time</th><th>Doctor</th><th>Status</th><th></th></tr>
    <c:forEach var="a" items="${appointments}"><tr><td>${a.date}</td><td>${a.time}</td><td><c:out value="${a.doctorName}"/></td>
      <td><span class="badge badge-${a.statusName}">${a.statusName}</span></td>
      <td><c:if test="${a.statusName eq 'BOOKED'}"><form method="post" action="${ctx}/patient" onsubmit="return confirm('Cancel this appointment?')">
        <input type="hidden" name="_csrf" value="${csrf}"><input type="hidden" name="action" value="cancelAppointment"><input type="hidden" name="id" value="${a.id}"><button class="btn danger small">Cancel</button></form></c:if></td></tr></c:forEach>
    <c:if test="${empty appointments}"><tr><td colspan="5" class="muted">No appointments yet.</td></tr></c:if></table></div>
</c:if>

<c:if test="${section eq 'advice'}">
  <h1>Medical Advice</h1>
  <c:forEach var="c" items="${consultations}">
    <div class="card"><div class="row between"><b><c:out value="${c.doctorName}"/></b><span class="muted">${c.appointmentDate}</span></div>
      <h4>Advice</h4><p><c:out value="${c.advice}"/></p>
      <c:if test="${not empty c.prescription}"><h4>Prescription</h4><p><c:out value="${c.prescription}"/></p></c:if></div>
  </c:forEach>
  <c:if test="${empty consultations}"><div class="card muted">You have not received any medical advice yet.</div></c:if>
</c:if>

<c:if test="${section eq 'records'}">
  <h1>Health Records</h1>
  <div class="card"><h3>Add record</h3>
    <form method="post" action="${ctx}/patient" class="grid">
      <input type="hidden" name="_csrf" value="${csrf}"><input type="hidden" name="action" value="addRecord">
      <label>Type<select name="recordType"><option>Diagnosis</option><option>Allergy</option><option>Medication</option><option>Lab result</option><option>Vaccination</option><option>Surgery</option><option>Other</option></select></label>
      <label>Date<input type="date" name="recordDate" required></label>
      <label>Description<textarea name="description" rows="2" required></textarea></label>
      <button class="btn">Add</button>
    </form></div>
  <div class="card"><table><tr><th>Date</th><th>Type</th><th>Description</th><th></th></tr>
    <c:forEach var="r" items="${records}"><tr><td>${r.recordDate}</td><td><c:out value="${r.recordType}"/></td><td><c:out value="${r.description}"/></td>
      <td><details><summary>Edit</summary>
        <form method="post" action="${ctx}/patient"><input type="hidden" name="_csrf" value="${csrf}"><input type="hidden" name="action" value="updateRecord"><input type="hidden" name="id" value="${r.id}">
          <label>Type<input name="recordType" value="<c:out value='${r.recordType}'/>" required></label>
          <label>Date<input type="date" name="recordDate" value="${r.recordDate}" required></label>
          <label>Description<textarea name="description" rows="2" required><c:out value="${r.description}"/></textarea></label>
          <button class="btn small">Save</button></form></details>
        <form method="post" action="${ctx}/patient" onsubmit="return confirm('Delete record?')"><input type="hidden" name="_csrf" value="${csrf}"><input type="hidden" name="action" value="deleteRecord"><input type="hidden" name="id" value="${r.id}"><button class="btn danger small">Delete</button></form></td></tr></c:forEach>
    <c:if test="${empty records}"><tr><td colspan="4" class="muted">No records yet.</td></tr></c:if></table></div>
</c:if>

<c:if test="${section eq 'profile'}">
  <h1>Profile Management</h1>
  <div class="card"><form method="post" action="${ctx}/patient">
    <input type="hidden" name="_csrf" value="${csrf}"><input type="hidden" name="action" value="updateProfile">
    <label>Name<input name="name" value="<c:out value='${profile.name}'/>" required></label>
    <label>Email (cannot be changed)<input value="<c:out value='${profile.email}'/>" disabled></label>
    <label>Phone<input name="phone" value="<c:out value='${profile.phone}'/>"></label>
    <label>Date of birth<input type="date" name="dob" value="${profile.extra}"></label>
    <label>New password (leave blank to keep current)<input type="password" name="newPassword" minlength="8"></label>
    <button class="btn">Update profile</button></form></div>
</c:if>

<c:if test="${section eq 'messages'}"><%@ include file="fragments/messages.jspf" %></c:if>
</main></div></body></html>
