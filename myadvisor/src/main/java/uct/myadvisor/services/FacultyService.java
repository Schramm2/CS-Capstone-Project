package uct.myadvisor.services;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import uct.myadvisor.data.Faculty;
import uct.myadvisor.data.FacultyRepository;

@Service
public class FacultyService {

    private final FacultyRepository repository;

    public FacultyService(FacultyRepository repository) {
        this.repository = repository;
    }

    // basic repository CRUD methods
    public Optional<Faculty> get(Integer id) {
        return repository.findById(id);
    }

    public Faculty update(Faculty entity) {
        return repository.save(entity);
    }

    public void delete(Integer id) {
        repository.deleteById(id);
    }

    public Page<Faculty> list(Pageable pageable) {
        return repository.findAll(pageable);
    }

    public Page<Faculty> list(Pageable pageable, Specification<Faculty> filter) {
        return repository.findAll(filter, pageable);
    }

    public int count() {
        return (int) repository.count();
    }

}
