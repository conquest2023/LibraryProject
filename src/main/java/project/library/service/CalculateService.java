package project.library.service;

import project.library.controller.dto.UserLocation;

import java.util.List;

public interface CalculateService {

    List<NearestLibraryDetail> findNearbyLibrary(UserLocation locationDto);
}
