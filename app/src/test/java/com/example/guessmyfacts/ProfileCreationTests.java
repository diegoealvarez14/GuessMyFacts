
package com.example.guessmyfacts;

import android.view.View;
import android.widget.Button;

import org.junit.Test;
import org.mockito.Mockito;
import static org.junit.Assert.*;

public class ProfileCreationTests {

    @Test
    public void clickPhotoIfDocumentIsVisible() {
        ProfileCreation profileCreation = Mockito.mock(ProfileCreation.class);
        Button submitButton = Mockito.mock(Button.class);
        profileCreation.clickPhoto();

        assertTrue(submitButton.getVisibility() == View.VISIBLE);

    }
}

