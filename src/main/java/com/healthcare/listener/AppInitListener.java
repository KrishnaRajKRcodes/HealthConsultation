package com.healthcare.listener;

import com.healthcare.dao.SettingsDAO;
import com.healthcare.dao.UserDAO;
import com.healthcare.model.Role;
import com.healthcare.service.UserService;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

/** On start-up: seeds default settings and a default admin (admin@health.com / Admin@123) if none exists. */
@WebListener
public class AppInitListener implements ServletContextListener {
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        ServletContext ctx = sce.getServletContext();
        try {
            SettingsDAO settings = new SettingsDAO();
            settings.ensureDefaults();
            ctx.setAttribute("siteName", settings.getAll().get("site_name"));
            if (!new UserDAO().existsWithRole(Role.ADMIN)) {
                new UserService().createUser("System Admin", "admin@health.com", "Admin@123", Role.ADMIN, null, null);
                ctx.log("Default admin created: admin@health.com / Admin@123 (change it!)");
            }
        } catch (Exception e) {
            ctx.log("Database initialisation failed - check db.properties and run sql/schema.sql", e);
            ctx.setAttribute("siteName", "HealthConnect");
        }
    }
}
