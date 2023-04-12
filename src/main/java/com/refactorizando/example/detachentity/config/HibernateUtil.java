package com.refactorizando.example.detachentity.config;

import com.refactorizando.example.detachentity.entity.Department;
import com.refactorizando.example.detachentity.entity.Employee;
import com.refactorizando.example.detachentity.entity.SingleDepartment;
import java.util.Properties;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;
import org.hibernate.service.ServiceRegistry;


@Slf4j
public class HibernateUtil {

  private static SessionFactory sessionFactory;

  public static SessionFactory getSessionFactory() {
    log.debug("Creating sessionFactory");

    if (sessionFactory == null) {
      try {
        Configuration configuration = new Configuration();
        Properties settings = new Properties();
        settings.put(Environment.DRIVER, "org.h2.Driver");
        settings.put(Environment.URL, "jdbc:h2:mem:mydb");
        settings.put(Environment.USER, "sa");
        settings.put(Environment.PASS, "");
        settings.put(Environment.DIALECT, "org.hibernate.dialect.H2Dialect");

        settings.put(Environment.SHOW_SQL, "false");
        settings.put(Environment.FORMAT_SQL, "true");
        settings.put(Environment.USE_SQL_COMMENTS, "true");
        settings.put(Environment.HBM2DDL_AUTO, "update");
        configuration.setProperties(settings);

        configuration.addAnnotatedClass(Department.class);
        configuration.addAnnotatedClass(Employee.class);
        configuration.addAnnotatedClass(SingleDepartment.class);

        ServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder().applySettings(
                configuration.getProperties())
            .build();
        sessionFactory = configuration.buildSessionFactory(serviceRegistry);

      } catch (Exception e) {
        log.error("Something went wrong {} ", e);
        throw new IllegalArgumentException();
      }
    }
    return sessionFactory;
  }
}