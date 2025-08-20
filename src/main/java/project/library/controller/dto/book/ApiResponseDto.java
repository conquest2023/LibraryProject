package project.library.controller.dto.book;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;

import java.util.List;

@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class ApiResponseDto {
    private List<BookDetailDto> detail;
}