package changycj.anemiascan;

import java.io.IOException;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.util.Vector;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import vuforia.CubeShaders;
import vuforia.LoadingDialogHandler;
import vuforia.SampleApplication3DModel;
import vuforia.SampleApplicationSession;
import vuforia.SampleUtils;
import vuforia.Teapot;
import vuforia.Texture;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.TextView;

import com.qualcomm.vuforia.CameraCalibration;
import com.qualcomm.vuforia.CameraDevice;
import com.qualcomm.vuforia.Frame;
import com.qualcomm.vuforia.Image;
import com.qualcomm.vuforia.Marker;
import com.qualcomm.vuforia.MarkerResult;
import com.qualcomm.vuforia.MarkerTracker;
import com.qualcomm.vuforia.Matrix34F;
import com.qualcomm.vuforia.Matrix44F;
import com.qualcomm.vuforia.Renderer;
import com.qualcomm.vuforia.State;
import com.qualcomm.vuforia.Tool;
import com.qualcomm.vuforia.Trackable;
import com.qualcomm.vuforia.TrackableResult;
import com.qualcomm.vuforia.VIDEO_BACKGROUND_REFLECTION;
import com.qualcomm.vuforia.Vec2F;
import com.qualcomm.vuforia.Vec3F;
import com.qualcomm.vuforia.Vuforia;

public class CameraRenderer implements GLSurfaceView.Renderer
{
    private static final String LOGTAG = "CameraRenderer";
    
    SampleApplicationSession vuforiaAppSession;
    CameraActivity mActivity;
    
    public boolean mIsActive = false;
        
    // OpenGL ES 2.0 specific:
    private int shaderProgramID = 0;
    private int vertexHandle = 0;
    private int normalHandle = 0;
    private int textureCoordHandle = 0;
    private int mvpMatrixHandle = 0;
    private int texSampler2DHandle = 0;
    
	Handler activityHandler = new Handler(Looper.getMainLooper());
    
    
    public CameraRenderer(CameraActivity activity,
        SampleApplicationSession session)
    {
        mActivity = activity;
        vuforiaAppSession = session;
    }
    
    
    // Called when the surface is created or recreated.
    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config)
    {
        Log.d(LOGTAG, "GLRenderer.onSurfaceCreated");
        
        // Call function to initialize rendering:
        initRendering();
        
        // Call Vuforia function to (re)initialize rendering after first use
        // or after OpenGL ES context was lost (e.g. after onPause/onResume):
        vuforiaAppSession.onSurfaceCreated();
    }
    
    
    // Called when the surface changed size.
    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height)
    {
        Log.d(LOGTAG, "GLRenderer.onSurfaceChanged");
        
        // Call Vuforia function to handle render surface size changes:
        vuforiaAppSession.onSurfaceChanged(width, height);
    }
    
    
    // Called to draw the current frame.
    @Override
    public void onDrawFrame(GL10 gl)
    {
        if (!mIsActive) {
        	return;
        }
        
        // Call our function to render content
        renderFrame();
    }
    
    
    private void initRendering()
    {
        Log.d(LOGTAG, "initRendering");
        
        // Define clear color
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, Vuforia.requiresAlpha() ? 0.0f
            : 1.0f);
        
        
        shaderProgramID = SampleUtils.createProgramFromShaderSrc(
            CubeShaders.CUBE_MESH_VERTEX_SHADER,
            CubeShaders.CUBE_MESH_FRAGMENT_SHADER);
        
        vertexHandle = GLES20.glGetAttribLocation(shaderProgramID,
            "vertexPosition");
        normalHandle = GLES20.glGetAttribLocation(shaderProgramID,
            "vertexNormal");
        textureCoordHandle = GLES20.glGetAttribLocation(shaderProgramID,
            "vertexTexCoord");
        mvpMatrixHandle = GLES20.glGetUniformLocation(shaderProgramID,
            "modelViewProjectionMatrix");
        texSampler2DHandle = GLES20.glGetUniformLocation(shaderProgramID,
            "texSampler2D");
    }
    
    
    private void renderFrame()
    {
        // Clear color and depth buffer
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        
        // Get the state from Vuforia and mark the beginning of a rendering
        // section
        State state = Renderer.getInstance().begin();
        
        // Explicitly render the Video Background
        Renderer.getInstance().drawVideoBackground();
        
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        
        // We must detect if background reflection is active and adjust the
        // culling direction.
        // If the reflection is active, this means the post matrix has been
        // reflected as well,
        // therefore standard counter clockwise face culling will result in
        // "inside out" models.
        GLES20.glEnable(GLES20.GL_CULL_FACE);
        GLES20.glCullFace(GLES20.GL_BACK);
        if (Renderer.getInstance().getVideoBackgroundConfig().getReflection() == VIDEO_BACKGROUND_REFLECTION.VIDEO_BACKGROUND_REFLECTION_ON)
            GLES20.glFrontFace(GLES20.GL_CW);  // Front camera
        else
            GLES20.glFrontFace(GLES20.GL_CCW);   // Back camera

        // Did we find any trackables this frame?
        int tNum = state.getNumTrackableResults();
        final String message;
        final int pixel;
        
        // 1 diagnostic card found
        if (tNum == 1) {
        	
        	// get trackables
        	TrackableResult result = state.getTrackableResult(0);
        	Marker trackable = (Marker) result.getTrackable();
        	
        	// get frame image into bitmap
        	Bitmap cameraBitmap = getCameraBitmap(state);
        	
        	// calculate region to analyze
        	Vec3F cardCenterScene = new Vec3F(0, 0, 0);
        	CameraCalibration calib = CameraDevice.getInstance().getCameraCalibration();
            Matrix34F pose = result.getPose();
            Vec2F cardCenter = Tool.projectPoint(calib, pose, cardCenterScene);

        	// withdraw colors from the region
            float[] center = cardCenter.getData();
            pixel = cameraBitmap.getPixel(Math.round(center[0]), Math.round(center[1]));
            
        	// show on screen
            message = String.format("%.3f, %.3f -- %d", center[0], center[1], Color.red(pixel));
            
            
        	SampleUtils.checkGLError("FrameMarkers render frame");
        } else {       	
        	message = "LALALLALALLALA";
        	pixel = 0;
        }
        
    	activityHandler.post(new Runnable() {
    		public void run() {
    			mActivity.updateHemoCount(message, pixel);
    		}
    	});
        
        GLES20.glDisable(GLES20.GL_DEPTH_TEST);
        Renderer.getInstance().end();
        
    }
    
    private Bitmap getCameraBitmap(State state) {
    	final int RGB565_FORMAT = 1;
    	
    	// get image
    	Image image = null;
    	Frame frame = state.getFrame();
    	for (int i = 0; i < frame.getNumImages(); i++) {
    		image = frame.getImage(i);
    		if (image.getFormat() == RGB565_FORMAT) {
    			break;
    		}
    	}
    	
    	if (image != null) {
    		ByteBuffer buffer = image.getPixels();
    		Bitmap bitmap = Bitmap.createBitmap(image.getWidth(), image.getHeight(),
    				Bitmap.Config.RGB_565);
    		bitmap.copyPixelsFromBuffer(buffer);
    		return bitmap;
    		
    	} else {
    		Log.e(LOGTAG, "image not found.");
    	}
    	return null;
    }
}