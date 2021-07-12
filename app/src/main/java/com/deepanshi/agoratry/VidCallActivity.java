package com.deepanshi.agoratry;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.os.Bundle;
import android.Manifest;
import android.content.pm.PackageManager;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.deepanshi.agoratry.media.RtcTokenBuilder;

import java.util.HashMap;
import java.util.Map;

import io.agora.rtc.Constants;
import io.agora.rtc.IRtcEngineEventHandler;
import io.agora.rtc.RtcEngine;
import io.agora.rtc.video.VideoCanvas;
import io.agora.rtc.video.VideoEncoderConfiguration;

import static android.view.View.generateViewId;
import static com.deepanshi.agoratry.media.RtcTokenBuilder.Role.Role_Publisher;


public class VidCallActivity extends AppCompatActivity {

    //[Hashmap to maintain ID's of dynamic frames that are created when a user joins the room
    // corresponding to the automatic unique uid generated by Agora SDK for each user in the same channel
    // so that video toggle effects can be performed
    // on that particular frame]
    private HashMap<Integer,Integer>user_ids=new HashMap<Integer,Integer>();

    private String agora_app_id="2c163c26938b4981834fd3646db98a84";
    private String agora_app_certificate="786c431b153d42c8996b72b55b937f3e";
    private String channel_name;
   // private Integer UID;
    String token;

    boolean mAudioUnMuted=true;
    boolean mVideoUnMuted=true;
    boolean mFrontCamera=true;

    private RtcEngine mRtcEngine;
    // [Permissions]
    private static final int PERMISSION_REQ_ID = 22;
    private static final String[] REQUESTED_PERMISSIONS = {Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA};
    // [/Permissions]

    private static final String LOG_TAG = VidCallActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vid_call);

        channel_name=getIntent().getStringExtra("CHANNEL_NAME");
       // UID=getIntent().getIntExtra("UID",0);

        RtcTokenBuilder result=new RtcTokenBuilder();
        token=result.buildTokenWithUid(agora_app_id,agora_app_certificate,channel_name,0, Role_Publisher,0);

        if(checkSelfPermission(REQUESTED_PERMISSIONS[0], PERMISSION_REQ_ID) &&
                checkSelfPermission(REQUESTED_PERMISSIONS[1], PERMISSION_REQ_ID)){
            initAgoraEngine();
        }
        visibility_gone(R.id.leaveBtn);
        visibility_gone(R.id.audioBtn);
        visibility_gone(R.id.Dis_audioBtn);
        visibility_gone(R.id.videoBtn);
        visibility_gone(R.id.Dis_videoBtn);
        visibility_gone(R.id.front_camera);
        visibility_gone(R.id.back_camera);

    }

    private void visibility_gone(int Id) {
        findViewById(Id).setVisibility(View.GONE);
    }

    private void visibility_visible(int Id) {
        findViewById(Id).setVisibility(View.VISIBLE);
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
                // [if permission granted, initialize the engine]
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

        mRtcEngine.setVideoEncoderConfiguration(new VideoEncoderConfiguration(VideoEncoderConfiguration.VD_1280x720,
                VideoEncoderConfiguration.FRAME_RATE.FRAME_RATE_FPS_30,
                VideoEncoderConfiguration.STANDARD_BITRATE,
                VideoEncoderConfiguration.ORIENTATION_MODE.ORIENTATION_MODE_FIXED_PORTRAIT));

    }

    private void setupLocalVideoFeed() {
        FrameLayout videoContainer = findViewById(R.id.my_video_container);
        SurfaceView videoSurface = RtcEngine.CreateRendererView(getBaseContext());
        videoSurface.setZOrderMediaOverlay(true);
        videoContainer.addView(videoSurface);
        mRtcEngine.setupLocalVideo(new VideoCanvas(videoSurface,
                VideoCanvas.RENDER_MODE_FILL,
                0));

    }

    private void setupRemoteVideoStream(int uid) {

          int Id=create_frame(uid);

            visibility_visible(Id);
            FrameLayout videoContainer = findViewById(Id);
            SurfaceView videoSurface = RtcEngine.CreateRendererView(getBaseContext());
            videoSurface.setZOrderMediaOverlay(true);
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
                    setupRemoteVideoStream(uid);
                }
            });
        }

        // [remote user has left channel]
        @Override
        public void onUserOffline(int uid, int reason) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    onRemoteUserLeft(uid);
                }
            });
        }

        // [remote user has toggled their video]
        @Override
        public void onRemoteVideoStateChanged(final int uid, final int state, int reason, int elapsed) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(reason!=7) {
                        onRemoteUserVideoToggle(uid, state);
                    }
                }
            });
        }

    };



    private void onRemoteUserVideoToggle(int uid, int state) {
        int Id=user_ids.get(uid);
        FrameLayout videoContainer = findViewById(Id); //dynamic change

        SurfaceView videoSurface = (SurfaceView) videoContainer.getChildAt(0);
        videoSurface.setVisibility((state == 0)? View.GONE : View.VISIBLE);

        // [ icon to let the other user know remote video has been disabled ]
        if(state == 0){
            ImageView noCamera = new ImageView(this);
            noCamera.setBackgroundColor(getColor(R.color.Dark_Gray));
            noCamera.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
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
        int Id=user_ids.get(uid);
        user_ids.remove(uid);
        removeVideo(uid);
    }

    private void removeVideo(int containerID) {

        FrameLayout videoContainer = findViewById(containerID);
        videoContainer.removeAllViews();
    }

    public void onjoinChannelClicked(View view) {

        mRtcEngine.joinChannel(token, channel_name, null, 0);//uid changed
        setupLocalVideoFeed();


        visibility_gone(R.id.joinBtn);
        visibility_visible(R.id.leaveBtn);
        visibility_visible(R.id.audioBtn);
        visibility_visible(R.id.videoBtn);
        visibility_visible(R.id.front_camera);

    }

    public void onLeaveChannelClicked(View view) {
        leaveChannel();
        Button b=findViewById(R.id.joinBtn);
        b.setText("Join Again");
        removeVideo(R.id.my_video_container);

        // [ removing the video of all the other users in the room ]
        for(Map.Entry m: user_ids.entrySet()){
            removeVideo((int)m.getValue());
        }


        visibility_visible(R.id.joinBtn);

        visibility_gone(R.id.leaveBtn);
        visibility_gone(R.id.audioBtn);
        visibility_gone(R.id.Dis_audioBtn);
        visibility_gone(R.id.videoBtn);
        visibility_gone(R.id.Dis_videoBtn);
        visibility_gone(R.id.front_camera);
        visibility_gone(R.id.back_camera);

    }

    private void leaveChannel() {

        mRtcEngine.leaveChannel();
    }


    public void onVideoMuteClicked(View view) {
        mVideoUnMuted=!mVideoUnMuted;

        if(mVideoUnMuted){
            visibility_gone(R.id.videoBtn);
            visibility_visible(R.id.Dis_videoBtn);
        }else{
            visibility_gone(R.id.Dis_videoBtn);
            visibility_visible(R.id.videoBtn);
        }

        mRtcEngine.muteLocalVideoStream(mVideoUnMuted);

    }

    public void onAudioMuteClicked(View view) {
        mAudioUnMuted = !mAudioUnMuted;

        if(mAudioUnMuted){
            visibility_gone(R.id.audioBtn);
            visibility_visible(R.id.Dis_audioBtn);
        }
        else{
            visibility_gone(R.id.Dis_audioBtn);
            visibility_visible(R.id.audioBtn);
        }
        mRtcEngine.muteLocalAudioStream(mAudioUnMuted);
    }

    public void Switch_Camera(View view) {
        mFrontCamera=!mFrontCamera;

        if(mFrontCamera){
            visibility_gone(R.id.back_camera);
            visibility_visible(R.id.front_camera);
        }
        else{
            visibility_gone(R.id.front_camera);
            visibility_visible(R.id.back_camera);
        }
        mRtcEngine.switchCamera();

    }

    private int create_frame(int uid) {
        // [dynamic frames]
        LinearLayout user_frames = findViewById(R.id.user_frames);
        FrameLayout user_f = new FrameLayout(this);

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(getResources().getDimensionPixelSize(R.dimen.user_frame_width), getResources().getDimensionPixelSize(R.dimen.user_frame_height));
        params.setMargins(0, 0, 20, 0);
        user_f.setLayoutParams(params);
        int Id = generateViewId();
        user_f.setId(Id);
        user_ids.put(uid, Id);
        user_frames.addView(user_f);
        findViewById(Id).setVisibility(View.VISIBLE);
        user_f.setElevation(10);
        // [/frames dynamic]
        return Id;

    }
}