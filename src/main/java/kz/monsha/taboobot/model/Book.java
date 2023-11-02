package kz.monsha.taboobot.model;

import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Id;
import dev.morphia.annotations.Property;
import lombok.Data;

@Data
@Entity("Books")
public class Book {
    @Id
    private String isbn;
    private String title;
    private String author;
    @Property("price")
    private double cost;

}