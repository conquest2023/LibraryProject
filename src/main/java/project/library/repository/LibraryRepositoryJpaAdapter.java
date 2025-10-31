package project.library.repository;

import lombok.RequiredArgsConstructor;
import project.library.repository.collection.Library;

import java.util.List;


@RequiredArgsConstructor
public class LibraryRepositoryJpaAdapter implements LibraryRepository{

    private final LibraryJpaRepository repository;
    @Override
    public List<Library> findByLibCodeIn(List<Integer> libCodes) {
        return repository.findByLibCodeIn(libCodes);
    }
}
