package project.library.controller.dto.book;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NearestLibrary {

   private String libCode;

   private String  isLoan;

   private double lat;
   private double lon;
   private double distanceKm;
}
