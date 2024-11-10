package uct.myadvisor.services;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import uct.myadvisor.data.Advisor;
import uct.myadvisor.data.Course;
import uct.myadvisor.data.Semester;
import uct.myadvisor.data.CourseRepository;
import uct.myadvisor.data.SmartTutorCourse;
import uct.myadvisor.services.CourseService;
import uct.myadvisor.data.Student;

@Service
public class CourseService {

    private final CourseRepository repository;

    private final SmartTutorCourseService smartTutorCourseService;
    private final UserService userService;
    private final SemesterService semesterService;

    public CourseService(CourseRepository repository, SmartTutorCourseService smartTutorCourseService, UserService userService, SemesterService semesterService) {
        this.repository = repository;
        this.smartTutorCourseService = smartTutorCourseService;
        this.userService = userService;
        this.semesterService = semesterService;
    }

    // basic repository CRUD methods
    public Optional<Course> get(Long id) {
        return repository.findById(id);
    }

    public Course update(Course entity) {
        return repository.save(entity);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }

    public Page<Course> list(Pageable pageable) {
        return repository.findAll(pageable);
    }

    public Page<Course> list(Pageable pageable, Specification<Course> filter) {
        return repository.findAll(filter, pageable);
    }

    public int count() {
        return (int) repository.count();
    }

    // custom repository get methods
    public Page<Course> findAllDepartmentCourses(Advisor advisor, Pageable pageable) {
        return repository.findAllDepartmentCourses(advisor.getId(), pageable);
    }

    public Page<Course> findAllStudentElectiveCourses(Student student, Pageable pageable) {
        return repository.findAllStudentMajorsElectiveCourses(student.getId(), pageable);
    }

    // create the smart tutor required courses in smart tutor for given student (based on their majors)
    @Transactional
    public void addCoursesForStudentMajors(Student student, Pageable pageable) {
        List<Course> courses = repository.findAllStudentMajorsCourses(student.getId());

        for (Course course : courses) {
            SmartTutorCourse newSmartTutorCourse = new SmartTutorCourse();
            newSmartTutorCourse.setYear(course.determineYear(course));
            newSmartTutorCourse.setRequired(true);
            newSmartTutorCourse.setPassed(false);
            newSmartTutorCourse.setCourse(course);
            newSmartTutorCourse.setStudent(student);

            if (course.getSemesters().size() > 1) {
                newSmartTutorCourse.setSemester(null);
            }
            else {
                newSmartTutorCourse.setSemester(course.getSemesters().iterator().next());
            }

            smartTutorCourseService.update(newSmartTutorCourse);
        }
    }
    
}
