package uct.myadvisor.data;


import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MeetingRepository extends JpaRepository<Meeting, Long>, JpaSpecificationExecutor<Meeting> {

    // get all meetings for a student
    @Query("SELECT m from Meeting m WHERE m.student.id = :studentId AND m.status = 'Booked'")
    Page<Meeting> listStudentMeetings(@Param("studentId") Long studentID, Pageable pageable);

    // get all booked meetings for an advisor
    @Query("SELECT m from Meeting m WHERE m.advisor.id = :advisorId AND m.status = 'Booked'")
    Page<Meeting> listAdvisorMeetings(@Param("advisorId") Long advisorID, Pageable pageable);

    // get all slots for an advisor
    @Query("SELECT m from Meeting m WHERE m.advisor.id = :advisorId AND m.status = 'Slot' AND m.start > NOW()")
    Page<Meeting> listAllAdvisorOpenMeetingslots(@Param("advisorId") Long advisorID, Pageable pageable);

    // get all advisor meeting requests or cancelled 
    @Query("SELECT m from Meeting m WHERE m.advisor.id = :advisorId AND (m.status = 'Cancelled' OR m.status = 'Awaiting Approval' OR m.status = 'Custom Request' OR m.status = 'Rejected')")
    Page<Meeting> listAdvisorMeetingRequests(@Param("advisorId") Long advisorID, Pageable pageable);

    // get all student meeting requests or cancelled
    @Query("SELECT m from Meeting m WHERE m.student.id = :studentId AND (m.status = 'Cancelled' OR m.status = 'Awaiting Approval' OR m.status = 'Custom Request' OR m.status = 'Rejected')")
    Page<Meeting> listStudentMeetingRequests(@Param("studentId") Long studentID, Pageable pageable);
    
    @Query("SELECT m from Meeting m WHERE m.shared.id = :sharedId")
    Page<Meeting> listAdvisorShared(@Param("sharedId") Long sharedID, Pageable pageable);
}
