package cl.serializers.delimited;


import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.junit.Test;

public class DelimitedStringParserExamples {

    @Test
    public void parseArrayOfStringsToAnObject() {
        
        String data = "Cabaret|1983|8.1|Liza Minelli,Michael York|Some comment to ignore";
        String[] values = DelimitedStringSplitter.pipe().split(data);
        
        Map<Integer, String> indexToProperty = new HashMap<>();
        indexToProperty.put(0, "title");
        indexToProperty.put(1, "year");
        indexToProperty.put(2, "rating");
        indexToProperty.put(3, "cast");
        
        Map<String, Function<String, Object>> valueParsers = new HashMap<>();
        valueParsers.put("cast", valueStr -> valueStr.split(","));
        
        DelimitedStringParser<Movie> parser = DelimitedStringParser.get(
                Movie.class, indexToProperty, valueParsers);
        
        Movie movie = parser.parse(values);
        
        assertEquals("Cabaret", movie.getTitle());
        assertEquals(1983, movie.getYear());
        assertEquals(new BigDecimal("8.1"), movie.getRating());
        assertArrayEquals(new String[] { "Liza Minelli", "Michael York" }, movie.getCast());
        
    }
    
    @Test
    public void useSetters() {
        Map<Integer, String> indexToProperty = new HashMap<>();
        indexToProperty.put(0, "title");
        indexToProperty.put(1, "year");
        indexToProperty.put(2, "rating");
        
        DelimitedStringParser<Movie> parser = DelimitedStringParser.get(
                Movie.class, indexToProperty, false)
                .with(DelimitedStringParser.useSetters, false).locked();
        
        Movie m = parser.parse(new String[] {"Forward in the past", "1990", "7.3"});
        
        assertEquals("Forward in the past", m.getTitle());
        assertEquals(1990, m.getYear());
        assertEquals(new BigDecimal("7.3"), m.getRating());
        assertNull(m.getCast());
    }
}

class Movie {
    private String title;
    private int year;
    private BigDecimal rating;
    private String[] cast;
    
    public Movie() {}

    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    public int getYear() {
        return year;
    }
    public void setYear(int year) {
        this.year = year;
    }
    public BigDecimal getRating() {
        return rating;
    }
    public void setRating(BigDecimal rating) {
        this.rating = rating;
    }
    public String[] getCast() {
        return cast;
    }
    public void setCast(String[] cast) {
        this.cast = cast;
    }
}
