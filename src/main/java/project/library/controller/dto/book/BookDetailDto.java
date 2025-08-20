package project.library.controller.dto.book;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;
import project.library.controller.dto.book.BookDto;

@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class BookDetailDto {
    private BookDto book;
}