package project.library.controller.dto.book.search;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import project.library.controller.dto.book.BookDetailDto;

import java.util.List;

@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class SearchApiResponseDto {
    private int numFound;
    private List<BookDetailDto> docs;
}