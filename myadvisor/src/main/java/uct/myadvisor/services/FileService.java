package uct.myadvisor.services;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import uct.myadvisor.data.File;
import uct.myadvisor.data.FileRepository;

@Service
public class FileService {

    private final FileRepository repository;

    public FileService(FileRepository repository) {
        this.repository = repository;
    }

    // basic repository CRUD methods
    public Optional<File> get(Long id) {
        return repository.findById(id);
    }

    public File update(File entity) {
        return repository.save(entity);
    }

    //custom delete to delete the system stored file and database reference
    public void delete(Long id) {
        Optional<File> file = repository.findById(id);

        if (file.isPresent()) {
            File fileEntity = file.get();

            // Delete the file from the filesystem
            boolean isDeleted = fileEntity.deleteFileFromSystem();
            
            // Only delete the db record if the actual file is deleted
            if (isDeleted) {
                // Delete the database record
                repository.deleteById(id);
            } else {
                throw new RuntimeException("Failed to delete file from the filesystem.");
            }
        }
    }

    public Page<File> list(Pageable pageable) {
        return repository.findAll(pageable);
    }

    public Page<File> list(Pageable pageable, Specification<File> filter) {
        return repository.findAll(filter, pageable);
    }

    public int count() {
        return (int) repository.count();
    }

    // Fetch files uploaded by both the advisor and student
    public Page<File> getFilesByAdvisorAndStudent(Long advisorId, Long studentId, Pageable pageable) {
        return repository.findByAdvisorAndStudent(advisorId, studentId, pageable);
    }

    // Fetch files uploaded by the student and their advisor
    public Page<File> getFilesByStudentAndAdvisors(Long studentId, Pageable pageable) {
        return repository.findByStudentAndAdvisors(studentId, pageable);
    }

}
