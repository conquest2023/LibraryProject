package project.library.controller.dto.book.search;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;

@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class BookSearchReseponseDto {
    private SearchApiResponseDto response;
}