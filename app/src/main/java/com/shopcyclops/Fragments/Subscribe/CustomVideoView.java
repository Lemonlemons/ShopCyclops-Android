package com.shopcyclops.Fragments.Subscribe;

import android.content.Context;
import android.graphics.Bitmap;
import android.opengl.GLException;
import android.opengl.GLSurfaceView;
import android.view.MotionEvent;
import android.view.View;

import com.red5pro.streaming.R5Stream;
import com.red5pro.streaming.view.R5VideoView;

import java.nio.IntBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by Andrew on 9/6/2015.
 */
public class CustomVideoView extends R5VideoView {

    public CustomVideoView(Context context) {
        super(context);
    }

    public CustomVideoView(Context context, android.util.AttributeSet attrs) {
        super(context,attrs);
    }

    public void attachStream(R5Stream stream) {
        super.attachStream(stream);
    }


}
