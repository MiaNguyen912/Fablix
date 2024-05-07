package Utility;

public class Star {
    private final String id;
    private int birthYear;
    private final String name;

    public Star(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setBirthYear(int birthYear) {this.birthYear = birthYear;}
    public int getBirthYear() {
        return birthYear;
    }

    public String toString() {
        return "ID:" + getId() + ", " +
                "Name:" + getName() + ", " +
                "Birth year:" + getBirthYear() + ".";
    }
}
