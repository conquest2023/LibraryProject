package project.library.service;

import project.library.controller.dto.UserLocation;
import project.library.controller.dto.book.BookDto;
import project.library.repository.NearestLibraryDetail;

import java.util.List;

public interface LibraryService {

    List<NearestLibraryDetail> findNearbyLibrary(UserLocation locationDto);

    List<NearestLibraryDetail> findTestNearbyLibrary(UserLocation userLocation);

    List<BookDto> searchBook(String sessionId,String title);

    List<String> getHistoryBook(String sessionId);
}
