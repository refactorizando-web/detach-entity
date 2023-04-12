package com.refactorizando.example.detachentity.repository;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.common.collect.Sets;
import com.refactorizando.example.detachentity.config.HibernateUtil;
import com.refactorizando.example.detachentity.entity.Department;
import com.refactorizando.example.detachentity.entity.Employee;
import com.refactorizando.example.detachentity.entity.SingleDepartment;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.PersistenceException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.MethodMode;


@SpringBootTest
@Slf4j
@DirtiesContext(methodMode = MethodMode.AFTER_METHOD)
public class DepartmentRepositoryIT {

  @PersistenceContext
  private EntityManager entityManager;

  @Autowired
  private SessionFactory sessionFactory;
  private SingleDepartment singleDepartment;

  private Department department;

  private Session session;
  private Transaction transaction;

  @BeforeEach
  public void beforeEach() {

    session = HibernateUtil.getSessionFactory().openSession();
    transaction = session.beginTransaction();

    List<Department> departments = session.createQuery("Select d from Department d",
        Department.class).list();

    if (null == departments || departments.size() == 0) {

      department = new Department();
      department.setName("Accounts");
      department = session.merge(department);
      session.evict(department);

    } else {

      department = departments.get(0);
      department = session.merge(department);
      session.evict(department);
    }
  }


  @Test
  public void given_department_when_detach_a_single_to_change_name_then_exception() {

    singleDepartment = new SingleDepartment();
    singleDepartment.setName("Accounts");
    session.persist(singleDepartment);
    session.evict(singleDepartment);

    singleDepartment.setName("Accounts exception");

    session.getTransaction().commit();

    assertThatThrownBy(() -> session.persist(singleDepartment)).isInstanceOf(
        PersistenceException.class).hasMessageContaining(
        "org.hibernate.PersistentObjectException` to JPA `PersistenceException` : detached entity passed to persist: com.refactorizando.example.detachentity.entity.SingleDepartment");

  }

  @Test
  public void given_department_when_detach_a_single_entity_with_merge_then_saved() {

    singleDepartment = new SingleDepartment();

    singleDepartment.setName("Accounts");
    session.persist(singleDepartment);
    session.evict(singleDepartment);

    singleDepartment.setName("Accounts exception");
    singleDepartment.setId(1L);
    session.merge(singleDepartment);
    session.getTransaction().commit();

    Query querySaved = session.createQuery("Select e from SingleDepartment e where  id= 1",
        SingleDepartment.class);

    singleDepartment = (SingleDepartment) querySaved.getSingleResult();

    assertTrue("Accounts exception".equalsIgnoreCase(singleDepartment.getName()));

  }

  @Test
  public void given_department_when_detach_a_employee_with_merge_then_saved() {

    Employee employee = new Employee();
    employee.setName("Noel");
    Department departmentMerge = session.merge(this.department);
    employee.setDepartment(departmentMerge);

    session.persist(employee);
    session.getTransaction().commit();

    List<Employee> employees = session.createQuery("Select c from Employee c", Employee.class)
        .list();

    assertEquals(employees.size(), 1);
    assertTrue(employees.get(0).getName().equalsIgnoreCase("Noel"));

  }

  @Test
  public void given_a_department_persist_when_new_employee_is_persist_then_exception_is_thrown() {

    department = new Department();
    department.setName("Accounts");
    session.persist(department);
    session.evict(department);
    department.setId(1L);

    Employee employee = new Employee();
    employee.setDepartment(department);

    session.persist(employee);
    assertThatThrownBy(() -> session.persist(department)).isInstanceOf(PersistenceException.class)
        .hasMessageContaining(
            "org.hibernate.PersistentObjectException` to JPA `PersistenceException` : detached entity passed to persist: com.refactorizando.example.detachentity.entity.Department");
    session.remove(employee);


  }

  @Test

  public void given_department_when_save_department_then_is_saved() {

    this.department = new Department();
    this.department.setName("Accounts");
    session.merge(department);
    session.evict(department);
    department.setId(1L);

    department.setName("Human Resource");

    session.merge(department);
    session.getTransaction().commit();

    List<Department> departments = session.createQuery("Select p from Department p",
        Department.class).list();

    assertEquals(departments.size(), 2);

  }

  @Test
  public void given_employee_when_save_without_department_then_employee_merged() {

    Employee employee = new Employee();

    employee.setName("Noel");

    employee = session.merge(employee);
    session.evict(employee);

    Query query = session.createQuery("Select e from Employee e where  id= :id", Employee.class);
    query.setParameter("id", employee.getId());
    employee = (Employee) query.getSingleResult();

    assertEquals("Noel", employee.getName());


  }

  @Test
  public void given_change_department_name_when_update_department_then_department_updated() {

    department.setName("human resource");

    session.merge(department);
    session.getTransaction().commit();

    List<Department> departments = session.createQuery("Select d from Department d",
        Department.class).list();

    assertEquals(departments.size(), 1);
    assertTrue("human resource".equalsIgnoreCase(departments.get(0).getName()));


  }


}
