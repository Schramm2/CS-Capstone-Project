package uct.myadvisor.data;


import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;


public interface DegreeRepository extends JpaRepository<Degree, Integer>, JpaSpecificationExecutor<Degree> {

    //Degree findByDegreename(String Degreename);

    // get all degrees for a faculty
    @Query("SELECT d FROM Degree d WHERE d.faculty.id = :facultyId")
    Page<Degree> findFacultyDegrees(@Param("facultyId") Long facultyId, Pageable pageable);    
}
