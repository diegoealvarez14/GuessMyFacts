package com.example.guessmyfacts;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Debug;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.annotations.NotNull;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


public class ProfileCreation extends AppCompatActivity {

    private FirebaseAuth mAuth;
    String email;

    GoogleSignInClient mGoogleSignInClient;
    DocumentReference docRef;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    final String AGE_KEY = "AGE";
    final String COLOR_KEY = "COLOR";
    final String HOBBY_KEY = "HOBBY";
    final String PHOTO_KEY = "PROFILE-PIC";
    String photoURL;
    String id;

    File image = null;



    private boolean docExists;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_creation);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        final Button updateButton = findViewById(R.id.updateButton);
        final Button submitButton = findViewById(R.id.submitButton);
        final Button imageButton = findViewById(R.id.cameraButton);

        mAuth = FirebaseAuth.getInstance();
        email = GoogleSignIn.getLastSignedInAccount(this).getEmail();
        GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(this);
        id = acct.getId();

        docRef = db.collection("users").document(email);

        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        docExists = true;
                    } else {
                        docExists = false;
                    }
                } else {
                    Log.d("Tag", "get failed with ", task.getException());
                }
            }
        });



        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                update();
            }
        });




        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                submit();
            }
        });


        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clickPhoto();
//                imageButton.setVisibility(View.INVISIBLE);
            }
        });


    }

    @Override
    protected void onStart() {

        super.onStart();
//        if(docExists) {
//
//        }
        //profile creation page should be displayed when the user logs in for the first time
        //add code here to navigate away to game activity directly if this is not the first time the user is logging in
    }

    private void signOut() {
        final Intent login = new Intent(this, Login.class);
        mGoogleSignInClient.signOut()
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        startActivity(login);
                        finish();
                    }
                });
    }

    public void clickPhoto(){
        final Button updateButton = findViewById(R.id.updateButton);
        final Button submitButton = findViewById(R.id.submitButton);
        dispatchTakePictureIntent();
//        galleryAddPic();
//        uploadPictureToFirebase();

//        TODO: JUST SET THE BUTTON CLICKABLE AFTER PHOTO IS TAKEN
        if(docExists) {
            updateButton.setVisibility(View.VISIBLE);
        }
        else {
            submitButton.setVisibility(View.VISIBLE);
        }
    }

//    private void uploadPictureToFirebase() {
//        FirebaseStorage storage = FirebaseStorage.getInstance();
//        // Create a storage reference from our app
//        StorageReference storageRef = storage.getReference();
//
//        // Create a reference to "mountains.jpg"
//        StorageReference userRef = storageRef.child(id + ".jpg");
//
//        // Create a reference to 'images/mountains.jpg'
//        StorageReference userImagesRef = storageRef.child("images/" + id + ".jpg");
//
//        // While the file names are the same, the references point to different files
//        userRef.getName().equals(userImagesRef.getName());    // true
//        userRef.getPath().equals(userImagesRef.getPath());    // false
//
//
//
//        Uri file = Uri.fromFile(image);
//        StorageReference riversRef = storageRef.child("images/"+file.getLastPathSegment());
//        UploadTask uploadTask = riversRef.putFile(file);
//
//        // Register observers to listen for when the download is done or if it fails
//        uploadTask.addOnFailureListener(new OnFailureListener() {
//            @Override
//            public void onFailure(@NonNull Exception exception) {
//                Toast.makeText(ProfileCreation.this, "Error Getting File From Local Storage", Toast.LENGTH_SHORT).show();
//            }
//        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
//            @Override
//            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
//                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.
//                Toast.makeText(ProfileCreation.this, "Successfully Uploaded Image from Local Storage", Toast.LENGTH_SHORT).show();
//            }
//        });
//
//    }


//    static final int REQUEST_TAKE_PHOTO = 1;
//
//    private void dispatchTakePictureIntent() {
//        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//        // Ensure that there's a camera activity to handle the intent
//        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
//            // Create the File where the photo should go
//            File photoFile = null;
//            try {
//                photoFile = createImageFile();
//            } catch (IOException ex) {
//                Log.d("Tag", "Failure Creating Image File Path ", ex);
//            }
//            // Continue only if the File was successfully created
//            if (photoFile != null) {
//                image = photoFile;
//                Uri photoURI = FileProvider.getUriForFile(this,
//                        "com.example.android.fileprovider",
//                        photoFile);
//                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
//                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
//            }
//        }
//    }



//    private static  String currentPhotoPath;
//
//    private File createImageFile() throws IOException {
//        // Create an image file name
//        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
//        String imageFileName = "JPEG_" + timeStamp + "_";
//        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
//        File image = File.createTempFile(
//                imageFileName,  /* prefix */
//                ".jpg",         /* suffix */
//                storageDir      /* directory */
//        );
//
//        // Save a file: path for use with ACTION_VIEW intents
//        currentPhotoPath = image.getAbsolutePath();
//        return image;
//    }


//    private void galleryAddPic() {
//        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
//        File f = new File(currentPhotoPath);
//        Uri contentUri = Uri.fromFile(f);
//        mediaScanIntent.setData(contentUri);
//        this.sendBroadcast(mediaScanIntent);
//    }

    static final int REQUEST_IMAGE_CAPTURE = 1;

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }




    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap bitmap = (Bitmap) extras.get("data");
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            photoURL = Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);
            //imageView.setImageBitmap(imageBitmap);
        }
    }


    private void update(){
        final Intent homeScreen = new Intent(this, HomeScreen.class);
        EditText editTextAge = findViewById(R.id.editTextAge);
        EditText editTextColor = findViewById(R.id.editTextColor);
        EditText editTextHobby = findViewById(R.id.editTextHobby);

        String age = editTextAge.getText().toString();
        String color = editTextColor.getText().toString();
        String hobby = editTextHobby.getText().toString();

        Map<String, Object> user = new HashMap<>();
        user.put(AGE_KEY, age);
        user.put(COLOR_KEY, color);
        user.put(HOBBY_KEY, hobby);
        user.put(PHOTO_KEY, photoURL);

        db.collection("users").document(email).update(user);
        startActivity(homeScreen);
    }

    private void submit(){
        final Intent homeScreen = new Intent(this, HomeScreen.class);

        EditText editTextAge = findViewById(R.id.editTextAge);
        EditText editTextColor = findViewById(R.id.editTextColor);
        EditText editTextHobby = findViewById(R.id.editTextHobby);

        String age = editTextAge.getText().toString();
        String color = editTextColor.getText().toString();
        String hobby = editTextHobby.getText().toString();



        Map<String, Object> user = new HashMap<>();
        user.put(AGE_KEY, age);
        user.put(COLOR_KEY, color);
        user.put(HOBBY_KEY, hobby);
        user.put(PHOTO_KEY, photoURL);

        db.collection("users").document(email).set(user)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(ProfileCreation.this, "Profile Submission Successful!", Toast.LENGTH_SHORT).show();
                        startActivity(homeScreen);
                        finish();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(ProfileCreation.this, "Failed Getting Document", Toast.LENGTH_LONG).show();
                Log.d("Tag", e.toString());
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        else if (id == R.id.logout) {
            signOut();
            mAuth.signOut();
        }
        else if (id == R.id.delete) {
            db.collection("users").document(email).delete();
        }

        return super.onOptionsItemSelected(item);
    }



}
