package Utility;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;

public class Staff {
    private final String email;
    private String fullName;

    public Staff(String email, String fullName) {
        this.email = email;
        this.fullName = fullName;
    }

    public String getEmail(){
        return email;
    }

    public void setFullname(String name){ fullName = name;}

    public String getFullname() { return fullName;}


}
