package uct.myadvisor.data;


import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CourseRepository extends JpaRepository<Course, Long>, JpaSpecificationExecutor<Course> {

   //Course findByCoursename(String Coursename);

   // gets all courses for an advisors chosen departments
   @Query(value = "SELECT c.* FROM courses c JOIN departments d ON c.department_id = d.id JOIN advisor_department ad ON ad.department_id = d.id WHERE ad.user_id = :advisorId",
      countQuery = "SELECT count(c.id) FROM courses c JOIN departments d ON c.department_id = d.id JOIN advisor_department ad ON ad.department_id = d.id WHERE ad.user_id = :advisorId",
      nativeQuery = true)
   Page<Course> findAllDepartmentCourses(@Param("advisorId") Long advisorId, Pageable pageable);

   //get all required courses for the students majors
   @Query(value = "SELECT DISTINCT c.* " +
            "FROM courses c " +
            "JOIN major_requiredcourse mr ON c.id = mr.course_id " +
            "JOIN majors m ON mr.major_id = m.id " +
            "JOIN user_major um ON m.id = um.major_id " +
            "JOIN application_user u ON um.user_id = u.id " +
            "LEFT JOIN course_semester cs ON c.id = cs.course_id " +
            "WHERE u.id = :studentId", 
      nativeQuery = true)
   List<Course> findAllStudentMajorsCourses(@Param("studentId") Long studentId);

   //get all elective courses for a students majors
   @Query(value = "SELECT DISTINCT c.* " +
                  "FROM courses c " +
                  "JOIN major_electivecourse mr ON c.id = mr.course_id " +
                  "JOIN majors m ON mr.major_id = m.id " +
                  "JOIN user_major um ON m.id = um.major_id " +
                  "JOIN application_user u ON um.user_id = u.id " +
                  "WHERE u.id = :studentId", 
         nativeQuery = true)
   Page<Course> findAllStudentMajorsElectiveCourses(@Param("studentId") Long studentId, Pageable pageable);
}