package com.ecom.utils;

import com.ecom.models.Reviews;
import com.ecom.models.User;

public class DTOUtils {
    public static void UserDTO(int userID,String username, String email,String userrole) {
        new User(userID,username,email,userrole);

    }

    public static void ReviewsDTO(int reviewID, int productID, int userID, String comment, int rating) {
        new Reviews(reviewID,userID,productID,rating,comment);
    }
}
