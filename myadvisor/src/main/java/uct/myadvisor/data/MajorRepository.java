package uct.myadvisor.data;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MajorRepository extends JpaRepository<Major, Integer>, JpaSpecificationExecutor<Major> {

    //Major findByMajorname(String Majorname);
    
    // get all majors for a faculty
    @Query("SELECT m FROM Major m JOIN m.degree d JOIN d.faculty f WHERE f.id = :facultyId")
    Page<Major> findAllFacultyMajors(@Param("facultyId") Long facultyId, Pageable pageable);
    
    // get all majors for a degree
    @Query("SELECT m FROM Major m WHERE m.degree.id = :degreeId")
    Page<Major> findAllDegreeMajors(@Param("degreeId") Long degreeId, Pageable pageable);
}
