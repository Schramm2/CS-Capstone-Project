package uct.myadvisor.data;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface FileRepository extends JpaRepository<File, Long>, JpaSpecificationExecutor<File> {

    //File findByName(String fileName);
    
    // Find files uploaded by both an advisor and a specific student -> for advisors file view
    @Query("SELECT f FROM File f WHERE (f.uploadedBy.id = :advisorId AND f.student.id = :studentId) OR f.uploadedBy.id = :studentId")
    Page<File> findByAdvisorAndStudent(@Param("advisorId") Long advisorId, @Param("studentId") Long studentId, Pageable pageable);  

    // Find files uploaded by both a student their advisor(s) -> for students file view
    @Query("SELECT f FROM File f WHERE (f.student.id = :studentId)")
    Page<File> findByStudentAndAdvisors(@Param("studentId") Long studentId, Pageable pageable);

}