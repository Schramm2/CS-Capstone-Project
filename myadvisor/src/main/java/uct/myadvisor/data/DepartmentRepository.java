package uct.myadvisor.data;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DepartmentRepository extends JpaRepository<Department, Integer>, JpaSpecificationExecutor<Department> {

    //Department findByDepartmentname(String Departmentname);
    
    // get all departments for a faculty
    @Query("SELECT d FROM Department d WHERE d.faculty.id = :facultyId")
    Page<Department> findAllFacultyDepartments(@Param("facultyId") Long facultyId, Pageable pageable);    
}
