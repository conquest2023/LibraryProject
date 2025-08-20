package project.library.controller.dto.book.search;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;

import java.util.List;

@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class SearchApiResponseDto {
    private int numFound;
    private List<DocWrapperDto> docs;
}