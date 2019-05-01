package org.reactnative.camera.tasks;

import android.graphics.Bitmap;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.WritableArray;

import org.reactnative.camera.PoseEstimatorModule;
import org.reactnative.camera.LimbPositions;

import static org.reactnative.camera.utils.ImageFormatConverter.convertYUV420_NV21toRGB8888;

public class PoseEstimatorAsyncTask extends android.os.AsyncTask<Void, Void, LimbPositions> {
    private PoseEstimatorModule mPoseEstimator;
    private PoseEstimatorAsyncTaskDelegate mDelegate;
    //private Bitmap mbitmap;
    private byte[] mImageData;
    private int mWidth;
    private int mHeight;
    private int mRotation;

    public PoseEstimatorAsyncTask(
            PoseEstimatorAsyncTaskDelegate delegate,
            PoseEstimatorModule poseEstimator,
            byte[] imageData,
            int width,
            int height,
            int rotation
    ) {
        mDelegate = delegate;
        mPoseEstimator = poseEstimator;
        mImageData = imageData;
        mWidth = width;
        mHeight = height;
        mRotation = rotation;
    }

    @Override
    protected LimbPositions doInBackground(Void... ignored) {
        if (isCancelled() || mDelegate == null || mPoseEstimator == null) {
            return null;
        }

        // Seems like Android's 'ScriptIntrinsicYuvToRGB' would be a preferred solution.
        int[] bytesInRGB = convertYUV420_NV21toRGB8888(mImageData, mWidth, mHeight);
        Bitmap bmp = Bitmap.createBitmap(bytesInRGB, mWidth, mHeight, Bitmap.Config.ARGB_8888);

        mPoseEstimator.run(bmp);
        if(mPoseEstimator.Output == null){
            return null;
        }
        return new LimbPositions(mPoseEstimator.Output);
    }

    @Override
    protected void onPostExecute(LimbPositions limbPositions) {
        super.onPostExecute(limbPositions);

        if (limbPositions != null) {
            mDelegate.onPoseEstimated(serializeEventData(limbPositions));
        }
        mDelegate.onPoseEstimatorTaskCompleted();
    }

    private WritableArray serializeEventData(LimbPositions limbPositions) {
        return Arguments.makeNativeArray(limbPositions.getLimbPositions());
    }
}
