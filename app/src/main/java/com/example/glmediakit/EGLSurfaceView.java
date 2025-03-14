package com.example.glmediakit;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import androidx.annotation.NonNull;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class EGLSurfaceView extends SurfaceView implements SurfaceHolder.Callback {
    private long nativeHandle = 0;

    private native long nativeInit();
    private native void nativeFinalize(long handle);
    private native void nativeSurfaceCreated(long handle, Surface surface);
    private native void nativeSurfaceChanged(long handle, int width, int height);
    private native void nativeSurfaceDestroyed(long handle);
    private native void nativeSetImage(long handle, ByteBuffer pixels, int width, int height);


    public EGLSurfaceView(Context context) {
        super(context);
        init();
    }

    public EGLSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        System.loadLibrary("GLMediaKit");

        // 注册Surface回调
        getHolder().addCallback(this);

        nativeHandle = nativeInit();
    }

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder holder) {
        if (nativeHandle != 0) {
            nativeSurfaceCreated(nativeHandle, holder.getSurface());
        }
    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {
        if (nativeHandle != 0) {
            nativeSurfaceChanged(nativeHandle, width, height);
        }
    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
        if (nativeHandle != 0) {
            nativeSurfaceDestroyed(nativeHandle);
        }
    }

    public void setImage(Bitmap bitmap) {
        if (nativeHandle != 0 && bitmap != null && !bitmap.isRecycled()) {
            ByteBuffer buffer = ByteBuffer.allocateDirect(bitmap.getWidth() * bitmap.getHeight() * 4);
            buffer.order(ByteOrder.nativeOrder());
            bitmap.copyPixelsToBuffer(buffer);
            buffer.position(0);

            nativeSetImage(nativeHandle, buffer, bitmap.getWidth(), bitmap.getHeight());
        }
    }

    @Override
    protected void finalize() throws Throwable {
        if (nativeHandle != 0) {
            nativeFinalize(nativeHandle);
            nativeHandle = 0;
        }
        super.finalize();
    }
}
