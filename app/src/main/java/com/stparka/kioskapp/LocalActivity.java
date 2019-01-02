package com.stparka.kioskapp;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LocalActivity extends Activity {

    private final Context context = this;
    private VideoView videoView;
    private TextView faceCounterView;

    private Dialog passwordDialog;

    private StatusBarLocker statusBarLocker;

    @Override
    public void onBackPressed() {
        hideSystemUI();
        setImmersiveMode();
    }

    @SuppressWarnings("deprecation")
    @SuppressLint({"ClickableViewAccessibility", "SetJavaScriptEnabled"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        removeTitleBar();
        doNotLockScreen();
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        statusBarLocker = new StatusBarLocker(this);
        statusBarLocker.lock();

        Intent intentLoad = new Intent()
                .setType("video/*")
                .setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intentLoad, "Select a file"), 123);

        Configuration configuration = Configuration.loadFromPreferences(context);

        String otp = configuration.getPassphrase();

        if (otp == null) {
            Intent intent = new Intent(LocalActivity.this, SettingsActivity.class);
            Toast.makeText(context, "Please setup first the One-Time-Passwords on your phone before you use the kiosk mode.", Toast.LENGTH_LONG).show();
            startActivity(intent);
            finish();
        }

    }

    private void doNotLockScreen() {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
    }

    private void removeTitleBar() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
    }


    // This snippet hides the system bars.
    private void hideSystemUI() {
        // Set the IMMERSIVE flag.
        // Set the content to appear under the system bars so that the content
        // doesn't resize when the system bars hide and show.
        View view = getWindow().getDecorView();
        view.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                        | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                        | View.SYSTEM_UI_FLAG_IMMERSIVE);
    }


    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        hideSystemUI();
        setImmersiveMode();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return blockKeys(keyCode, event);
    }

    private boolean blockKeys(int keyCode, KeyEvent event) {
        return event.isSystem();
    }

    private void askPassword() {
        passwordDialog.show();
    }

    @SuppressWarnings("deprecation")
    public static Camera getCameraInstance() {
        Camera c = null;
        try {
            c = Camera.open(Camera.CameraInfo.CAMERA_FACING_FRONT); // attempt to get a Camera instance
        } catch (Exception e) {
            // Camera is not available (in use or does not exist)
            e.printStackTrace();
        }
        return c; // returns null if camera is unavailable
    }

    private boolean checkCameraHardware(Context context) {
        return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA);
    }

    @Override
    protected void onPause() {
        super.onPause();
        ActivityManager activityManager = (ActivityManager) getApplicationContext()
                .getSystemService(Context.ACTIVITY_SERVICE);

        activityManager.moveTaskToFront(getTaskId(), 0);
    }


    @Override
    protected void onDestroy() {
        statusBarLocker.release();
        passwordDialog.dismiss();
        super.onDestroy();
    }

    @Override
    public void onResume() {
        super.onResume();
        getWindow().getDecorView().setSystemUiVisibility(
                View.GONE);
        hideSystemUI();
        setImmersiveMode();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        hideSystemUI();
        setImmersiveMode();
        return super.onTouchEvent(event);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        hideSystemUI();
        setImmersiveMode();
        return super.dispatchTouchEvent(ev);
    }

    protected void setImmersiveMode() {
        getWindow().getDecorView()
                .setSystemUiVisibility(
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                                | View.SYSTEM_UI_FLAG_FULLSCREEN
                                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                                | View.INVISIBLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    private void loadVideo(Uri selectedFile){
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        Toast.makeText(this, "Loading " + selectedFile.toString(), Toast.LENGTH_SHORT).show();
        setContentView(com.stparka.kioskapp.R.layout.activity_video);
        videoView = (VideoView)findViewById(R.id.videoview);
        hideSystemUI();
        setImmersiveMode();
        videoView.setVideoURI(selectedFile);
        videoView.setMediaController(null);
        videoView.start();
        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                videoView.start();
            }
        });

        View mDecorView = getWindow().getDecorView();
        mDecorView.setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
            @Override
            public void onSystemUiVisibilityChange(int visibility) {
                hideSystemUI();
            }
        });

        if (checkCameraHardware(this)) {

            mDecorView.setOnTouchListener(new View.OnTouchListener() {

                private long lastTouchTime;
                private int count;

                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    if (MotionEvent.ACTION_UP == motionEvent.getAction()) {
                        long touchTime = System.currentTimeMillis();
                        if (touchTime - lastTouchTime >= 400) {
                            count = 0;
                        }
                        count++;
                        if (count >= 11) {
                            askPassword();
                            count = 0;
                        }
                        lastTouchTime = touchTime;
                    }
                    return false;
                }
            });

            passwordDialog = new PasswordDialog(this, new Runnable() {
                @Override
                public void run() {
                    Intent intent = new Intent(LocalActivity.this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    finish();
                }
            });
        }
    }

    private final List blockedKeys = new ArrayList(Arrays.asList(KeyEvent.KEYCODE_VOLUME_DOWN, KeyEvent.KEYCODE_VOLUME_UP));

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (blockedKeys.contains(event.getKeyCode())) {
            return true;
        } else {
            return super.dispatchKeyEvent(event);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==123 && resultCode==RESULT_OK) {
            Uri selectedfile = data.getData(); //The uri with the location of the file
            loadVideo(selectedfile);
        }
    }
}
