package project.library.controller.dto.book;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Getter
@Setter
@NoArgsConstructor // 기본 생성자 추가 (JSON 파싱 및 유연성을 위해)
@JsonIgnoreProperties(ignoreUnknown = true)
public class BookDto {
    private String bookname;
    private String authors;
    private String publisher;
    private String publication_year;
    private String isbn13;
    private String description;
    private String bookImageURL;

    // 서킷 브레이커용 상태 필드 추가
    private boolean isError = false;

    // 기존 생성자
    public BookDto(String bookname, String authors, String publisher, String publication_year, String isbn13, String description, String bookImageURL) {
        this.bookname = bookname;
        this.authors = authors;
        this.publisher = publisher;
        this.publication_year = publication_year;
        this.isbn13 = isbn13;
        this.description = description;
        this.bookImageURL = bookImageURL;
        this.isError = false;
    }

    // 에러 객체 생성용 팩토리 메서드
    public static BookDto createError(String errorMessage) {
        BookDto errorDto = new BookDto();
        errorDto.setBookname(errorMessage); // 제목 자리에 에러 메시지를 넣음
        errorDto.setAuthors("-");
        errorDto.setPublisher("-");
        errorDto.setError(true);
        return errorDto;
    }
}