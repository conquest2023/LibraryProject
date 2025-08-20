package project.library.controller.dto.book.search;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;

@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class BookSearchResponseDto {
    private SearchApiResponseDto response;
}