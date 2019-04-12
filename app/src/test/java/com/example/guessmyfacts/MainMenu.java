package com.example.guessmyfacts;

import android.test.suitebuilder.annotation.Smoke;
import android.view.Menu;
import android.view.MenuItem;

import com.google.errorprone.annotations.Var;

import org.junit.Test;
import org.mockito.Mockito;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class MainMenu {
    @Test
    public void assureItemMenuIsNotSelected() {
        HomeScreen homeScreen = Mockito.mock(HomeScreen.class);
        MenuItem a = Mockito.mock(MenuItem.class);
        assertFalse(homeScreen.onOptionsItemSelected(a));

    }
    @Test
    public void menuItemNotVisibleWhenNotSelected() {
        HomeScreen homeScreen = Mockito.mock(HomeScreen.class);
        MenuItem a = Mockito.mock(MenuItem.class);
        assertFalse(homeScreen.onOptionsItemSelected(a));
        assertFalse(a.isVisible());
    }





}