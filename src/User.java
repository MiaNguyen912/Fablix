import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;

public class User {
    private final String username;
    private final String userID;

    private String fullname;


    private ArrayList<Integer> sales_id;
    private HashMap<String, HashMap<String, String>> cart_items;

    // {
    //  "12345": {title: "movieX", quantity: 2},
    //  "12005": {title: "movie&", quantity: 1},
    // }

    public User(String username, String id) {
        this.username = username;
        this.userID = id;
        cart_items = new HashMap<>();
        sales_id = new ArrayList<>();
    }

    public String getUsername(){
        return username;
    }

    public String getUserID() { return userID;}

    public void setFullname(String name){ fullname = name;}

    public String getFullname() { return fullname;}

    public HashMap<String, HashMap<String, String>> getCart(){
        return cart_items;
    }

    public void setCart(HashMap<String, HashMap<String, String>> new_cart){
        cart_items = new_cart;
    }


    public void setSales(ArrayList<Integer> sales){
        sales_id = sales;
    }

    public ArrayList<Integer> getSales(){
        return sales_id;
    }




    public void addItemToCart(String item_id, String item_name){
        if (cart_items.get(item_id) == null){
            // item doesn't exist in cart yet
            HashMap<String, String> new_item = new HashMap<>();
            new_item.put("title", item_name);
            new_item.put("quantity", "1");
            cart_items.put(item_id, new_item);
        } else {
            // if item is already in cart, increase the quantity
            HashMap<String, String> current_item_details = cart_items.get(item_id);
            String current_quantity = current_item_details.get("quantity");
            String new_quantity = (Integer.parseInt(current_quantity) + 1) + "";

            current_item_details.put("quantity", new_quantity);
            cart_items.put(item_id, current_item_details);
        }

    }

    public void removeItem(String item_id){
        cart_items.remove(item_id);
    }

    public void increaseItem(String item_id){
        // get current item's details
        HashMap<String, String> current_item_details = cart_items.get(item_id);
        String current_quantity = current_item_details.get("quantity");
        String new_quantity = (Integer.parseInt(current_quantity) + 1) + "";

        current_item_details.put("quantity", new_quantity);
        cart_items.put(item_id, current_item_details);
    }

    public void decreaseItem(String item_id){
        // get current item's details
        HashMap<String, String> current_item_details = cart_items.get(item_id);
        String current_quantity = current_item_details.get("quantity");
        String new_quantity = (Integer.parseInt(current_quantity) - 1) + "";

        if (new_quantity.equals("0")){
            removeItem(item_id);
        } else {
            current_item_details.put("quantity", new_quantity);
            cart_items.put(item_id, current_item_details);
        }

    }
}
