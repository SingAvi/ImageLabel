package com.singavi.imagelabel;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.cloud.FirebaseVisionCloudDetectorOptions;
import com.google.firebase.ml.vision.cloud.label.FirebaseVisionCloudLabel;
import com.google.firebase.ml.vision.cloud.label.FirebaseVisionCloudLabelDetector;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.label.FirebaseVisionLabel;
import com.google.firebase.ml.vision.label.FirebaseVisionLabelDetector;
import com.google.firebase.ml.vision.label.FirebaseVisionLabelDetectorOptions;
import com.singavi.imagelabelling.Helper.InternetConnection;
import com.wonderkiln.camerakit.CameraKitError;
import com.wonderkiln.camerakit.CameraKitEvent;
import com.wonderkiln.camerakit.CameraKitEventListener;
import com.wonderkiln.camerakit.CameraKitImage;
import com.wonderkiln.camerakit.CameraKitVideo;
import com.wonderkiln.camerakit.CameraView;


import java.util.List;

import dmax.dialog.SpotsDialog;

public class MainActivity extends AppCompatActivity {

    CameraView cameraView;
    Button Snap;
    android.app.AlertDialog alertDialog;

    @Override
    protected void onResume() {
        super.onResume();
        cameraView.start();

    }

    @Override
    protected void onPause()
    {
        super.onPause();
        cameraView.stop();

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        Snap = findViewById(R.id.btn_detect);
        cameraView = findViewById(R.id.cameraView);

        alertDialog = new SpotsDialog.Builder().setMessage("wait")
                .setContext(this)
        .setCancelable(false).build();

        Snap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cameraView.start();
                cameraView.captureImage();
            }
        });

        cameraView.addCameraKitListener(new CameraKitEventListener() {
            @Override
            public void onEvent(CameraKitEvent cameraKitEvent) {

            }

            @Override
            public void onError(CameraKitError cameraKitError) {

            }

            @Override
            public void onImage(CameraKitImage cameraKitImage) {
                    alertDialog.show();
                Bitmap bitmap = cameraKitImage.getBitmap();
                bitmap = Bitmap.createScaledBitmap(bitmap,cameraView.getWidth(),cameraView.getHeight(),false);
                cameraView.stop();

                runDetector(bitmap);

            }

            @Override
            public void onVideo(CameraKitVideo cameraKitVideo) {

            }
        });

    }

    private void runDetector(Bitmap bitmap) {

        final FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(bitmap);

        new com.singavi.imagelabelling.Helper.InternetConnection(new InternetConnection.Consumer() {
            @Override
            public void accept(boolean internet) {
                    if(internet)
                    {
                        FirebaseVisionCloudDetectorOptions options =   new FirebaseVisionCloudDetectorOptions.Builder()
                                                                            .setMaxResults(1)
                                                                            .build();


                        FirebaseVisionCloudLabelDetector detector = FirebaseVision.getInstance().getVisionCloudLabelDetector(options);

                            detector.detectInImage(image)
                                    .addOnSuccessListener(new OnSuccessListener<List<FirebaseVisionCloudLabel>>() {
                                        @Override
                                        public void onSuccess(List<FirebaseVisionCloudLabel> firebaseVisionCloudLabels) {

                                                processDataResultCloud(firebaseVisionCloudLabels);
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.d("Error",e.getMessage());
                                        }
                                    });
                    }

                    else
                    {
                        FirebaseVisionLabelDetectorOptions options =   new FirebaseVisionLabelDetectorOptions.Builder()
                                .setConfidenceThreshold(0.8f)
                                .build();


                        FirebaseVisionLabelDetector detector = FirebaseVision.getInstance().getVisionLabelDetector(options);

                        detector.detectInImage(image)
                               .addOnSuccessListener(new OnSuccessListener<List<FirebaseVisionLabel>>() {
                                   @Override
                                   public void onSuccess(List<FirebaseVisionLabel> firebaseVisionLabels) {
                                       processDataResult(firebaseVisionLabels);
                                   }
                               })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.d("Error",e.getMessage());
                                    }
                                });
                    }
            }
        });


    }

    private void processDataResultCloud(List<FirebaseVisionCloudLabel> firebaseVisionCloudLabels) {

        for (FirebaseVisionCloudLabel label :firebaseVisionCloudLabels)
        {
            Toast.makeText(this, "Cloud Result"+label.getLabel(), Toast.LENGTH_SHORT).show();
        }
        if (alertDialog.isShowing())
                alertDialog.dismiss();
    }

    private void processDataResult(List<FirebaseVisionLabel> firebaseVisionCloudLabels) {

        for (FirebaseVisionLabel label :firebaseVisionCloudLabels)
        {
            Toast.makeText(this, "Cloud Result"+label.getLabel(), Toast.LENGTH_SHORT).show();
        }
        if (alertDialog.isShowing())
                alertDialog.dismiss();
    }


}
