package project.library.repository;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NearestLibraryDetail {
    private String libCode;      // 도서관 코드
    private String libName;      // 도서관 이름
    private String isLoan;
    private String address;      // 주소
    private String tel;          // 전화번호
//    private boolean isLoan;
    private double latitude;     // 위도
    private double longitude;    // 경도
    private double distanceKm;   // 사용자와의 거리 (km)

    private String  message;
    private boolean isError;
    public NearestLibraryDetail(String libCode, String libName, String isLoan, String address, String tel, double latitude, double longitude, double distanceKm) {
        this.libCode = libCode;
        this.libName = libName;
        this.isLoan = isLoan;
        this.address = address;
        this.tel = tel;
        this.latitude = latitude;
        this.longitude = longitude;
        this.distanceKm = distanceKm;
    }

    public NearestLibraryDetail(String message) {
        this.message = message;
    }

    public static NearestLibraryDetail createError(String message) {

        NearestLibraryDetail error = new NearestLibraryDetail(message);

        error.isError=true;
        return error;
    }
}
