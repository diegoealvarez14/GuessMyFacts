
package com.example.guessmyfacts;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class UserTest {
    User user;
    @Before
    public void initialize() {
        user = new User("user@domain.com", 23, "White", "Soccer", "abcd");
    }

    @Test
    public void notNull() {
        assertNotNull(user);
    }

    @Test
    public void Contents() {
        String email = "user@domain.com";
        int age = 23;
        String color = "White";
        String hobby = "Soccer";
        String profile_pic = "abcd";

        assertEquals(email, user.email);
        assertEquals(age, user.age);
        assertEquals(color, user.color);
        assertEquals(hobby, user.hobby);
        assertEquals(profile_pic, user.profile_pic);

    }

}