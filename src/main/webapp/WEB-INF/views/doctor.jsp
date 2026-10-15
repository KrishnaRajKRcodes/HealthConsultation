<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ include file="fragments/head.jspf" %>
<c:set var="formAction" value="${ctx}/doctor"/>
<div class="layout">
<aside class="sidebar">

    <h2>
        <i class="fa-solid fa-heart-pulse"></i>
        <c:out value="${applicationScope.siteName}"/>
    </h2>

    <p class="muted">
        <i class="fa-solid fa-user-doctor"></i>
        <c:out value="${sessionScope.user.name}"/>
        <br>
        <i class="fa-solid fa-stethoscope"></i>
        <c:out value="${sessionScope.user.extra}"/>
    </p>

    <a class="${section eq 'schedule' ? 'active' : ''}"
       href="${ctx}/doctor?section=schedule">
        <i class="fa-solid fa-calendar-days"></i>
        Schedule Management
    </a>

    <a class="${section eq 'consultations' ? 'active' : ''}"
       href="${ctx}/doctor?section=consultations">
        <i class="fa-solid fa-user-doctor"></i>
        Consultations
    </a>

    <a class="${section eq 'records' ? 'active' : ''}"
       href="${ctx}/doctor?section=records">
        <i class="fa-solid fa-file-medical"></i>
        Patient Records
    </a>

    <a class="${section eq 'messages' ? 'active' : ''}"
       href="${ctx}/doctor?section=messages">
        <i class="fa-solid fa-comments"></i>
        Communication
    </a>

    <a href="${ctx}/logout">
        <i class="fa-solid fa-right-from-bracket"></i>
        Logout
    </a>

</aside>
<main>
<%@ include file="fragments/flash.jspf" %>

<c:if test="${section eq 'schedule'}">
  <h1>Schedule Management</h1>
  <div class="card"><h3>Add working period</h3>
    <form method="post" action="${ctx}/doctor" class="grid">
      <input type="hidden" name="_csrf" value="${csrf}"><input type="hidden" name="action" value="addSlot">
      <label>Day<select name="day">
        <option value="MONDAY">Monday</option><option value="TUESDAY">Tuesday</option><option value="WEDNESDAY">Wednesday</option>
        <option value="THURSDAY">Thursday</option><option value="FRIDAY">Friday</option><option value="SATURDAY">Saturday</option><option value="SUNDAY">Sunday</option></select></label>
      <label>From<input type="time" name="start" required></label>
      <label>To<input type="time" name="end" required></label>
      <button class="btn">Add</button>
    </form></div>
  <div class="card"><h3>My availability</h3><table>
    <tr><th>Day</th><th>From</th><th>To</th><th></th></tr>
    <c:forEach var="s" items="${slots}"><tr><td>${s.day}</td><td>${s.startTime}</td><td>${s.endTime}</td>
      <td><form method="post" action="${ctx}/doctor"><input type="hidden" name="_csrf" value="${csrf}"><input type="hidden" name="action" value="deleteSlot"><input type="hidden" name="id" value="${s.id}"><button class="btn danger small">Remove</button></form></td></tr></c:forEach>
    <c:if test="${empty slots}"><tr><td colspan="4" class="muted">No working hours set - patients cannot book you until you add some.</td></tr></c:if></table></div>
</c:if>

<c:if test="${section eq 'consultations'}">
  <h1>Consultations</h1>
  <c:forEach var="a" items="${appointments}">
    <c:set var="cons" value="${consultationMap[a.id]}"/>
    <div class="card">
      <div class="row between"><b><c:out value="${a.patientName}"/></b>
        <span>${a.date} ${a.time} <span class="badge badge-${a.statusName}">${a.statusName}</span></span></div>
      <p class="muted">Reason: <c:out value="${a.reason}" default="-"/></p>
      <c:if test="${a.statusName ne 'CANCELLED'}">
        <details ${a.statusName eq 'BOOKED' ? 'open' : ''}><summary>${empty cons ? 'Provide advice' : 'View / edit advice'}</summary>
          <form method="post" action="${ctx}/doctor">
            <input type="hidden" name="_csrf" value="${csrf}"><input type="hidden" name="action" value="saveConsultation"><input type="hidden" name="appointmentId" value="${a.id}">
            <label>Medical advice<textarea name="advice" rows="3" required><c:out value="${cons.advice}"/></textarea></label>
            <label>Prescription (optional)<textarea name="prescription" rows="2"><c:out value="${cons.prescription}"/></textarea></label>
            <button class="btn">Save &amp; complete</button>
          </form></details>
      </c:if>
    </div>
  </c:forEach>
  <c:if test="${empty appointments}"><div class="card muted">No consultations scheduled yet.</div></c:if>
</c:if>

<c:if test="${section eq 'records'}">
  <h1>Patient Records</h1>
  <div class="card"><form method="get" action="${ctx}/doctor" class="row">
    <input type="hidden" name="section" value="records">
    <select name="patientId" required><option value="">-- select patient --</option>
      <c:forEach var="p" items="${patients}"><option value="${p.id}" ${param.patientId eq p.id ? 'selected' : ''}><c:out value="${p.name}"/> (ID ${p.id})</option></c:forEach></select>
    <button class="btn">Open records</button></form></div>
  <c:if test="${not empty patient}">
    <div class="card"><h3><c:out value="${patient.name}"/></h3>
      <p class="muted">Email: <c:out value="${patient.email}"/> | Phone: <c:out value="${patient.phone}" default="-"/> | DOB: <c:out value="${patient.extra}" default="-"/></p></div>
    <div class="card"><h3>Health records</h3><table><tr><th>Date</th><th>Type</th><th>Details</th></tr>
      <c:forEach var="r" items="${records}"><tr><td>${r.recordDate}</td><td><c:out value="${r.recordType}"/></td><td><c:out value="${r.description}"/></td></tr></c:forEach>
      <c:if test="${empty records}"><tr><td colspan="3" class="muted">No records.</td></tr></c:if></table></div>
    <div class="card"><h3>Consultation history</h3>
      <c:forEach var="c" items="${history}"><div class="msg recv"><b>${c.appointmentDate}</b> - <c:out value="${c.doctorName}"/>
        <p><c:out value="${c.advice}"/></p><c:if test="${not empty c.prescription}"><p class="muted">Rx: <c:out value="${c.prescription}"/></p></c:if></div></c:forEach>
      <c:if test="${empty history}"><p class="muted">No consultations yet.</p></c:if></div>
  </c:if>
</c:if>

<c:if test="${section eq 'messages'}"><%@ include file="fragments/messages.jspf" %></c:if>
</main></div></body></html>
