package project.library.controller.dto.book;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;

import java.util.List;

@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class LibraryResponseDto {
    private ApiResponseDto response;

    private int numFound;
    private List<BookDetailDto> docs;
}