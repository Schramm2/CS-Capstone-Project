package uct.myadvisor.services;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import uct.myadvisor.data.Degree;
import uct.myadvisor.data.DegreeRepository;
import uct.myadvisor.data.Faculty;

@Service
public class DegreeService {

    private final DegreeRepository repository;

    public DegreeService(DegreeRepository repository) {
        this.repository = repository;
    }

    // basic repository CRUD methods
    public Optional<Degree> get(Integer id) {
        return repository.findById(id);
    }

    public Degree update(Degree entity) {
        return repository.save(entity);
    }

    public void delete(Integer id) {
        repository.deleteById(id);
    }

    public Page<Degree> list(Pageable pageable) {
        return repository.findAll(pageable);
    }

    public Page<Degree> list(Pageable pageable, Specification<Degree> filter) {
        return repository.findAll(filter, pageable);
    }

    public int count() {
        return (int) repository.count();
    }

    // custom repository get methods
    public Page<Degree> findFacultyDegrees(Faculty faculty, Pageable pageable) {
        return repository.findFacultyDegrees(faculty.getId(), pageable);
    }

}
