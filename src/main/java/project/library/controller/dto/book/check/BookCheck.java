package project.library.controller.dto.book.check;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class BookCheck {

    private String hasBook;

    private String  loanAvailable;


}
