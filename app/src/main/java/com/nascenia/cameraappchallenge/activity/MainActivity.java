package com.nascenia.cameraappchallenge.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.Camera;
import android.media.AudioFormat;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.nascenia.cameraappchallenge.R;
import com.nascenia.cameraappchallenge.fragment.CameraFragment;
import com.nascenia.cameraappchallenge.fragment.GimBallFragment;
import com.nascenia.cameraappchallenge.fragment.MeFragment;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {


    @BindView(R.id.bottom_relative_layout)
    RelativeLayout bottomRelativeLayout;

    @BindView(R.id.background_view)
    View bottomRelativeLayoutBackgroundView;

    @BindView(R.id.record_corner_imageview)
    ImageView recordCornerImageview;
    @BindView(R.id.record_imageview)
    ImageView recordImageview;

    @BindView(R.id.record_layout)
    FrameLayout recordFramelayout;

    @BindView(R.id.tvDuration)
    TextView tvDuration;

    @BindView(R.id.middle_pointer_TextView)
    TextView middlePointerTextView;

    private boolean forever = false;

    int images[] = {R.drawable.record_corner_image};
    private boolean isVisible = true;

    private static Camera mCamera = null;
    private TextureView mPreview;
    private MediaRecorder mMediaRecorder;
    private File mOutputFile;

    private boolean isRecording = false;
    private static final String TAG = "Recorder";
    private ImageView captureButton;
    private static final int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 1;

    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mPreview = (TextureView) findViewById(R.id.surface_view);
        ButterKnife.bind(this);

        sharedPreferences = getSharedPreferences("CAMERA_RESULATION_PREF", Context.MODE_PRIVATE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        
        if (sharedPreferences.getBoolean("GENERAL_1_CENTER_POINT_STATUS", false))
            middlePointerTextView.setVisibility(View.VISIBLE);
        else
            middlePointerTextView.setVisibility(View.GONE);
    }

    @OnClick(R.id.setttings_textView)
    public void showSettingsAvtivity() {
        //forever = false;
        startActivity(new Intent(MainActivity.this, SettingsActivity.class)
                .putExtra("preview_height", mPreview.getHeight())
                .putExtra("preview_width", mPreview.getWidth()));
    }

    public void onCaptureClick(View view) {

        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.CAMERA)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST_READ_CONTACTS);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        } else {
            recordVideo();
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_CONTACTS: {

                if (grantResults.length == 0
                        || grantResults[0] !=
                        PackageManager.PERMISSION_GRANTED) {

                    Log.i(TAG, "Permission has been denied by user");
                } else {

                    recordVideo();

                }
                Log.i(TAG, "Permission has been granted by user");
            }
            return;

        }
    }

    private boolean prepareVideoRecorder() {

        // BEGIN_INCLUDE (configure_preview)
        mCamera = CameraHelper.getDefaultCameraInstance();

        /*// previous code


        // We need to make sure that our preview and recording video size are supported by the
        // camera. Query camera to find all the sizes and choose the optimal size given the
        // dimensions of our preview surface.
        Camera.Parameters parameters = mCamera.getParameters();
        List<Camera.Size> mSupportedPreviewSizes = parameters.getSupportedPreviewSizes();
        List<Camera.Size> mSupportedVideoSizes = parameters.getSupportedVideoSizes();
        Camera.Size optimalSize = CameraHelper.getOptimalVideoSize(mSupportedVideoSizes,
                mSupportedPreviewSizes, mPreview.getWidth(), mPreview.getHeight());

        // Use the same size for recording profile.
        CamcorderProfile profile = CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH);
        profile.videoFrameWidth = optimalSize.width;
        profile.videoFrameHeight = optimalSize.height;
        profile.audioCodec = AudioFormat.ENCODING_PCM_16BIT;*/


        /************prepare Camera and MediaRecorder with custom attributes********************************/

        // We need to make sure that our preview and recording video size are supported by the
        // camera. Query camera to find all the sizes and choose the optimal size given the
        // dimensions of our preview surface.
        Camera.Parameters parameters = mCamera.getParameters();
        // Use the same size for recording profile.
        final CamcorderProfile profile = CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH);

        //checking is there any resulation set from  settings activity
        if (sharedPreferences.contains("RESULATION_WIDTH")) {
            profile.videoFrameWidth = sharedPreferences.getInt("RESULATION_WIDTH", 0);
            profile.videoFrameHeight = sharedPreferences.getInt("RESULATION_HEIGHT", 0);
            Log.i("res_value", "from pref " + profile.videoFrameWidth + " " + profile.videoFrameHeight);
        } else {
            List<Camera.Size> mSupportedPreviewSizes = parameters.getSupportedPreviewSizes();
            List<Camera.Size> mSupportedVideoSizes = parameters.getSupportedVideoSizes();
            Camera.Size optimalSize = CameraHelper.getOptimalVideoSize(mSupportedVideoSizes,
                    mSupportedPreviewSizes, mPreview.getWidth(), mPreview.getHeight());

            profile.videoFrameWidth = optimalSize.width;
            profile.videoFrameHeight = optimalSize.height;
            Log.i("res_value", "from defa " + profile.videoFrameWidth + " " + profile.videoFrameHeight);
        }
        profile.audioCodec = AudioFormat.ENCODING_PCM_16BIT;

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this, "Width: " + profile.videoFrameWidth + " Height: " + profile.videoFrameHeight, Toast.LENGTH_LONG).show();
            }
        });

        // likewise for the camera object itself.
        parameters.setPreviewSize(profile.videoFrameWidth, profile.videoFrameHeight);
        parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
//        parameters.setPreviewFrameRate(60);
        mCamera.setParameters(parameters);
        try {
            // Requires API level 11+, For backward compatibility use {@link setPreviewDisplay}
            // with {@link SurfaceView}
            mCamera.setPreviewTexture(mPreview.getSurfaceTexture());
            setCameraDisplayOrientation(this, 0, mCamera);
        } catch (Exception e) {
            Log.e(TAG, "Surface texture is unavailable or unsuitable" + e.getMessage());
            return false;
        }
        // END_INCLUDE (configure_preview)


        // BEGIN_INCLUDE (configure_media_recorder)
        mMediaRecorder = new MediaRecorder();

        // Step 1: Unlock and set camera to MediaRecorder
        mCamera.unlock();
        mMediaRecorder.setCamera(mCamera);

        // Step 2: Set sources
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);

//        mMediaRecorder.setVideoFrameRate(60);


        // Step 3: Set a CamcorderProfile (requires API Level 8 or higher)
        mMediaRecorder.setProfile(profile);

        // Step 4: Set output file
        mOutputFile = CameraHelper.getOutputMediaFile(CameraHelper.MEDIA_TYPE_VIDEO);
        if (mOutputFile == null) {
            return false;
        }
//        mMediaRecorder.setAudioEncoder(AudioFormat.ENCODING_PCM_16BIT);
//        mMediaRecorder.setVideoFrameRate(60);

        mMediaRecorder.setOutputFile(mOutputFile.getPath());
        // END_INCLUDE (configure_media_recorder)

        // Step 5: Prepare configured MediaRecorder
        try {
            mMediaRecorder.prepare();
        } catch (IllegalStateException e) {
            Log.d(TAG, "IllegalStateException preparing MediaRecorder: " + e.getMessage());
            releaseMediaRecorder();
            return false;
        } catch (IOException e) {
            Log.d(TAG, "IOException preparing MediaRecorder: " + e.getMessage());
            releaseMediaRecorder();
            return false;
        }
        return true;
    }

    private void recordVideo() {


        if (isRecording) {
            animate(recordCornerImageview, images, 0, false);
            forever = false;
            fadeInAndShowBottomRelativeLayout(findViewById(R.id.bottom_relative_layout));
            fadeInAndShowBottomRelativeLayout(findViewById(R.id.setttings_textView));
            fadeOutAndHideBottomRelativeLayout(findViewById(R.id.tvDuration));
            isVisible = true;
            // BEGIN_INCLUDE(stop_release_media_recorder)

            // stop recording and release camera
            try {
                mMediaRecorder.stop();  // stop the recording
            } catch (RuntimeException e) {
                // RuntimeException is thrown when stop() is called immediately after start().
                // In this case the output file is not properly constructed ans should be deleted.
                Log.d(TAG, "RuntimeException: stop() is called immediately after start()");
                //noinspection ResultOfMethodCallIgnored
                mOutputFile.delete();
            }
            releaseMediaRecorder(); // release the MediaRecorder object
            mCamera.lock();         // take camera access back from MediaRecorder

            // inform the user that recording has stopped
            isRecording = false;
            releaseCamera();
            // END_INCLUDE(stop_release_media_recorder)

        } else {
            animate(recordCornerImageview, images, 0, true);
            forever = true;
            fadeOutAndHideBottomRelativeLayout(findViewById(R.id.bottom_relative_layout));
            fadeOutAndHideBottomRelativeLayout(findViewById(R.id.setttings_textView));
            fadeInAndShowBottomRelativeLayout(findViewById(R.id.tvDuration));
            isVisible = false;

            // BEGIN_INCLUDE(prepare_start_media_recorder)
            this.runOnUiThread(new Runnable() {
                public void run() {
//                    Toast.makeText(activity, "Hello", Toast.LENGTH_SHORT).show();
                    new MediaPrepareTask().execute(null, null, null);
                }
            });

//            new MediaPrepareTask().execute(null, null, null);

            // END_INCLUDE(prepare_start_media_recorder)
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // if we are using MediaRecorder, release it first
        releaseMediaRecorder();
        // release the camera immediately on pause event
        releaseCamera();
    }

    private void releaseMediaRecorder() {
        if (mMediaRecorder != null) {
            // clear recorder configuration
            mMediaRecorder.reset();
            // release the recorder object
            mMediaRecorder.release();
            mMediaRecorder = null;
            // Lock camera for later use i.e taking it back from MediaRecorder.
            // MediaRecorder doesn't need it anymore and we will release it if the activity pauses.
            mCamera.lock();
        }
    }

    private void releaseCamera() {
        if (mCamera != null) {
            // release the camera for other applications
            mCamera.release();
            mCamera = null;
        }
    }

    private void setCameraDisplayOrientation(Activity activity,
                                             int cameraId, android.hardware.Camera camera) {
        android.hardware.Camera.CameraInfo info =
                new android.hardware.Camera.CameraInfo();
        android.hardware.Camera.getCameraInfo(cameraId, info);
        int rotation = getWindowManager().getDefaultDisplay()
                .getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }

        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;  // compensate the mirror
        } else {  // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }
        camera.setDisplayOrientation(result);
    }

    /**
     * Asynchronous task for preparing the {@link android.media.MediaRecorder} since it's a long blocking
     * operation.
     */
    class MediaPrepareTask extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... voids) {
            // initialize video camera
            if (prepareVideoRecorder()) {
                // Camera is available and unlocked, MediaRecorder is prepared,
                // now you can start recording
//                mMediaRecorder.setAudioEncoder();
                mMediaRecorder.start();
                startCal = Calendar.getInstance();
                startCal.setTimeZone(TimeZone.getTimeZone("UTC"));
                updateUI();
                isRecording = true;
            } else {
                // prepare didn't work, release the camera

                releaseMediaRecorder();
                return false;
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            seconds = 0;
            minutes = 0;
            hour = 0;
            if (!result) {
                MainActivity.this.finish();
            }
        }
    }

    long seconds = 0;
    int minutes = 0;
    int hour = 0;
    Timer timer = null;
    Calendar startCal, currentCal;

    private void updateUI() {
        timer = new Timer();

        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {


                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        currentCal = Calendar.getInstance();
                        TimeZone tz = TimeZone.getDefault();
                        currentCal.setTimeZone(TimeZone.getDefault());
                        long diff = currentCal.getTimeInMillis() - startCal.getTimeInMillis();
                        long diffSeconds = diff / 1000;
                        long diffMins = diffSeconds / 60;
                        long diffHours = diffMins / 60;
                        SimpleDateFormat dateFormat = new SimpleDateFormat("mm:ss");
                        String hours = diffHours > 9 ? diffHours + "" : ("0" + diffHours);
                        String strTime = hours + ":" + dateFormat.format(diff);

//                        seconds = System.currentTimeMillis();
                        tvDuration.setText(strTime);
//                        if (seconds%59 == 0) {
//                            tvDuration.setText(String.format("%02d", hour% 60) + ":" + String.format("%02d", minutes% 60) + ":" + String.format("%02d", seconds % 60));
//                            minutes = seconds / (59+(minutes*60));
////                            seconds = seconds % 60;
//                            hour = minutes / (59+(hour*60));
//                        }
//                        ++seconds;
//                        tvDuration.setText(String.format("%02d", hour% 60) + ":" + String.format("%02d", minutes% 60) + ":" + String.format("%02d", seconds % 60));
                    }
                });
            }
        }, 0, 1000);
    }

//    @OnClick(R.id.record_layout)
//    public void animateRecordView() {
//        forever = true;
//        animate(recordCornerImageview, images, 0, true);
//        if(isVisible) {
//            fadeOutAndHideBottomRelativeLayout(findViewById(R.id.bottom_relative_layout));
//            fadeOutAndHideBottomRelativeLayout(findViewById(R.id.setttings_textView));
//            fadeInAndShowBottomRelativeLayout(findViewById(R.id.tvDuration));
//            isVisible = false;
//        }else{
//            fadeInAndShowBottomRelativeLayout(findViewById(R.id.bottom_relative_layout));
//            fadeInAndShowBottomRelativeLayout(findViewById(R.id.setttings_textView));
//            fadeOutAndHideBottomRelativeLayout(findViewById(R.id.tvDuration));
//            isVisible = true;
//        }
//    }

    private void fadeOutAndHideBottomRelativeLayout(final View view) {
        Animation fadeOut = new AlphaAnimation(1, 0);
        fadeOut.setInterpolator(new AccelerateInterpolator());
        fadeOut.setDuration(1000);

        fadeOut.setAnimationListener(new Animation.AnimationListener() {
            public void onAnimationEnd(Animation animation) {
//                view.setBackgroundResource(0);
                view.setVisibility(View.GONE);
            }

            public void onAnimationRepeat(Animation animation) {
            }

            public void onAnimationStart(Animation animation) {
            }
        });

        view.startAnimation(fadeOut);

    }

    private void fadeInAndShowBottomRelativeLayout(final View view) {
        Animation fadeOut = new AlphaAnimation(0, 1);
        fadeOut.setInterpolator(new AccelerateInterpolator());
        fadeOut.setDuration(1000);

        fadeOut.setAnimationListener(new Animation.AnimationListener() {
            public void onAnimationEnd(Animation animation) {
//                view.setBackgroundResource();
                view.setVisibility(View.VISIBLE);
            }

            public void onAnimationRepeat(Animation animation) {
            }

            public void onAnimationStart(Animation animation) {
            }
        });

        view.startAnimation(fadeOut);

    }

    private void animate(final ImageView imageView, final int images[], final int imageIndex, final boolean forever1) {

        //imageView <-- The View which displays the images
        //images[] <-- Holds R references to the images to display
        //imageIndex <-- index of the first image to show in images[]
        //forever <-- If equals true then after the last image it starts all over again with the first image resulting in an infinite loop. You have been warned.

        int fadeInDuration = 1000; // Configure time values here
        int timeBetween = 20;
        int fadeOutDuration = 1000;

        imageView.setVisibility(View.INVISIBLE);    //Visible or invisible by default - this will apply when the animation ends
        imageView.setImageResource(images[imageIndex]);

        Animation fadeIn = new AlphaAnimation(0, 1);
        fadeIn.setInterpolator(new DecelerateInterpolator()); // add this
        fadeIn.setDuration(fadeInDuration);

        Animation fadeOut = new AlphaAnimation(1, 0);
        fadeOut.setInterpolator(new AccelerateInterpolator()); // and this
        // fadeOut.setStartOffset(fadeInDuration + timeBetween);
        fadeOut.setDuration(fadeOutDuration);

        AnimationSet animation = new AnimationSet(false); // change to false
        animation.addAnimation(fadeIn);
        animation.addAnimation(fadeOut);
        animation.setRepeatCount(1);
        imageView.setAnimation(animation);

        animation.setAnimationListener(new Animation.AnimationListener() {
            public void onAnimationEnd(Animation animation) {
                if (images.length - 1 > imageIndex) {
                    animate(imageView, images, imageIndex + 1, forever); //Calls itself until it gets to the end of the array
                } else {
                    if (forever) {
                        animate(imageView, images, 0, forever);  //Calls itself to start the animation all over again in a loop if forever = true
                    } else {
                        imageView.setVisibility(View.VISIBLE);
                    }
                }
            }

            public void onAnimationRepeat(Animation animation) {
                // TODO Auto-generated method stub
            }

            public void onAnimationStart(Animation animation) {
                // TODO Auto-generated method stub
            }
        });
    }


}
