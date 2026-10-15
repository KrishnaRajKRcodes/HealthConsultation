<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ include file="fragments/head.jspf" %>
<div class="layout">
<aside class="sidebar">

    <h2><i class="fa-solid fa-heart-pulse"></i>
        <c:out value="${applicationScope.siteName}"/>
    </h2>

    <p class="muted">
        <i class="fa-solid fa-user-shield"></i>
        Admin: <c:out value="${sessionScope.user.name}"/>
    </p>

    <a class="${section eq 'analytics' ? 'active' : ''}"
       href="${ctx}/admin?section=analytics">
        <i class="fa-solid fa-chart-line"></i>
        System Analytics
    </a>

    <a class="${section eq 'users' ? 'active' : ''}"
       href="${ctx}/admin?section=users">
        <i class="fa-solid fa-users"></i>
        User Management
    </a>

    <a class="${section eq 'appointments' ? 'active' : ''}"
       href="${ctx}/admin?section=appointments">
        <i class="fa-solid fa-calendar-check"></i>
        Appointment Management
    </a>

    <a class="${section eq 'settings' ? 'active' : ''}"
       href="${ctx}/admin?section=settings">
        <i class="fa-solid fa-gear"></i>
        System Settings
    </a>

    <a href="${ctx}/logout">
        <i class="fa-solid fa-right-from-bracket"></i>
        Logout
    </a>

</aside>
<main>
<%@ include file="fragments/flash.jspf" %>

<c:if test="${section eq 'analytics'}">
  <h1>System Analytics</h1>
  <div class="stats">
    <div class="stat"><b>${totalUsers}</b>Total users</div>
    <div class="stat"><b>${totalAppointments}</b>Total appointments</div>
    <c:forEach var="e" items="${usersByRole}"><div class="stat"><b>${e.value}</b>${e.key}S</div></c:forEach>
  </div>
  <div class="card"><h3>Appointments by status</h3>
    <c:forEach var="e" items="${apptByStatus}">
      <div class="bar"><span>${e.key}</span><div class="fill" style="width:${totalAppointments == 0 ? 0 : e.value * 100 / totalAppointments}%"></div><i>${e.value}</i></div>
    </c:forEach></div>
  <div class="card"><h3>Appointments per doctor</h3>
    <table><tr><th>Doctor</th><th>Appointments</th></tr>
    <c:forEach var="e" items="${apptPerDoctor}"><tr><td><c:out value="${e.key}"/></td><td>${e.value}</td></tr></c:forEach>
    <c:if test="${empty apptPerDoctor}"><tr><td colspan="2" class="muted">No data yet.</td></tr></c:if></table></div>

      <!-- Analytics Charts -->
  <div class="analytics-charts">

    <div class="card">
      <h3><i class="fa-solid fa-chart-pie"></i> Users by Role</h3>
      <canvas id="usersChart"></canvas>
    </div>

    <div class="card">
      <h3><i class="fa-solid fa-chart-column"></i> Appointments by Status</h3>
      <canvas id="appointmentsChart"></canvas>
    </div>

  </div>

  <!-- Chart data from the application -->
  <div id="usersChartData" hidden>
    <c:forEach var="e" items="${usersByRole}">
      <span data-label="<c:out value='${e.key}'/>"
            data-value="${e.value}"></span>
    </c:forEach>
  </div>

  <div id="appointmentsChartData" hidden>
    <c:forEach var="e" items="${apptByStatus}">
      <span data-label="<c:out value='${e.key}'/>"
            data-value="${e.value}"></span>
    </c:forEach>
  </div>
</c:if>

<c:if test="${section eq 'users'}">
  <h1>User Management</h1>
  <div class="card"><h3>Add user</h3>
    <form method="post" action="${ctx}/admin" class="grid">
      <input type="hidden" name="_csrf" value="${csrf}"><input type="hidden" name="action" value="createUser">
      <label>Name<input name="name" required></label>
      <label>Email<input type="email" name="email" required></label>
      <label>Temporary password<input type="password" name="password" required minlength="8"></label>
      <label>Role<select name="role"><option value="PATIENT">Patient</option><option value="DOCTOR">Doctor</option><option value="ADMIN">Admin</option></select></label>
      <label>Phone<input name="phone"></label>
      <label>Specialization (doctor) / DOB yyyy-mm-dd (patient)<input name="extra"></label>
      <button class="btn">Create user</button>
    </form></div>
  <div class="card"><table>
    <tr><th>ID</th><th>Name</th><th>Email</th><th>Role</th><th>Status</th><th>Actions</th></tr>
    <c:forEach var="u" items="${users}">
      <tr>
        <td>${u.id}</td><td><c:out value="${u.name}"/></td><td><c:out value="${u.email}"/></td><td>${u.roleLabel}</td>
        <td><span class="badge ${u.active ? 'badge-COMPLETED' : 'badge-CANCELLED'}">${u.active ? 'Active' : 'Disabled'}</span></td>
        <td>
          <details><summary>Edit</summary>
            <form method="post" action="${ctx}/admin" class="grid">
              <input type="hidden" name="_csrf" value="${csrf}"><input type="hidden" name="action" value="updateUser"><input type="hidden" name="id" value="${u.id}">
              <label>Name<input name="name" value="<c:out value='${u.name}'/>" required></label>
              <label>Email<input type="email" name="email" value="<c:out value='${u.email}'/>" required></label>
              <label>Role<select name="role">
                <option value="PATIENT" ${u.roleName eq 'PATIENT' ? 'selected' : ''}>Patient</option>
                <option value="DOCTOR" ${u.roleName eq 'DOCTOR' ? 'selected' : ''}>Doctor</option>
                <option value="ADMIN" ${u.roleName eq 'ADMIN' ? 'selected' : ''}>Admin</option></select></label>
              <label>Phone<input name="phone" value="<c:out value='${u.phone}'/>"></label>
              <label>Specialization / DOB<input name="extra" value="<c:out value='${u.extra}'/>"></label>
              <label class="check"><input type="checkbox" name="active" ${u.active ? 'checked' : ''}> Active</label>
              <button class="btn">Save</button>
            </form>
          </details>
          <form method="post" action="${ctx}/admin" onsubmit="return confirm('Delete this user and all their data?')">
            <input type="hidden" name="_csrf" value="${csrf}"><input type="hidden" name="action" value="deleteUser"><input type="hidden" name="id" value="${u.id}">
            <button class="btn danger small">Delete</button>
          </form>
        </td>
      </tr>
    </c:forEach></table></div>
</c:if>

<c:if test="${section eq 'appointments'}">
  <h1>Appointment Management</h1>
  <div class="card"><h3>Schedule an appointment</h3>
    <form method="post" action="${ctx}/admin" class="grid">
      <input type="hidden" name="_csrf" value="${csrf}"><input type="hidden" name="action" value="bookAppointment">
      <label>Patient<select name="patientId" required><c:forEach var="p" items="${patients}"><option value="${p.id}"><c:out value="${p.name}"/></option></c:forEach></select></label>
      <label>Doctor<select name="doctorId" required><c:forEach var="d" items="${doctors}"><option value="${d.id}"><c:out value="${d.name}"/> - <c:out value="${d.extra}"/></option></c:forEach></select></label>
      <label>Date<input type="date" name="date" required></label>
      <label>Time<input type="time" name="time" required></label>
      <label>Reason<input name="reason" maxlength="255"></label>
      <button class="btn">Schedule</button>
    </form>
    <p class="muted">The time must fall inside the doctor's working hours and must be free.</p></div>
  <div class="card"><table>
    <tr><th>#</th><th>Date</th><th>Time</th><th>Patient</th><th>Doctor</th><th>Status</th><th>Actions</th></tr>
    <c:forEach var="a" items="${appointments}">
      <tr><td>${a.id}</td><td>${a.date}</td><td>${a.time}</td><td><c:out value="${a.patientName}"/></td><td><c:out value="${a.doctorName}"/></td>
        <td><span class="badge badge-${a.statusName}">${a.statusName}</span></td>
        <td class="row">
          <form method="post" action="${ctx}/admin"><input type="hidden" name="_csrf" value="${csrf}"><input type="hidden" name="action" value="setStatus"><input type="hidden" name="id" value="${a.id}">
            <select name="status" onchange="this.form.submit()">
              <option ${a.statusName eq 'BOOKED' ? 'selected' : ''}>BOOKED</option>
              <option ${a.statusName eq 'COMPLETED' ? 'selected' : ''}>COMPLETED</option>
              <option ${a.statusName eq 'CANCELLED' ? 'selected' : ''}>CANCELLED</option></select></form>
          <form method="post" action="${ctx}/admin" onsubmit="return confirm('Delete appointment?')"><input type="hidden" name="_csrf" value="${csrf}"><input type="hidden" name="action" value="deleteAppointment"><input type="hidden" name="id" value="${a.id}"><button class="btn danger small">Delete</button></form>
        </td></tr>
    </c:forEach>
    <c:if test="${empty appointments}"><tr><td colspan="7" class="muted">No appointments yet.</td></tr></c:if></table></div>
</c:if>

<c:if test="${section eq 'settings'}">
  <h1>System Settings</h1>
  <div class="card"><form method="post" action="${ctx}/admin">
    <input type="hidden" name="_csrf" value="${csrf}"><input type="hidden" name="action" value="saveSettings">
    <label>Site name<input name="site_name" value="<c:out value='${settings.site_name}'/>" required></label>
    <label>Appointment slot length (minutes, 10-120)<input type="number" name="slot_minutes" value="${settings.slot_minutes}" min="10" max="120" required></label>
    <label>Booking window (days ahead, 1-365)<input type="number" name="max_advance_days" value="${settings.max_advance_days}" min="1" max="365" required></label>
    <label class="check"><input type="checkbox" name="allow_registration" ${settings.allow_registration eq 'true' ? 'checked' : ''}> Allow patient self-registration</label>
    <button class="btn">Save settings</button>
  </form></div>
</c:if>
</main>
</div>

<script src="https://cdn.jsdelivr.net/npm/chart.js"></script>

<c:if test="${section eq 'analytics'}">
<script>
    function getChartData(elementId) {
        const elements = document.querySelectorAll(
            "#" + elementId + " span"
        );

        const labels = [];
        const values = [];

        elements.forEach(function (element) {
            labels.push(element.dataset.label);
            values.push(Number(element.dataset.value));
        });

        return {
            labels: labels,
            values: values
        };
    }

    const usersData = getChartData("usersChartData");

    new Chart(document.getElementById("usersChart"), {
        type: "doughnut",
        data: {
            labels: usersData.labels,
            datasets: [{
                label: "Users",
                data: usersData.values
            }]
        },
        options: {
            responsive: true,
            plugins: {
                legend: {
                    position: "bottom"
                }
            }
        }
    });

    const appointmentsData = getChartData("appointmentsChartData");

    new Chart(document.getElementById("appointmentsChart"), {
        type: "bar",
        data: {
            labels: appointmentsData.labels,
            datasets: [{
                label: "Appointments",
                data: appointmentsData.values
            }]
        },
        options: {
            responsive: true,
            scales: {
                y: {
                    beginAtZero: true,
                    ticks: {
                        precision: 0
                    }
                }
            }
        }
    });
</script>
</c:if>

</body>
</html>
