package com.photo.draz.photoshopper;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.design.widget.Snackbar;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.tbruyelle.rxpermissions.RxPermissions;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.List;
import android.util.Log;

import clarifai2.api.ClarifaiBuilder;
import clarifai2.api.ClarifaiClient;
import clarifai2.api.ClarifaiResponse;
import clarifai2.dto.input.ClarifaiInput;
import clarifai2.dto.input.image.ClarifaiImage;
import clarifai2.dto.model.ConceptModel;
import clarifai2.dto.model.output.ClarifaiOutput;
import clarifai2.dto.prediction.Concept;
import okhttp3.OkHttpClient;
import rx.functions.Action1;

public class MainActivity extends AppCompatActivity {

    ImageView IV;
    TextView tv;
    Button B;
    int ctr=0,ctr2=0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);



        setContentView(R.layout.activity_main);
        ImageButton IB = (ImageButton)findViewById(R.id.imageButton);
        B= (Button)findViewById(R.id.PermissionButton);
        B.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*
                Intent intent = new Intent(this, recognizeImage.class);

                Toast.makeText(this, "Running the API", Toast.LENGTH_SHORT).show();
                startActivity(intent);
                                      //    ---->>call clarifai activity here
                */
                if(ctr2==0){ctr2++;
                    tv.setText("ctr2 is now="+ctr2);}
                else
                letsDoIt();


            }
        });
        tv = (TextView)findViewById(R.id.textView);
        IV = (ImageView)findViewById(R.id.imageView);
        IB.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v)
            {
                if(ctr==0) {
                    askpermission();
                    ctr++;
                }
                else
                dispatchTakePictureIntent(v);
            }
        });






    }
    String msg = "Android : ";
    void letsDoIt()
    {
    Log.d(msg,"start of letsdo it");
        Bitmap bmp = BitmapFactory.decodeResource(getResources(), R.drawable.cameraicon);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] byteArray = stream.toByteArray();

      //  Log.d(msg, "The onStop() event");
        Log.d(msg,"byte array created, now going to call onImagePicked");
        onImagePicked(byteArray);


    }


    void askpermission()
    {

        //to ask for storage permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            /*
            RxPermissions.getInstance(this)
                    .request(Manifest.permission.READ_EXTERNAL_STORAGE)
                    .subscribe(new Action1<Boolean>() {
                        @Override public void call(Boolean granted) {
                            if (!granted) {
                                new AlertDialog.Builder(MainActivity.this)
                                        .setCancelable(false)
                                        .setMessage("Permission for Storing not granted, please grant permission for app to funciton")
                                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                            @Override public void onClick(DialogInterface dialog, int which) {
                                                moveTaskToBack(true);
                                                finish();
                                            }
                                        })
                                        .show();
                            }
                        }
                    });
           */
            RxPermissions.getInstance(this)
                    .request(Manifest.permission.CAMERA)
                    .subscribe();




        }

    }



    private void onImagePicked(@NonNull final byte[] imageBytes) {
        // Now we will upload our image to the Clarifai API
        //setBusy(true);


        final ClarifaiClient client = new ClarifaiBuilder("C2frOe2W6656ccNjgZfEqXs0M4qPWZTxzJ6FBdwY", "jKGnryRLfxCwqI2EoXKzje4R1PMrrda3KOQDjnaI").buildSync();




        // Make sure we don't show a list of old concepts while the image is being uploaded
        //adapter.setData(Collections.<Concept>emptyList());
    Log.d(msg,"inside the image picked Creating App object");

        final App ap = new App();
       // ap.onCreate();
        Log.d(msg,"obj created ofAPP");
        new AsyncTask<Void, Void, ClarifaiResponse<List<ClarifaiOutput<Concept> > > >() {
            @Override protected ClarifaiResponse<List <ClarifaiOutput <Concept> > > doInBackground(Void... params) {
                // The default Clarifai model that identifies concepts in images
                final ConceptModel generalModel = ap.clarifaiClient().getDefaultModels().generalModel();

                // Use this model to predict, with the image that the user just selected as the input
                return generalModel.predict()
                        .withInputs(ClarifaiInput.forImage(ClarifaiImage.of(imageBytes)))
                        .executeSync();
            }

            @Override protected void onPostExecute(ClarifaiResponse<List<ClarifaiOutput<Concept>>> response) {
//                setBusy(false);
                if (!response.isSuccessful()) {
                    showErrorSnackbar(R.string.clarifai_id);
                    return;
                }
                final List<ClarifaiOutput<Concept>> predictions = response.get();
                if (predictions.isEmpty()) {
                    showErrorSnackbar(R.string.clarifai_secret);
                    return;
                }
                //adapter.setData(predictions.get(0).data());

                List<Concept> concepts=predictions.get(0).data();



                final Concept concept = concepts.get(0);
                tv.setText(concept.name() != null ? concept.name() : concept.id());
                // tv2.setText(String.valueOf(concept.value()));             not printing the probability right now


                // tv.setText(predictions);
                IV.setImageBitmap(BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length));
            }

            private void showErrorSnackbar(@StringRes int errorString) {
                /*Snackbar.make(
                        this,
                        errorString,
                        Snackbar.LENGTH_INDEFINITE
                ).show();
                */
            }
        }.execute();
        Log.d(msg,"execute done");
    }




/*

    private void dispatchTakePictureIntent(View v) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photofile = null;
            try {
                photofile = CreateFile();
            } catch (IOException e) {
                tv.setText("EXCEPTION");
            }

            if (photofile != null) {
                tv.setText("NOT EXCEP");
                Uri photoURI = FileProvider.getUriForFile(this, "com.photo.draz.fileprovider", photofile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);


            }

            galleryAddPic();

        }
    }

    private void galleryAddPic() {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(mCurrentPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
    }




    String mCurrentPhotoPath;
    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int REQUEST_TAKE_PHOTO = 1;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            IV.setImageBitmap(imageBitmap);
        }
    }
    private File CreateFile() throws IOException {
        String name = "picccc";

        File StorageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(name, ".jpg", StorageDir);

        mCurrentPhotoPath = image.getAbsolutePath();
        return image;

    }

*/








}
