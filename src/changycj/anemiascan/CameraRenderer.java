package changycj.anemiascan;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import org.apache.commons.math3.stat.regression.SimpleRegression;

import vuforia.SampleApplicationSession;
import vuforia.SampleUtils;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.qualcomm.vuforia.CameraCalibration;
import com.qualcomm.vuforia.CameraDevice;
import com.qualcomm.vuforia.Frame;
import com.qualcomm.vuforia.Image;
import com.qualcomm.vuforia.Marker;
import com.qualcomm.vuforia.Matrix34F;
import com.qualcomm.vuforia.Rectangle;
import com.qualcomm.vuforia.Renderer;
import com.qualcomm.vuforia.State;
import com.qualcomm.vuforia.Tool;
import com.qualcomm.vuforia.TrackableResult;
import com.qualcomm.vuforia.VIDEO_BACKGROUND_REFLECTION;
import com.qualcomm.vuforia.Vec3F;
import com.qualcomm.vuforia.Vuforia;

import vuforia.LineShaders;

public class CameraRenderer implements GLSurfaceView.Renderer
{
    private static final String LOGTAG = "CameraRenderer";
    
    SampleApplicationSession vuforiaAppSession;
    CameraActivity mActivity;
    
    public boolean mIsActive = false;
    
	Handler activityHandler = new Handler(Looper.getMainLooper());
	CameraCalibration cameraCalib;
	private final int RGB565_FORMAT = 1;
	private Bitmap cameraBitmap;
	private Vec3F[][] hemoLvlLocs;
	private double[] hemoLevels;
	private Vec3F measureLoc;
	
	// Open GL magic
    private int vbShaderProgramID = 0;
    private int vbVertexHandle = 0;   
    private int lineOpacityHandle = 0;
    private int lineColorHandle = 0;
    private int mvpMatrixButtonsHandle = 0;
    
    private Rectangle[] renderRectangle;
    
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
        float[] resolution = cameraCalib.getSize().getData();
        cameraBitmap = Bitmap.createBitmap((int) resolution[0], (int) resolution[1], 
        		Bitmap.Config.RGB_565);
        
        // init hemoglobin levels: location on card
        hemoLevels = new double[] {4, 6, 8, 10, 12, 14};
    	hemoLvlLocs = new Vec3F[6][];
    	hemoLvlLocs[0] = getHemoLvlArea(-0.63f, -2.3f, 0);
    	hemoLvlLocs[1] = getHemoLvlArea(-0.63f, -1.38f, 0);
    	hemoLvlLocs[2] = getHemoLvlArea(-0.63f, -0.46f, 0);
    	hemoLvlLocs[3] = getHemoLvlArea(-0.63f, 0.46f, 0);
    	hemoLvlLocs[4] = getHemoLvlArea(-0.63f, 1.38f, 0);
    	hemoLvlLocs[5] = getHemoLvlArea(-0.63f, 2.3f, 0);  
    	
    	measureLoc = new Vec3F(1.92f, 0, 0);
    	
    	// rectangles to render on frames
    	renderRectangle = new Rectangle[8];
    	
    	// red color scale bars
    	renderRectangle[0] = new Rectangle(-2.08f, 2.76f, 0.84f, 1.84f);
    	renderRectangle[1] = new Rectangle(-2.08f, 1.84f, 0.84f, 0.92f);
    	renderRectangle[2] = new Rectangle(-2.08f, 0.92f, 0.84f, 0f);
    	renderRectangle[3] = new Rectangle(-2.08f, 0f, 0.84f, -0.92f);
    	renderRectangle[4] = new Rectangle(-2.08f, -0.92f, 0.84f, -1.84f);
    	renderRectangle[5] = new Rectangle(-2.08f, -1.84f, 0.84f, -2.76f);
    	
    	// the green circle
    	renderRectangle[6] = new Rectangle(1.52f, 0.4f, 2.22f, -0.4f);
    	
    	// the entire target
    	renderRectangle[7] = new Rectangle(-3.625f, 3.625f, 3.625f, -3.625f);
        
    	// Open GL magic!
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, Vuforia.requiresAlpha() ? 0.0f
            : 1.0f);
        vbShaderProgramID = SampleUtils.createProgramFromShaderSrc(
               LineShaders.LINE_VERTEX_SHADER, LineShaders.LINE_FRAGMENT_SHADER);
        vbVertexHandle = GLES20.glGetAttribLocation(vbShaderProgramID,
                "vertexPosition");
        mvpMatrixButtonsHandle = GLES20.glGetUniformLocation(vbShaderProgramID,
                "modelViewProjectionMatrix");
        lineOpacityHandle = GLES20.glGetUniformLocation(vbShaderProgramID,
                "opacity");
        lineColorHandle = GLES20.glGetUniformLocation(vbShaderProgramID,
                "color");
    }
    
    private Vec3F[] getHemoLvlArea(float cx, float cy, float cz) {
    	Vec3F[] samples = new Vec3F[9];
    	int r = 3, c = 3;
    	for (int i = 0; i < r; i++) {
    		for (int j = 0; j < c; j++) {
    			samples[i * r + j] = new Vec3F(cx + (i - 1) * 0.1f, cy + (j-1) * 0.1f, cz);
    		} 
    	}
    	return samples;
    }
    
    
    private void renderFrame() {
    	
    	// Open GL initializing stuff magic
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        State state = Renderer.getInstance().begin();
        Renderer.getInstance().drawVideoBackground();
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        GLES20.glEnable(GLES20.GL_CULL_FACE);
        GLES20.glCullFace(GLES20.GL_BACK);
        if (Renderer.getInstance().getVideoBackgroundConfig().getReflection() 
        		== VIDEO_BACKGROUND_REFLECTION.VIDEO_BACKGROUND_REFLECTION_ON)
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
        	Matrix34F pose = result.getPose();
        	
        	float[] modelViewMatrix = Tool.convertPose2GLMatrix(result.getPose()).getData();
        	
        	float[] modelViewProjection = new float[16];
            Matrix.multiplyMM(modelViewProjection, 0, vuforiaAppSession
                .getProjectionMatrix().getData(), 0, modelViewMatrix, 0);
        	
            float[] vbVertices = initGLVertices();

                
            // Render frame around button
            GLES20.glUseProgram(vbShaderProgramID);
                
            GLES20.glVertexAttribPointer(vbVertexHandle, 3,
            		GLES20.GL_FLOAT, false, 0, fillBuffer(vbVertices));
                
            GLES20.glEnableVertexAttribArray(vbVertexHandle);
            GLES20.glUniform1f(lineOpacityHandle, 1.0f);
            GLES20.glUniform3f(lineColorHandle, 1.0f, 1.0f, 1.0f);
                
            GLES20.glUniformMatrix4fv(mvpMatrixButtonsHandle, 1, false,
                modelViewProjection, 0);
                
            GLES20.glDrawArrays(GLES20.GL_LINES, 0, 8 * renderRectangle.length);
            GLES20.glDisableVertexAttribArray(vbVertexHandle);
            
        	
        	// get frame image into bitmap
        	cameraBitmap = getCameraBitmap(state);
        	
        	// analyze each level, and get average red value
        	int[] reds = new int[hemoLevels.length];
        	for (int i = 0; i < hemoLevels.length; i++) {
        		int[] ps = getPixelsOnBitmap(hemoLvlLocs[i], pose);
        		reds[i] = averageRed(ps);
        	}
            
        	// measured red component
        	int measuredPixel = getPixelsOnBitmap(new Vec3F[]{measureLoc}, pose)[0];
        	
        	// fit least squares regression
        	double count = hemoCountModel(reds, Color.red(measuredPixel));
        	
        	// show on screen
            message = String.format("%d, %d, %d, %d, %d, %d -- %.3f", 
            		reds[0], reds[1], reds[2],
            		reds[3], reds[4], reds[5], count); 
            pixel = measuredPixel;
            
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
    
    private int averageRed(int[] pixels) {
    	int sum = 0;
    	for (int i = 0; i < pixels.length; i++) {
    		sum += Color.red(pixels[i]);
    	}
    	return sum / pixels.length;
    }
    
    private float[] initGLVertices() {
    	float[] vertices = new float[renderRectangle.length * 24];
    	int vInd = 0;   	
    	
    	for (Rectangle rect : renderRectangle) {
    		vertices[vInd] = rect.getLeftTopX();
            vertices[vInd + 1] = rect.getLeftTopY();
            vertices[vInd + 2] = 0.0f;
            vertices[vInd + 3] = rect.getRightBottomX();
            
            vertices[vInd + 4] = rect.getLeftTopY();
            vertices[vInd + 5] = 0.0f;
            vertices[vInd + 6] = rect.getRightBottomX();
            vertices[vInd + 7] = rect.getLeftTopY();
            vertices[vInd + 8] = 0.0f;
            vertices[vInd + 9] = rect.getRightBottomX();
            vertices[vInd + 10] = rect.getRightBottomY();
            vertices[vInd + 11] = 0.0f;
            vertices[vInd + 12] = rect.getRightBottomX();
            vertices[vInd + 13] = rect.getRightBottomY();
            vertices[vInd + 14] = 0.0f;
            vertices[vInd + 15] = rect.getLeftTopX();
            vertices[vInd + 16] = rect.getRightBottomY();
            vertices[vInd + 17] = 0.0f;
            vertices[vInd + 18] = rect.getLeftTopX();
            vertices[vInd + 19] = rect.getRightBottomY();
            vertices[vInd + 20] = 0.0f;
            vertices[vInd + 21] = rect.getLeftTopX();
            vertices[vInd + 22] = rect.getLeftTopY();
            vertices[vInd + 23] = 0.0f;
            vInd += 24;
    	}    	
    	return vertices;
    }
    
    private Buffer fillBuffer(float[] array) {
        ByteBuffer bb = ByteBuffer.allocateDirect(4 * array.length); 
        bb.order(ByteOrder.LITTLE_ENDIAN);
        for (float d : array)
            bb.putFloat(d);
        bb.rewind();       
        return bb;   
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