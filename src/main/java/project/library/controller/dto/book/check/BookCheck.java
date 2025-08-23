package project.library.controller.dto.book.check;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.SimpleObjectIdResolver;
import lombok.Getter;
import lombok.Setter;

@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class BookCheck {

    private String libCode;

    private String hasBook;

    private String  loanAvailable;

}
