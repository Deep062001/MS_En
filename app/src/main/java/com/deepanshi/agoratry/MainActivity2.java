package com.deepanshi.agoratry;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.os.Bundle;
import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.PorterDuff;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.deepanshi.agoratry.media.RtcTokenBuilder;

import java.util.HashMap;
import java.util.Hashtable;

import io.agora.rtc.Constants;
import io.agora.rtc.IRtcEngineEventHandler;
import io.agora.rtc.RtcEngine;
import io.agora.rtc.video.VideoCanvas;
import io.agora.rtc.video.VideoEncoderConfiguration;

import static android.view.View.generateViewId;
import static com.deepanshi.agoratry.media.RtcTokenBuilder.Role.Role_Publisher;


public class MainActivity2 extends AppCompatActivity {


    private HashMap<Integer,Integer>user_ids=new HashMap<Integer,Integer>();  //dynamic
    private String agora_app_id="2c163c26938b4981834fd3646db98a84";
    private String agora_app_certificate="786c431b153d42c8996b72b55b937f3e";
    private String channel_name;
    private Integer UID;
    String token;
    boolean mMuted=true;
    LinearLayout user_frames;

    private RtcEngine mRtcEngine;
    // Permissions
    private static final int PERMISSION_REQ_ID = 22;
    private static final String[] REQUESTED_PERMISSIONS = {Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA};

    private static final String LOG_TAG = MainActivity2.class.getSimpleName();
   // static final VideoEncoderConfiguration.VideoDimensions VD_1920x1080 = new VideoEncoderConfiguration.VideoDimensions(1920, 1080);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        channel_name=getIntent().getStringExtra("CHANNEL_NAME");
        UID=getIntent().getIntExtra("UID",0);

        RtcTokenBuilder result=new RtcTokenBuilder();
        token=result.buildTokenWithUid(agora_app_id,agora_app_certificate,channel_name,0, Role_Publisher,0);

        if(checkSelfPermission(REQUESTED_PERMISSIONS[0], PERMISSION_REQ_ID) &&
                checkSelfPermission(REQUESTED_PERMISSIONS[1], PERMISSION_REQ_ID)){
            initAgoraEngine();
        }
      //  findViewById(R.id.bg_video_container).setVisibility(View.GONE);  //mine
        findViewById(R.id.audioBtn).setVisibility(View.GONE);
        findViewById(R.id.leaveBtn).setVisibility(View.GONE);
        findViewById(R.id.videoBtn).setVisibility(View.GONE);

    }

    public boolean checkSelfPermission(String permission, int requestCode) {
        if (ContextCompat.checkSelfPermission(this,
                permission)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    REQUESTED_PERMISSIONS,
                    requestCode);
            return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.i(LOG_TAG, "onRequestPermissionsResult " + grantResults[0] + " " + requestCode);

        switch (requestCode) {
            case PERMISSION_REQ_ID:
                if (grantResults[0] != PackageManager.PERMISSION_GRANTED || grantResults[1] != PackageManager.PERMISSION_GRANTED) {
                    Log.i(LOG_TAG, "Need permissions " + Manifest.permission.RECORD_AUDIO + "/" + Manifest.permission.CAMERA);
                    return;
                }
                // if permission granted, initialize the engine
                initAgoraEngine();
                break;
        }
    }

    private void initAgoraEngine() {
        try {
            mRtcEngine = RtcEngine.create(getBaseContext(), agora_app_id, mRtcEventHandler);
        } catch (Exception e) {
            Log.e(LOG_TAG, Log.getStackTraceString(e));

            throw new RuntimeException("NEED TO check rtc sdk init fatal error\n" + Log.getStackTraceString(e));
        }
        setupSession();
    }

    private void setupSession() {
        mRtcEngine.setChannelProfile(Constants.CHANNEL_PROFILE_COMMUNICATION);

        mRtcEngine.enableVideo();

        mRtcEngine.setVideoEncoderConfiguration(new VideoEncoderConfiguration(VideoEncoderConfiguration.VD_1280x720, VideoEncoderConfiguration.FRAME_RATE.FRAME_RATE_FPS_30,
                VideoEncoderConfiguration.STANDARD_BITRATE,
                VideoEncoderConfiguration.ORIENTATION_MODE.ORIENTATION_MODE_FIXED_PORTRAIT));

    }

    private void setupLocalVideoFeed() {
        FrameLayout videoContainer = findViewById(R.id.my_video_container);
        SurfaceView videoSurface = RtcEngine.CreateRendererView(getBaseContext());
        videoSurface.setZOrderMediaOverlay(true);
        videoContainer.addView(videoSurface);
        mRtcEngine.setupLocalVideo(new VideoCanvas(videoSurface, VideoCanvas.RENDER_MODE_FILL, UID));
        //mine
       // mRtcEngine.startPreview();

    }

    private void setupRemoteVideoStream(int uid) {
        //[dynamic frames]
        user_frames=findViewById(R.id.user_frames);
        FrameLayout user_f=new FrameLayout(this);
        user_f.setLayoutParams(new FrameLayout.LayoutParams(576,1024));
        int Id=generateViewId();
        user_f.setId(Id);
        user_ids.put(uid,Id);
        user_frames.addView(user_f);
        findViewById(Id).setVisibility(View.VISIBLE);
        user_f.setElevation(10);
//       [/frames dynamic]


        findViewById(Id).setVisibility(View.VISIBLE);    //dynamic
        FrameLayout videoContainer = findViewById(Id);     //dynamic
        SurfaceView videoSurface = RtcEngine.CreateRendererView(getBaseContext());
        videoContainer.addView(videoSurface);
        mRtcEngine.setupRemoteVideo(new VideoCanvas(videoSurface, VideoCanvas.RENDER_MODE_FILL, uid));
        mRtcEngine.setRemoteSubscribeFallbackOption(io.agora.rtc.Constants.STREAM_FALLBACK_OPTION_AUDIO_ONLY);
    }


    // Handle SDK Events
    private final IRtcEngineEventHandler mRtcEventHandler = new IRtcEngineEventHandler() {
        @Override
        public void onUserJoined(final int uid, int elapsed) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
//                    mRtcEngine.setVideoEncoderConfiguration(new VideoEncoderConfiguration(VideoEncoderConfiguration.VD_480x480, VideoEncoderConfiguration.FRAME_RATE.FRAME_RATE_FPS_30,
//                            VideoEncoderConfiguration.STANDARD_BITRATE,
//                            VideoEncoderConfiguration.ORIENTATION_MODE.ORIENTATION_MODE_FIXED_PORTRAIT));
                    setupRemoteVideoStream(uid);
                }
            });
        }

        // remote user has left channel
        @Override
        public void onUserOffline(int uid, int reason) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    onRemoteUserLeft(uid);
                }
            });
        }

        // remote user has toggled their video
        @Override
        public void onRemoteVideoStateChanged(final int uid, final int state, int reason, int elapsed) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    onRemoteUserVideoToggle(uid, state);
                }
            });
        }
    };

    private void onRemoteUserVideoToggle(int uid, int state) {
        int Id=user_ids.get(uid);
        FrameLayout videoContainer = findViewById(Id);//dynamic chnge

        SurfaceView videoSurface = (SurfaceView) videoContainer.getChildAt(0);
        videoSurface.setVisibility(state == 0 ? View.GONE : View.VISIBLE);

        // add an icon to let the other user know remote video has been disabled
        if(state == 0){
            ImageView noCamera = new ImageView(this);
            noCamera.setImageResource(R.drawable.video_disabled);
            videoContainer.addView(noCamera);
        } else {
            ImageView noCamera = (ImageView) videoContainer.getChildAt(1);
            if(noCamera != null) {
                videoContainer.removeView(noCamera);
            }
        }
    }

    private void onRemoteUserLeft(int uid) {
//        int Id=user_ids.get(uid);//mine
//        findViewById(uid).setVisibility(View.GONE); //mine
        removeVideo(uid);
    }

    private void removeVideo(int containerID) {
        FrameLayout videoContainer = findViewById(containerID);
        videoContainer.removeAllViews();
    }

    public void onjoinChannelClicked(View view) {


        mRtcEngine.joinChannel(token, channel_name, null, UID);//uid changed
        setupLocalVideoFeed();



        findViewById(R.id.joinBtn).setVisibility(View.GONE); // set the join button hidden
        findViewById(R.id.audioBtn).setVisibility(View.VISIBLE); // set the audio button hidden
        findViewById(R.id.leaveBtn).setVisibility(View.VISIBLE); // set the leave button hidden
        findViewById(R.id.videoBtn).setVisibility(View.VISIBLE); // set the video button hidden


    }

    public void onLeaveChannelClicked(View view) {
        leaveChannel();
        removeVideo(R.id.my_video_container);
      //  removeVideo(R.id.bg_video_container);
        ///
        //mRtcEngine.stopPreview();

        findViewById(R.id.joinBtn).setVisibility(View.VISIBLE); // set the join button visible
        findViewById(R.id.audioBtn).setVisibility(View.GONE); // set the audio button hidden
        findViewById(R.id.leaveBtn).setVisibility(View.GONE); // set the leave button hidden
        findViewById(R.id.videoBtn).setVisibility(View.GONE); // set the video button hidden
        findViewById(R.id.Dis_audioBtn).setVisibility(View.GONE);//mine
    }

    private void leaveChannel() {
        mRtcEngine.leaveChannel();
        Intent i= new Intent(this,MainActivity.class);
        startActivity(i);
    }


    public void onVideoMuteClicked(View view) {

    }

    public void onAudioMuteClicked(View view) {
        mMuted = !mMuted;
        ImageView mute=findViewById(R.id.audioBtn);
        ImageView unmute=findViewById(R.id.Dis_audioBtn);
        if(mMuted){
            mute.setVisibility(mute.GONE);
            unmute.setVisibility(unmute.VISIBLE);
        }
        else{
            unmute.setVisibility(unmute.GONE);
            mute.setVisibility(mute.VISIBLE);
        }
        mRtcEngine.muteLocalAudioStream(mMuted);
    }
}