package uct.myadvisor.services;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import uct.myadvisor.data.Admin;
import uct.myadvisor.data.AdminRepository;
import uct.myadvisor.data.Advisor;
import uct.myadvisor.data.AdvisorRepository;
import uct.myadvisor.data.Faculty;
import uct.myadvisor.data.Student;
import uct.myadvisor.data.StudentRepository;
import uct.myadvisor.data.User;
import uct.myadvisor.data.UserRepository;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final AdminRepository adminRepository;
    private final AdvisorRepository advisorRepository;

    public UserService(UserRepository userRepository, StudentRepository studentRepository, AdminRepository adminRepository, AdvisorRepository advisorRepository) {
        this.userRepository = userRepository;
        this.studentRepository = studentRepository;
        this.adminRepository = adminRepository;
        this.advisorRepository = advisorRepository;
    }

    // Create a Student upon registration
    public boolean createStudent(Student student) {
        // Check whether existing user
        Optional<Student> existingUser = studentRepository.findByUsername(student.getUsername());
        if (existingUser.isPresent()) {
            System.out.println(existingUser);
            return false; // Student already exists
        }

        // Create a new student
        studentRepository.save(student);

        return true;
    }

    // User Methods

    // basic repository CRUD methods
    public Optional<User> get(Long id) {
        return userRepository.findById(id);
    }

    public User update(User entity) {
        return userRepository.save(entity);
    }

    public void delete(Long id) {
        userRepository.deleteById(id);
    }

    public Page<User> list(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    public Page<User> list(Pageable pageable, Specification<User> filter) {
        return userRepository.findAll(filter, pageable);
    }

    public int count() {
        return (int) userRepository.count();
    }

    // Student Methods

    // basic repository CRUD methods
    public Optional<Student> getStudent(Long id) {
        return studentRepository.findById(id);
    }

    public Student updateStudent(Student entity) {
        return studentRepository.save(entity);
    }

    public void deleteStudent(Long id) {
        studentRepository.deleteById(id);
    }

    public Page<Student> listStudents(Pageable pageable) {
        return studentRepository.findAll(pageable);
    }

    public Page<Student> listStudents(Pageable pageable, Specification<Student> filter) {
        return studentRepository.findAll(filter, pageable);
    }

    public int countStudent() {
        return (int) studentRepository.count();
    }

    public Page<Student> findAdvisorsStudents(Advisor advisor, Pageable pageable) {
        return studentRepository.findAdvisorsStudents(advisor.getId(), pageable);
    }

    // Admin Methods

    // basic repository CRUD methods
    public Optional<Admin> getAdmin(Long id) {
        return adminRepository.findById(id);
    }

    public Admin updateAdmin(Admin entity) {
        return adminRepository.save(entity);
    }

    public void deleteAdmin(Long id) {
        adminRepository.deleteById(id);
    }

    public Page<Admin> listAdmins(Pageable pageable) {
        return adminRepository.findAll(pageable);
    }

    public Page<Admin> listAdmins(Pageable pageable, Specification<Admin> filter) {
        return adminRepository.findAll(filter, pageable);
    }

    public int countAdmin() {
        return (int) adminRepository.count();
    }

    // Advisor Methods

    // basic repository CRUD methods
    public Optional<Advisor> getAdvisor(Long id) {
        return advisorRepository.findById(id);
    }

    public Advisor updateAdvisor(Advisor entity) {
        return advisorRepository.save(entity);
    }

    public void deleteAdvisor(Long id) {
        advisorRepository.deleteById(id);
    }

    public Page<Advisor> listAdvisors(Pageable pageable) {
        return advisorRepository.findAll(pageable);
    }

    public Page<Advisor> listAdvisors(Pageable pageable, Specification<Advisor> filter) {
        return advisorRepository.findAll(filter, pageable);
    }

    public int countAdvisor() {
        return (int) advisorRepository.count();
    }

    public Page<Advisor> findFacultyAdvisors(Faculty faculty, Pageable pageable) {
        return advisorRepository.findFacultyAdvisors(faculty.getId(), pageable);
    }

    public Page<Advisor> findStudentsAdvisors(Student student, Pageable pageable) {
        return advisorRepository.findStudentsAdvisors(student.getId(), pageable);
    }

}
