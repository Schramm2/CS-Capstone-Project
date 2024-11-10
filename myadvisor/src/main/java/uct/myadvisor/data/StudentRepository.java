package uct.myadvisor.data;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface StudentRepository extends JpaRepository<Student, Long>, JpaSpecificationExecutor<Student> {

    Optional<Student> findByUsername(String username);

    // get all students for an advisor based on common majors
    @Query(value = "SELECT DISTINCT au_student.* " +
                   "FROM application_user au_advisor " +
                   "JOIN user_major um_advisor ON au_advisor.id = um_advisor.user_id " +
                   "JOIN user_major um_student ON um_advisor.major_id = um_student.major_id " +
                   "JOIN application_user au_student ON um_student.user_id = au_student.id " +
                   "WHERE au_advisor.id = :advisorId " +
                   "AND au_advisor.role = 'ADVISOR' " +
                   "AND au_student.role = 'STUDENT'", 
           nativeQuery = true)
    Page<Student> findAdvisorsStudents(@Param("advisorId") Long advisorId, Pageable pageable);

}
