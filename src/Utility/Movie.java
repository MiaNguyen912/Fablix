package Utility;

import java.util.ArrayList;

public class Movie {
    private final String id;
    private String title;
    private int year;
    private String director;
    private int price;

    private ArrayList<String> genres;

    public Movie(String id, String title, int year, String director, int price) {
        this.id = id;
        this.title = title;
        this.year = year;
        this.director = director;
        this.price = price;
        genres = new ArrayList<>();
    }

    public String getId() {
        return id;
    }
    public String getTitle() {
        return title;
    }
    public int getYear() { return year; }
    public String getDirector() {
        return director;
    }
    public int getPrice() {
        return price;
    }
    public void setGenres(ArrayList<String> genres) {this.genres = genres;}
    public ArrayList<String> getGenres(){return genres;}

    public String toString() {
        return "ID:" + getId() + ", " +
                "Title:" + getTitle() + ", " +
                "Release year:" + getYear() + ", " +
                "Director:" + getDirector() + ", " +
                "Price:" + getPrice() + ", " +
                "Genres: " + getGenres();
    }
}
