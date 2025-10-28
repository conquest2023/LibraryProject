package project.library.service;

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

}
