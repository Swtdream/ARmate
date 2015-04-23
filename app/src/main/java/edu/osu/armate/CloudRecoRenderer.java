/*===============================================================================
Copyright (c) 2012-2014 Qualcomm Connected Experiences, Inc. All Rights Reserved.

Vuforia is a trademark of QUALCOMM Incorporated, registered in the United States 
and other countries. Trademarks of QUALCOMM Incorporated are used with permission.
===============================================================================*/

package edu.osu.armate;

import android.app.Activity;
import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.AsyncTask;
import android.view.View;
import android.widget.ProgressBar;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.qualcomm.vuforia.Matrix34F;
import com.qualcomm.vuforia.Matrix44F;
import com.qualcomm.vuforia.Renderer;
import com.qualcomm.vuforia.State;
import com.qualcomm.vuforia.Tool;
import com.qualcomm.vuforia.TrackableResult;
import com.qualcomm.vuforia.VIDEO_BACKGROUND_REFLECTION;
import com.qualcomm.vuforia.Vuforia;

import edu.osu.armate.Api.HttpHelper;
import edu.osu.armate.utils.ARObject;
import edu.osu.armate.utils.CubeShaders;
import edu.osu.armate.utils.LoadingDialogHandler;
import edu.osu.armate.utils.MeshObject;
import edu.osu.armate.utils.SampleUtils;
import edu.osu.armate.utils.TargetObject;
import edu.osu.armate.utils.Teapot;
import edu.osu.armate.utils.Cube;
import edu.osu.armate.utils.Texture;

import java.util.Vector;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;


// The renderer class for the CloudReco sample. 
public class CloudRecoRenderer implements GLSurfaceView.Renderer
{
    SampleApplicationSession vuforiaAppSession;
    
    private static final float OBJECT_SCALE_FLOAT = 3.0f;
    
    private int shaderProgramID;
    private int vertexHandle;
    private int normalHandle;
    private int textureCoordHandle;
    private int mvpMatrixHandle;
    private int texSampler2DHandle;
    
    private Vector<Texture> mTextures;

    private MeshObject mObject;
    
    private CloudReco mActivity;

    private HttpHelper httpHelper;
    private Gson gson;
    private String currentObject = "";
    private String objectJson = "";
    private boolean isLoadingFinished = false;
//    private ProgressBar progressIndicator;
    private LoadingDialogHandler loadingDialogHandler;

    public CloudRecoRenderer(SampleApplicationSession session, CloudReco activity, LoadingDialogHandler ld)
    {
        vuforiaAppSession = session;
        mActivity = activity;
        loadingDialogHandler = ld;
    }
    
    
    // Called when the surface is created or recreated.
    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config)
    {
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
        // Call Vuforia function to handle render surface size changes:
        vuforiaAppSession.onSurfaceChanged(width, height);
    }
    
    
    // Called to draw the current frame.
    @Override
    public void onDrawFrame(GL10 gl)
    {
        // Call our function to render content
        renderFrame();
    }
    
    
    // Function for initializing the renderer.
    private void initRendering()
    {
        // Define clear color
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, Vuforia.requiresAlpha() ? 0.0f
                : 1.0f);
        
        for (Texture t : mTextures)
        {
            GLES20.glGenTextures(1, t.mTextureID, 0);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, t.mTextureID[0]);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                    GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                    GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA,
                    t.mWidth, t.mHeight, 0, GLES20.GL_RGBA,
                    GLES20.GL_UNSIGNED_BYTE, t.mData);
        }
        
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
    
    
    // The render function.
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
        GLES20.glEnable(GLES20.GL_CULL_FACE);
        if (Renderer.getInstance().getVideoBackgroundConfig().getReflection() == VIDEO_BACKGROUND_REFLECTION.VIDEO_BACKGROUND_REFLECTION_ON)
            GLES20.glFrontFace(GLES20.GL_CW);  // Front camera
        else
            GLES20.glFrontFace(GLES20.GL_CCW);   // Back camera
            
        // Did we find any trackables this frame?
        if (state.getNumTrackableResults() > 0)
        {
            // Gets current trackable result
            TrackableResult trackableResult = state.getTrackableResult(0);
            
            if (trackableResult == null)
            {
                return;
            }

            mActivity.stopFinderIfStarted();
            
            // Renders the Augmentation View with the 3D Book data Panel
            renderAugmentation(trackableResult);
            
        }
        else
        {
            mActivity.startFinderIfStopped();
        }
        
        GLES20.glDisable(GLES20.GL_DEPTH_TEST);
        
        Renderer.getInstance().end();
    }

    public class RenderAsyncTask extends AsyncTask<Void, Integer, String> {

        String name;
        Matrix34F pose;


        public RenderAsyncTask(TrackableResult trackableResult) {
            super();
            this.name = trackableResult.getTrackable().getName();
            this.pose = trackableResult.getPose();
        }

        @Override
        protected String doInBackground(Void... voids) {
            if(httpHelper == null) {
                httpHelper = new HttpHelper();
            }
//            if(httpHelper.findModelByName(trackableResult.getTrackable().getName())) {
//                return httpHelper.getResult();
//            } else {
//                return "";
//            }

            publishProgress(0);
            httpHelper.findModelByName(name);
//            publishProgress(30);
//            System.out.println(httpHelper.getResult());
            objectJson = httpHelper.getResult();
//            publishProgress(50);
            return objectJson;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if(gson == null) {
                gson = new Gson();
            }
            ARObject obj = gson.fromJson(s, new TypeToken<ARObject>(){}.getType());
//            publishProgress(70);
            mObject = new TargetObject(obj);
//            publishProgress(90);
            publishProgress(100);
            isLoadingFinished = true;
            showAR(pose);
        }

//        @Override
//        protected void onProgressUpdate(Integer... values) {
//            pb.setProgress(values[0]);
//        }


        @Override
        protected void onProgressUpdate(Integer... values) {
            switch (values[0]) {
                case 0:
                    loadingDialogHandler.sendEmptyMessage(1);
                    break;
                case 100:
                    loadingDialogHandler.sendEmptyMessage(0);
                    break;
            }
        }
    }
    
    private void renderAugmentation(TrackableResult trackableResult)
    {
//        switch (trackableResult.getTrackable().getName()) {
//            case "001":
//                mObject = new Teapot();
//                break;
//            case "002":
//                mObject = new Cube();
//                break;
//        }
        if(trackableResult == null || trackableResult.getTrackable() == null) {
            return;
        }
        if(trackableResult.getTrackable().getName()!="" && !currentObject.equals(trackableResult.getTrackable().getName())) {
            currentObject = trackableResult.getTrackable().getName();
            isLoadingFinished = false;
            RenderAsyncTask renderAsyncTask = new RenderAsyncTask(trackableResult);
            renderAsyncTask.execute();
        }

        if(isLoadingFinished && currentObject != "") {
            showAR(trackableResult.getPose());
        }


    }
    
    public void showAR(Matrix34F pose) {
        Matrix44F modelViewMatrix_Vuforia = Tool
                .convertPose2GLMatrix(pose);
        float[] modelViewMatrix = modelViewMatrix_Vuforia.getData();

        int textureIndex = 0;

        // deal with the modelview and projection matrices
        float[] modelViewProjection = new float[16];
        Matrix.translateM(modelViewMatrix, 0, 0.0f, 0.0f, OBJECT_SCALE_FLOAT);
        Matrix.scaleM(modelViewMatrix, 0, OBJECT_SCALE_FLOAT,
                OBJECT_SCALE_FLOAT, OBJECT_SCALE_FLOAT);
        Matrix.multiplyMM(modelViewProjection, 0, vuforiaAppSession
                .getProjectionMatrix().getData(), 0, modelViewMatrix, 0);

        // activate the shader program and bind the vertex/normal/tex coords
        GLES20.glUseProgram(shaderProgramID);
        GLES20.glVertexAttribPointer(vertexHandle, 3, GLES20.GL_FLOAT, false,
                0, mObject.getVertices());
        GLES20.glVertexAttribPointer(normalHandle, 3, GLES20.GL_FLOAT, false,
                0, mObject.getNormals());
        GLES20.glVertexAttribPointer(textureCoordHandle, 2, GLES20.GL_FLOAT,
                false, 0, mObject.getTexCoords());

        GLES20.glEnableVertexAttribArray(vertexHandle);
        GLES20.glEnableVertexAttribArray(normalHandle);
        GLES20.glEnableVertexAttribArray(textureCoordHandle);

        // activate texture 0, bind it, and pass to shader
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,
                mTextures.get(textureIndex).mTextureID[0]);
        GLES20.glUniform1i(texSampler2DHandle, 0);

        // pass the model view matrix to the shader
        GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false,
                modelViewProjection, 0);

        // finally draw the teapot
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, mObject.getNumObjectIndex(),
                GLES20.GL_UNSIGNED_SHORT, mObject.getIndices());

        // disable the enabled arrays
        GLES20.glDisableVertexAttribArray(vertexHandle);
        GLES20.glDisableVertexAttribArray(normalHandle);
        GLES20.glDisableVertexAttribArray(textureCoordHandle);

        SampleUtils.checkGLError("CloudReco renderFrame");
    }

    public void setTextures(Vector<Texture> textures)
    {
        mTextures = textures;
    }
    
}
