package uct.myadvisor.services;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import uct.myadvisor.data.Advisor;
import uct.myadvisor.data.Meeting;
import uct.myadvisor.data.MeetingRepository;
import uct.myadvisor.data.Student;

@Service
public class MeetingService {

    private final MeetingRepository repository;

    public MeetingService(MeetingRepository repository) {
        this.repository = repository;
    }

    // basic repository CRUD methods
    public Optional<Meeting> get(Long id) {
        return repository.findById(id);
    }

    public Meeting update(Meeting entity) {
        return repository.save(entity);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }

    public Page<Meeting> listEverything(Pageable pageable) {
        return repository.findAll(pageable);
    }

    public Page<Meeting> list(Pageable pageable, Specification<Meeting> filter) {
        return repository.findAll(filter, pageable);
    }

    public int count() {
        return (int) repository.count();
    }

    // custom repository get methods
    public Page<Meeting> listAdvisorBooked(Advisor advisor, Pageable pageable) {
        return repository.listAdvisorMeetings(advisor.getId(), pageable);
    }

    public Page<Meeting> listStudentBooked(Student student, Pageable pageable) {   
        return repository.listStudentMeetings(student.getId(), pageable);
    }

    public Page<Meeting> listAdvisorSlots(Advisor advisor, Pageable pageable) {
        return repository.listAllAdvisorOpenMeetingslots(advisor.getId(), pageable);
    }

    // List only the meetings where student has been assigned to the meeting,
    // Advisor has not approved the meeting
    public Page<Meeting> listAdvisorRequests(Advisor advisor, Pageable pageable) {
        return repository.listAdvisorMeetingRequests(advisor.getId(), pageable);
    }
    public Page<Meeting> listStudentRequests(Student student, Pageable pageable) {
        return repository.listStudentMeetingRequests(student.getId(), pageable);
    }

    public Page<Meeting> listSharedNotes(Advisor advisor, Pageable pageable) {
        return repository.listAdvisorShared(advisor.getId(), pageable);
    }
}
