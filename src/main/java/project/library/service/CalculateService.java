package project.library.service;

import project.library.controller.dto.LocationDto;

import java.util.List;

public interface CalculateService {

    List<NearestLibraryDetail> calculateDistance(LocationDto locationDto);
}
