package uct.myadvisor.services;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import uct.myadvisor.data.Degree;
import uct.myadvisor.data.Department;
import uct.myadvisor.data.DepartmentRepository;
import uct.myadvisor.data.Faculty;
import uct.myadvisor.data.Major;

@Service
public class DepartmentService {

    private final DepartmentRepository repository;

    public DepartmentService(DepartmentRepository repository) {
        this.repository = repository;
    }

    // basic repository CRUD methods
    public Optional<Department> get(Integer id) {
        return repository.findById(id);
    }

    public Department update(Department entity) {
        return repository.save(entity);
    }

    public void delete(Integer id) {
        repository.deleteById(id);
    }

    public Page<Department> list(Pageable pageable) {
        return repository.findAll(pageable);
    }

    public Page<Department> list(Pageable pageable, Specification<Department> filter) {
        return repository.findAll(filter, pageable);
    }

    public int count() {
        return (int) repository.count();
    }

    // custom repository get methods
    public Page<Department> findAllFacultyDepartments(Faculty faculty, Pageable pageable) {
        return repository.findAllFacultyDepartments(faculty.getId(), pageable);
    }

}
