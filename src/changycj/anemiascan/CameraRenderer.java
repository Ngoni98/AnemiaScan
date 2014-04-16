package changycj.anemiascan;

import java.nio.ByteBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import org.apache.commons.math3.stat.regression.SimpleRegression;

import vuforia.CubeShaders;
import vuforia.SampleApplicationSession;
import vuforia.SampleUtils;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.qualcomm.vuforia.CameraCalibration;
import com.qualcomm.vuforia.CameraDevice;
import com.qualcomm.vuforia.Frame;
import com.qualcomm.vuforia.Image;
import com.qualcomm.vuforia.Marker;
import com.qualcomm.vuforia.Matrix34F;
import com.qualcomm.vuforia.Renderer;
import com.qualcomm.vuforia.State;
import com.qualcomm.vuforia.Tool;
import com.qualcomm.vuforia.TrackableResult;
import com.qualcomm.vuforia.VIDEO_BACKGROUND_REFLECTION;
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
	CameraCalibration cameraCalib;
	private final int RGB565_FORMAT = 1;
	private Bitmap cameraBitmap;
	private Vec3F[] hemoLvlLocs;
	private double[] hemoLevels;
    
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
        
        // get camera calibration
        cameraCalib = CameraDevice.getInstance().getCameraCalibration();
        
        // initialize bitmap for analyzing
        float[] res = cameraCalib.getSize().getData();
        Log.d(LOGTAG, String.format("%.3f, %.3f", res[0], res[1]));
        cameraBitmap = Bitmap.createBitmap((int) res[0], (int) res[1], 
        		Bitmap.Config.RGB_565);
        
        // init hemoglobin levels: location on card
        hemoLevels = new double[] {2, 4, 6, 8, 10};
    	hemoLvlLocs = new Vec3F[5];
    	hemoLvlLocs[0] = new Vec3F(-21.43f, 0, 0);
    	hemoLvlLocs[1] = new Vec3F(-10.71f, 0, 0);
    	hemoLvlLocs[2] = new Vec3F(0, 0, 0);
    	hemoLvlLocs[3] = new Vec3F(10.71f, 0, 0);
    	hemoLvlLocs[4] = new Vec3F(21.43f, 0, 0);
    	
        
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
        	cameraBitmap = getCameraBitmap(state);
        	
        	// calculate region to analyze (testing first: dead center of target)
        	int[] pixels = getPixelsOnBitmap(hemoLvlLocs, result.getPose());
        	
        	// get the reds
        	int[] reds = new int[pixels.length];
        	for (int i = 0; i < pixels.length; i++) {
        		reds[i] = Color.red(pixels[i]);
        	}
            
        	// measured red component
        	int measurement = 100;
        	
        	// fit least squares regression
        	double count = hemoCountModel(reds, measurement);
        	
        	// show on screen
            message = String.format("%d, %d, %d, %d, %d -- %.3f", 
            		Color.red(pixels[0]), Color.red(pixels[1]), Color.red(pixels[2]),
            		Color.red(pixels[3]), Color.red(pixels[4]), count); 
            pixel = pixels[2];
            
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
    
    private double hemoCountModel(int[] pixels, int measurement) {
    	SimpleRegression model = new SimpleRegression(true);
    	double[][] data = new double[hemoLevels.length][2];
    	for (int i = 0; i < hemoLevels.length; i++) {
    		data[i][0] = (double) pixels[i];
    		data[i][1] = (double) hemoLevels[i];
    	}
    	model.addData(data);
    	return model.predict(measurement);
    	
    }
    
    private int[] getPixelsOnBitmap(Vec3F[] vectors, Matrix34F pose) {
        
    	int[] pixels = new int[vectors.length];
    	
    	for (int i = 0; i < vectors.length; i++) {
    		float[] point = Tool.projectPoint(cameraCalib, pose, vectors[i]).getData();
    		pixels[i] = cameraBitmap
    				.getPixel(Math.round(point[0]), Math.round(point[1]));
    	}
    	
    	return pixels;
    }
    
    private Bitmap getCameraBitmap(State state) {
    	
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
    		cameraBitmap.copyPixelsFromBuffer(buffer);
    		return cameraBitmap;
    		
    	} else {
    		Log.e(LOGTAG, "image not found.");
    	}
    	return null;
    }
}