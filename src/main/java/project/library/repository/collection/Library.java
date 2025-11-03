package project.library.repository.collection;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Data
@Document(collection = "libraries")
public class Library {
    @Id
    private String id;

    @Field("libCode")
    private int libCode;

    @Field("libName")
    private String libName;

    @Field("address")
    private String address;

    @Field("tel")
    private String tel;

    @Field("fax")
    private String fax;

    @Field("latitude")
    private Double latitude;

    @Field("longitude")
    private Double longitude;

    @Field("homepage")
    private String homepage;

    @Field("closed")
    private String closed;

    @Field("operatingTime")
    private String operatingTime;

    @Field("BookCount")
    private Integer bookCount;


    public Library(int libCode, String address,String libName, String closed,Double latitude,Double longitude ,String tel,String homepage) {
        this.libCode=libCode;
        this.address = address;
        this.libName = libName;
        this.closed = closed;
        this.latitude=latitude;
        this.longitude=longitude;
        this.tel= tel;
        this.homepage=homepage;
    }
}
