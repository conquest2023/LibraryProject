package project.library.service.domain;

import org.springframework.data.geo.Point;
import project.library.controller.dto.book.NearestLibrary;
import project.library.controller.dto.book.search.BookSearchReseponseDto;
import project.library.util.GeoDistance;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class NearestLibrarySelector {

    public List<NearestLibrary> selectTopN(
            double userLat, double userLon,
            Map<String, Point> nearby, // libCode -> Point
            List<AbstractMap.SimpleEntry<String, BookSearchReseponseDto>> apiResults,
            int limit
    ) {
        return apiResults.stream()
                .filter(e -> e.getValue() != null && e.getValue().getResponse() != null)
                .filter(e -> "Y".equals(e.getValue().getResponse().getResult().getHasBook()))
                .map(e -> {
                    String libCode = e.getKey();
                    var resp = e.getValue().getResponse().getResult();
                    var p = nearby.get(libCode);
                    if (p == null) return null;

                    double km = GeoDistance.haversine(userLat, userLon, p.getY(), p.getX());
                    String isLoan = resp.getLoanAvailable();
                    return new NearestLibrary(libCode, isLoan, p.getY(), p.getX(), km);
                })
                .filter(Objects::nonNull)
                .sorted(Comparator.comparingDouble(NearestLibrary::getDistanceKm))
                .limit(limit)
                .toList();
    }
}