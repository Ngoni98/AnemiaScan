package changycj.anemiascan;


import com.qualcomm.vuforia.CameraDevice;
import com.qualcomm.vuforia.Marker;
import com.qualcomm.vuforia.MarkerTracker;
import com.qualcomm.vuforia.State;
import com.qualcomm.vuforia.TrackerManager;
import com.qualcomm.vuforia.Vec2F;
import com.qualcomm.vuforia.Vuforia;

import vuforia.LoadingDialogHandler;
import vuforia.SampleApplicationControl;
import vuforia.SampleApplicationException;
import vuforia.SampleApplicationGLView;
import vuforia.SampleApplicationSession;
import android.support.v7.app.ActionBarActivity;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class CameraActivity extends ActionBarActivity implements SampleApplicationControl{
	private static final String TAG = "CameraActivity";

	SampleApplicationSession vuforiaAppSession;
	
	private Marker dataSet;
	private CameraRenderer mRenderer;
	
	boolean mIsDroidDevice = false;
	boolean mContAutofocus = false;
	private RelativeLayout mUILayout;
	private SampleApplicationGLView mGlView;
    LoadingDialogHandler loadingDialogHandler = new LoadingDialogHandler(this);

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "onCreate");
		super.onCreate(savedInstanceState);
		
		vuforiaAppSession = new SampleApplicationSession(this);
		
        startLoadingAnimation();
		
		vuforiaAppSession.initAR(this, ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		
//        mGestureDetector = new GestureDetector(this, new GestureListener());
        
        mIsDroidDevice = android.os.Build.MODEL.toLowerCase().startsWith(
            "droid");
		
	}
	
	@Override
	protected void onResume() {
		Log.d(TAG, "onResume");
		super.onResume();
		
		if (mIsDroidDevice) {
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		}
		
		try {
			vuforiaAppSession.resumeAR();
		} catch (SampleApplicationException e) {
			Log.e(TAG, e.getString());
		}
		
        // Resume the GL view:
        if (mGlView != null)
        {
            mGlView.setVisibility(View.VISIBLE);
            mGlView.onResume();
        }
	}
	
	@Override
	protected void onPause() {
		Log.d(TAG, "onPause");
		super.onPause();
		
        if (mGlView != null)
        {
            mGlView.setVisibility(View.INVISIBLE);
            mGlView.onPause();
        }
		
		try {
			vuforiaAppSession.pauseAR();
		} catch (SampleApplicationException e) {
			Log.e(TAG, e.getString());
		}
	}
	
	@Override
	protected void onDestroy() {
		Log.d(TAG, "onDestroy");
		super.onDestroy();
		
		try {
			vuforiaAppSession.stopAR();
		} catch (SampleApplicationException e) {
			Log.e(TAG, e.getString());
		}
        
        System.gc();
	}
	
	public void onConfigurationChanged(Configuration config) {
		Log.d(TAG, "onConfigurationChanged");
		super.onConfigurationChanged(config);
		
		vuforiaAppSession.onConfigurationChanged();
	}
	
	@Override
	public boolean doInitTrackers() {
		boolean result = true;
		
		TrackerManager tManager = TrackerManager.getInstance();
		
		MarkerTracker markerTracker = (MarkerTracker) tManager.initTracker(
				MarkerTracker.getClassType());
		if (markerTracker == null) {
			Log.e(TAG, "Tracker not initialized");
			result = false;
		} else {
			Log.i(TAG, "Tracker initialized");
		}
		return result;
	}

	@Override
	public boolean doLoadTrackersData() {
		TrackerManager tManager = TrackerManager.getInstance();
		MarkerTracker markerTracker = (MarkerTracker) tManager
				.getTracker(MarkerTracker.getClassType());
		if (markerTracker == null) return false;
				
		dataSet = markerTracker.createFrameMarker(0, "card", new Vec2F(100, 100));
		if (dataSet == null) {
			Log.e(TAG, "Failed to create frame marker.");
			return false;
		}
		
		Log.i(TAG, "Successfully Initialized markerTracker");
		return true;
	}

	@Override
	public boolean doStartTrackers() {
		boolean result = true;
		
		MarkerTracker markerTracker = (MarkerTracker) TrackerManager.getInstance()
				.getTracker(MarkerTracker.getClassType());
		if (markerTracker != null) {
			markerTracker.start();
		}
		return result;
	}

	@Override
	public boolean doStopTrackers() {
		boolean result = true;
		
		MarkerTracker markerTracker = (MarkerTracker) TrackerManager.getInstance().getTracker(
				MarkerTracker.getClassType());
		if (markerTracker != null) markerTracker.stop();
		
		return result;
	}

	@Override
	public boolean doUnloadTrackersData() {
		boolean result = true;
		return result;
	}

	@Override
	public boolean doDeinitTrackers() {
		boolean result = true;
		
		TrackerManager tManager = TrackerManager.getInstance();
		tManager.deinitTracker(MarkerTracker.getClassType());
		
		return result;
	}

	@Override
	public void onInitARDone(SampleApplicationException exception) {
		if (exception == null) {
			initApplicationAR();
            mRenderer.mIsActive = true;

            // Now add the GL surface view. It is important
            // that the OpenGL ES surface view gets added
            // BEFORE the camera is started and video
            // background is configured.
            addContentView(mGlView, new LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT));
            
            // Sets the UILayout to be drawn in front of the camera
            mUILayout.bringToFront();
            
            // Hides the Loading Dialog
            loadingDialogHandler
                .sendEmptyMessage(LoadingDialogHandler.HIDE_LOADING_DIALOG);
            
            // Sets the layout background to transparent
            mUILayout.setBackgroundColor(Color.TRANSPARENT);
			
			try {
				vuforiaAppSession.startAR(CameraDevice.CAMERA.CAMERA_DEFAULT);
			} catch (SampleApplicationException e) {
				Log.e(TAG, e.getString());
			}
			
			boolean result = CameraDevice.getInstance().setFocusMode(
					CameraDevice.FOCUS_MODE.FOCUS_MODE_CONTINUOUSAUTO);
			if (result) {
				mContAutofocus = true;
			} else {
				Log.e(TAG, "Unable to enable continuous autofocus");
			} 
			
		} else {
			Log.e(TAG, exception.getString());
			finish();
		}
	}

	@Override
	public void onQCARUpdate(State state) {	}
	
	private void startLoadingAnimation() {
        LayoutInflater inflater = LayoutInflater.from(this);
        mUILayout = (RelativeLayout) inflater.inflate(R.layout.camera_overlay,
            null, false);
        
        mUILayout.setVisibility(View.VISIBLE);
        mUILayout.setBackgroundColor(Color.BLACK);
        
        // Gets a reference to the loading dialog
        loadingDialogHandler.mLoadingDialogContainer = mUILayout
            .findViewById(R.id.loading_indicator);
        
        // Shows the loading indicator at start
        loadingDialogHandler
            .sendEmptyMessage(LoadingDialogHandler.SHOW_LOADING_DIALOG);
        
        // Adds the inflated layout to the view
        addContentView(mUILayout, new LayoutParams(LayoutParams.MATCH_PARENT,
            LayoutParams.MATCH_PARENT));
		
	}
	
	private void initApplicationAR() {
		int depthSize = 16;
		int stencilSize = 0;
		boolean translucent = Vuforia.requiresAlpha();
		
		mGlView = new SampleApplicationGLView(this);
        mGlView.init(translucent, depthSize, stencilSize);
        
        mRenderer = new CameraRenderer(this, vuforiaAppSession);
        mGlView.setRenderer(mRenderer);
	}
	
	public void updateHemoCount(String count, int pixel) {
        TextView text = (TextView) findViewById(R.id.hemoglobin_count);
        text.setText(count);
        text.setTextColor(pixel);
	}
}
