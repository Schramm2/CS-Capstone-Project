package uct.myadvisor.services;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import uct.myadvisor.data.Degree;
import uct.myadvisor.data.Faculty;
import uct.myadvisor.data.Major;
import uct.myadvisor.data.MajorRepository;
import uct.myadvisor.data.Message;
import uct.myadvisor.data.User;

@Service
public class MajorService {

    private final MajorRepository repository;

    public MajorService(MajorRepository repository) {
        this.repository = repository;
    }

    // basic repository CRUD methods
    public Optional<Major> get(Integer id) {
        return repository.findById(id);
    }

    public Major update(Major entity) {
        return repository.save(entity);
    }

    public void delete(Integer id) {
        repository.deleteById(id);
    }

    public Page<Major> list(Pageable pageable) {
        return repository.findAll(pageable);
    }

    public Page<Major> list(Pageable pageable, Specification<Major> filter) {
        return repository.findAll(filter, pageable);
    }

    public int count() {
        return (int) repository.count();
    }

    // custom repository get methods
    public Page<Major> findAllFacultyMajors(Faculty faculty, Pageable pageable) {
        return repository.findAllFacultyMajors(faculty.getId(), pageable);
    }

    public Page<Major> findAllDegreeMajors(Degree degree, Pageable pageable) {
        return repository.findAllDegreeMajors(degree.getId(), pageable);
    }
}
