package uct.myadvisor.services;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import uct.myadvisor.data.SmartTutorCourse;
import uct.myadvisor.data.SmartTutorCourseRepository;
import uct.myadvisor.data.Student;

@Service
public class SmartTutorCourseService {

    private final SmartTutorCourseRepository repository;

    public SmartTutorCourseService(SmartTutorCourseRepository repository) {
        this.repository = repository;
    }

    // basic repository CRUD methods
    public Optional<SmartTutorCourse> get(Long id) {
        return repository.findById(id);
    }

    public SmartTutorCourse update(SmartTutorCourse entity) {
        return repository.save(entity);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }

    public Page<SmartTutorCourse> list(Pageable pageable) {
        return repository.findAll(pageable);
    }

    public Page<SmartTutorCourse> list(Pageable pageable, Specification<SmartTutorCourse> filter) {
        return repository.findAll(filter, pageable);
    }

    public int count() {
        return (int) repository.count();
    }

    // get all courses for a student
    public Page<SmartTutorCourse> listAllStudentCourses(Student student, Pageable pageable) {
        return repository.listAllStudentCourses(student.getId(), pageable);
    }

    // delete all smart tutor courses for user
    @Transactional
    public void deleteAllSmartTutorCoursesForUser(Student student) {
        repository.deleteAllByStudent(student.getId());
    }
}
