package project.library.controller.dto.book;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;

@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class LibraryResponseDto {
    private ApiResponseDto response;
}