package project.library.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import project.library.repository.collection.Library;

import java.util.List;

@Repository
public interface LibraryJpaRepository extends MongoRepository<Library, String> {
    List<Library> findByLibCodeIn(List<Integer> libCodes);
}
