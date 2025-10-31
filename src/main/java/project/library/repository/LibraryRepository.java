package project.library.repository;

import project.library.repository.collection.Library;

import java.util.List;

public interface LibraryRepository
{
    List<Library> findByLibCodeIn(List<Integer> libCodes);

}
