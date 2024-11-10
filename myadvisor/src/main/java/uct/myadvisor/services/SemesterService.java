package uct.myadvisor.services;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import uct.myadvisor.data.Semester;
import uct.myadvisor.data.SemesterRepository;

@Service
public class SemesterService {

    private final SemesterRepository repository;

    public SemesterService(SemesterRepository repository) {
        this.repository = repository;
    }

    // basic repository CRUD methods
    public Optional<Semester> get(Long id) {
        return repository.findById(id);
    }

    public Semester update(Semester entity) {
        return repository.save(entity);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }

    public Page<Semester> list(Pageable pageable) {
        return repository.findAll(pageable);
    }

    public Page<Semester> list(Pageable pageable, Specification<Semester> filter) {
        return repository.findAll(filter, pageable);
    }

    public int count() {
        return (int) repository.count();
    }

}
