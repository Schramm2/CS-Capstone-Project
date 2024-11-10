package uct.myadvisor.data;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface SmartTutorCourseRepository extends JpaRepository<SmartTutorCourse, Long>, JpaSpecificationExecutor<SmartTutorCourse> {

    //SmartTutorCourse findBySmartTutorCoursename(String SmartTutorCoursename);

    // get all smart tutor courses for a student
    @Query("SELECT s from SmartTutorCourse s WHERE s.student.id = :studentId")
    Page<SmartTutorCourse> listAllStudentCourses(@Param("studentId") Long studentID, Pageable pageable);

    // delete all smart tutor courses for a student
    @Modifying
    @Transactional
    @Query(value = "DELETE FROM smart_tutor_courses WHERE student_id = :studentId", nativeQuery = true)
    void deleteAllByStudent(@Param("studentId") Long studentID);
}
