package project.library.controller.dto;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class LocationDto {
    private double latitude;  // 위도
    private double longitude; // 경도

    private String  isbn;

}