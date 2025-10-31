package project.library.controller.dto;

import lombok.Getter;
import lombok.ToString;
import org.springframework.data.geo.Circle;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.connection.RedisGeoCommands;

@Getter
@ToString
public class UserLocation {
    private double latitude;  // 위도

    private double longitude; // 경도

    private String  isbn;


    public   Circle toCircle(double radiusKm) {
        Point p = new Point(longitude, latitude);
        Distance radius = new Distance(radiusKm, RedisGeoCommands.DistanceUnit.KILOMETERS);
        return new Circle(p, radius);
    }
}