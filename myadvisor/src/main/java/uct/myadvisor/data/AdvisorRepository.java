package uct.myadvisor.data;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AdvisorRepository extends JpaRepository<Advisor, Long>, JpaSpecificationExecutor<Advisor> {

    Optional<Advisor> findByUsername(String username);

    //get all advisors for a faculty
    @Query("SELECT a FROM Advisor a WHERE a.faculty.id = :facultyId")
    Page<Advisor> findFacultyAdvisors(@Param("facultyId") Long facultyId, Pageable pageable);

    //get all advisors for a student based on their majors in common
    @Query(value = "SELECT DISTINCT au_advisor.* " +
                   "FROM application_user au_student " +
                   "JOIN user_major um_student ON au_student.id = um_student.user_id " +
                   "JOIN user_major um_advisor ON um_student.major_id = um_advisor.major_id " +
                   "JOIN application_user au_advisor ON um_advisor.user_id = au_advisor.id " +
                   "WHERE au_student.id = :studentId " +
                   "AND au_advisor.role = 'ADVISOR'",
           nativeQuery = true)
    Page<Advisor> findStudentsAdvisors(@Param("studentId") Long studentId, Pageable pageable);
}
