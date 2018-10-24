package com.aji.dev;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.WindowManager;

import com.aji.util.TipsUtil;
import com.aji.view.CameraCustomPreview;
import com.aji.view.CameraPreview;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * Created by Administrator on 2018\10\23 0023.
 */

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class Camera2CustomPreviewDevice {
    private static final SparseIntArray ORIENTATION = new SparseIntArray();

    static {
        ORIENTATION.append(Surface.ROTATION_0, 90);
        ORIENTATION.append(Surface.ROTATION_90, 0);
        ORIENTATION.append(Surface.ROTATION_180, 270);
        ORIENTATION.append(Surface.ROTATION_270, 180);
    }

    private static final int SENSOR_ORIENTATION_DEFAULT_DEGREES = 90;
    private static final int SENSOR_ORIENTATION_INVERSE_DEGREES = 270;
    private static final SparseIntArray DEFAULT_ORIENTATIONS = new SparseIntArray();
    private static final SparseIntArray INVERSE_ORIENTATIONS = new SparseIntArray();

    static {
        DEFAULT_ORIENTATIONS.append(Surface.ROTATION_0, 90);
        DEFAULT_ORIENTATIONS.append(Surface.ROTATION_90, 0);
        DEFAULT_ORIENTATIONS.append(Surface.ROTATION_180, 270);
        DEFAULT_ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }

    static {
        INVERSE_ORIENTATIONS.append(Surface.ROTATION_0, 270);
        INVERSE_ORIENTATIONS.append(Surface.ROTATION_90, 180);
        INVERSE_ORIENTATIONS.append(Surface.ROTATION_180, 90);
        INVERSE_ORIENTATIONS.append(Surface.ROTATION_270, 0);
    }

    //预览状态
    private static final int STATE_PREVIEW = 1;
    //等待获取图像状态(等待拍照)
    private static final int STATE_WAITING_PRECAPTURE = 2;
    private int mState = STATE_PREVIEW;

    private Context context;
    //当前打开的摄像头方向（默认后摄像头）
    private int mCurrentFacing = CameraCharacteristics.LENS_FACING_FRONT;
    // 预览视图
    private CameraCustomPreview mPreviewTexture;
    //相机管理器
    private CameraManager mCameraManager;
    private CameraCharacteristics cameraCharacteristics;
    // 相机最大的预览宽度
    private static final int MAX_PREVIEW_WIDTH = 1920;
    // 相机最大的预览高度
    private static final int MAX_PREVIEW_HEIGHT = 1080;
    // 处理静态图片的输出
    private ImageReader mImageReader;
    //相机传感器方向
    private int mSensorOrientation;
    //相机预览的大小Size
    private Size mPreviewSize;
    //相机的拍照大小
//    private Size mCaptureSize;
    //设备ID
    private String mCameraId;
    //后台处理线程
    private HandlerThread mBackgroundThread;
    //与后台线程通讯的Handler
    private Handler mBackgroundHandler;
    //摄像头设备
    private CameraDevice mCameraDevice;
    // 预览的请求
    private CaptureRequest mPreviewRequest;
    private CaptureRequest.Builder mPreviewRequestBuilder;
    private CameraCaptureSession mCaptureSession;
    //是否是录像中
    private boolean isRecording;

    private Semaphore mCameraOpenCloseLock = new Semaphore(1);

    public Camera2CustomPreviewDevice(Context context) {
        this.context = context;
    }

    /**
     * 计算合适的大小Size,在相机拍照
     *
     * @param choices           所有尺寸
     * @param textureViewWidth  预览画面宽度
     * @param textureViewHeight 预览画面高度
     * @param maxWidth          屏幕宽度
     * @param maxHeight         屏幕高度
     * @return
     */
    public static Size chooseOptimalSize(Size[] choices, int textureViewWidth, int textureViewHeight,
                                         int maxWidth, int maxHeight, CompareSizesByArea compareSizesByArea) {
        // Collect the supported resolutions that are at least as big as the preview Surface
        List<Size> bigEnough = new ArrayList<>();
        // Collect the supported resolutions that are smaller than the preview Surface
        List<Size> notBigEnough = new ArrayList<>();

        for (Size option : choices) {
            //预览大小在屏幕大小范围内并且宽高比跟屏幕宽高比一样则找出满足条件的预览大小
            if (option.getWidth() <= maxWidth && option.getHeight() <= maxHeight) {
                if (option.getWidth() >= textureViewWidth &&
                        option.getHeight() >= textureViewHeight) {
                    bigEnough.add(option);
                } else {
                    notBigEnough.add(option);
                }
            }
        }
        // Pick the smallest of those big enough. If there is no one big enough, pick the largest of those not big enough.
        if (bigEnough.size() > 0) {
            return Collections.min(bigEnough, compareSizesByArea);
        } else if (notBigEnough.size() > 0) {
            return Collections.max(notBigEnough, compareSizesByArea);
        } else {
            Log.e(" 计算结果", "Couldn't find any suitable preview size");
            return choices[0];
        }
    }

    public void switchCamera() {
        release();
        mCurrentFacing = (mCurrentFacing == CameraCharacteristics.LENS_FACING_BACK ? CameraCharacteristics.LENS_FACING_FRONT : CameraCharacteristics.LENS_FACING_BACK);
        configPreviewSize(mPreviewTexture, mPreviewTexture.getWidth(), mPreviewTexture.getHeight());
        openDevice();
    }

    public void configPreviewSize(CameraCustomPreview textureView, int width, int height) {
        this.mPreviewTexture = textureView;
        mCameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
        // 开启后台工作线程
        startWorkThread();
        try {
            //获取到可用的相机
            for (String cameraId : mCameraManager.getCameraIdList()) {
                //获取到每个相机的参数对象，包含前后摄像头，分辨率等
                cameraCharacteristics = mCameraManager.getCameraCharacteristics(cameraId);
                Integer facing = cameraCharacteristics.get(CameraCharacteristics.LENS_FACING);
                if (facing != mCurrentFacing) {
                    continue;
                }
//                if (!Camera2Utils.matchCameraDirection(cameraCharacteristics, currentDirection)) {
//                    continue;
//                }
                //存储流配置类
                StreamConfigurationMap map = cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                if (map == null) {
                    continue;
                }
//                //检查设备,是否支持自动对焦
//                mAutoFocusSupported = Camera2Utils.checkAutoFocus(cameraCharacteristics);
//                //获取最小焦距值。
//                Float minFocalDistance = Camera2Utils.getMinimumFocusDistance(cameraCharacteristics);
//                if (minFocalDistance != null) {
//                    minimum_focus_distance = minFocalDistance;
//                }
//
//                Float maxZoomValue = Camera2Utils.getMaxZoom(cameraCharacteristics);
//                if (maxZoomValue != null) {
//                    maxZoom = maxZoomValue;
//                }
//                TipsUtil.log((facing == CameraCharacteristics.LENS_FACING_BACK ? "后" : "前") + " 摄像头 " + " 是否支持自动对焦 " + mAutoFocusSupported + " 获取到焦距的最大值 " + minimum_focus_distance + " 最大的缩放值 " + maxZoom);
                //获取相机传感器方向
                mSensorOrientation = cameraCharacteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
                //获取到屏幕的旋转角度，进一步判断是否，需要交换维度来获取预览大小
                WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
                int displayRotation = wm.getDefaultDisplay().getRotation();
                boolean swappedDimensions = false;
                switch (displayRotation) {
                    case Surface.ROTATION_0:
                    case Surface.ROTATION_180:
                        if (mSensorOrientation == 90 || mSensorOrientation == 270) {
                            swappedDimensions = true;
                        }
                        break;
                    case Surface.ROTATION_90:
                    case Surface.ROTATION_270:
                        if (mSensorOrientation == 0 || mSensorOrientation == 180) {
                            swappedDimensions = true;
                        }
                        break;
                    default:
                        TipsUtil.log("Display rotation is invalid: " + displayRotation);
                }
                Point displaySize = new Point();
                ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getSize(displaySize);
                int rotatedPreviewWidth = width;
                int rotatedPreviewHeight = height;
                int maxPreviewWidth = displaySize.x;
                int maxPreviewHeight = displaySize.y;
                //当角度反了的时候
                if (swappedDimensions) {
                    rotatedPreviewWidth = height;
                    rotatedPreviewHeight = width;
                    maxPreviewWidth = displaySize.y;
                    maxPreviewHeight = displaySize.x;
                }
                if (maxPreviewWidth > MAX_PREVIEW_WIDTH) {
                    maxPreviewWidth = MAX_PREVIEW_WIDTH;
                }
                if (maxPreviewHeight > MAX_PREVIEW_HEIGHT) {
                    maxPreviewHeight = MAX_PREVIEW_HEIGHT;
                }
                mPreviewSize = chooseOptimalSize(map.getOutputSizes(SurfaceTexture.class),
                        rotatedPreviewWidth, rotatedPreviewHeight, maxPreviewWidth,
                        maxPreviewHeight, new CompareSizesByArea());
                //--2018/10/11 先使用预览大小---对于静态图片，使用可用的最大值来拍摄。
//                mCaptureSize = Collections.max(Arrays.asList(map.getOutputSizes(ImageFormat.JPEG)), new CompareSizeByArea());
                //设置ImageReader,将大小，图片格式
                mImageReader = ImageReader.newInstance(mPreviewSize.getWidth(), mPreviewSize.getHeight(), ImageFormat.JPEG, 2);
                mImageReader.setOnImageAvailableListener(onImageAvailableListener, mBackgroundHandler);
//                // 计算出来的预览大小，设置成TextureView宽高.
                int orientation = context.getResources().getConfiguration().orientation;
                //横屏
                if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    mPreviewTexture.setAspectRatio(mPreviewSize.getWidth(), mPreviewSize.getHeight());
                } else {
                    mPreviewTexture.setAspectRatio(mPreviewSize.getHeight(), mPreviewSize.getWidth());
                }
//                // 检查，相机是否支持闪光。
//                Boolean available = cameraCharacteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE);
//                mFlashSupported = available == null ? false : available;
                mCameraId = cameraId;
                // Log.i(TAG, " 根据相机的前后摄像头" + mCameraId + " 方向是：" + currentDirection);
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
            //不支持该设备
            if (e instanceof NullPointerException) {
                TipsUtil.toast(context, "设备不支持Camera2 API");
            }
        }
    }

    public void openDevice() {
        try {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            if (!mCameraOpenCloseLock.tryAcquire(2500, TimeUnit.MILLISECONDS)) {
                throw new RuntimeException("Time out waiting to lock camera opening.");
            }
            mCameraManager.openCamera(mCameraId, mStateCallback, mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void takePicture() {
        lockFocus();
    }

    public void startRecord() {
        try {
            closePreviewSession();
            setUpMediaRecorder();
            SurfaceTexture texture = mPreviewTexture.getSurfaceTexture();
            assert texture != null;
            texture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
            //创建录制的session会话
            mPreviewRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_RECORD);
            List<Surface> surfaces = new ArrayList<>();
            // 为相机预览设置Surface
//            Surface previewSurface = new Surface(texture);
//            surfaces.add(previewSurface);
//            mPreviewRequestBuilder.addTarget(previewSurface);
            surfaces.add(mImageReader.getSurface());
            mPreviewRequestBuilder.addTarget(mImageReader.getSurface());
            // 为 MediaRecorder设置Surface
            Surface recorderSurface = mMediaRecorder.getSurface();
            surfaces.add(recorderSurface);
            surfaces.add(mImageReader.getSurface());
            mPreviewRequestBuilder.addTarget(recorderSurface);
//            //与未录像的状态保持一致。
//            if (zoomRect != null) {
//                mPreviewBuilder.set(CaptureRequest.SCALER_CROP_REGION, zoomRect);
//            }
            // Start a capture session
            // Once the session starts, we can update the UI and start recording
            mCameraDevice.createCaptureSession(surfaces, new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(CameraCaptureSession cameraCaptureSession) {
                    mCaptureSession = cameraCaptureSession;
                    updatePreview();
                    TipsUtil.log(" startRecordingVideo  正式开始录制 ");
                    isRecording = true;
                    mMediaRecorder.start();
                    uiHandler.sendEmptyMessage(2);
                }

                @Override
                public void onConfigureFailed(CameraCaptureSession cameraCaptureSession) {

                }
            }, mBackgroundHandler);
        } catch (CameraAccessException | IOException e) {
            e.printStackTrace();
        }
    }

    public void stopRecord() {
        isRecording = false;
        /**
         * 在MediaRecorder停止前，停止相机预览，防止抛出serious error异常。
         */
        try {
            mCaptureSession.stopRepeating();
            mCaptureSession.abortCaptures();
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        // 停止录制
        mMediaRecorder.stop();
        mMediaRecorder.reset();
        TipsUtil.toast(context, "停止录像");
        startPreview();
    }

    //启动工作线程
    private void startWorkThread() {
        mBackgroundThread = new HandlerThread("Camera2Thread");
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(this.mBackgroundThread.getLooper());
    }

    //安全停止后台线程和对应的Handler
    private void stopWorkThread() {
        if (mBackgroundThread != null) {
            mBackgroundThread.quitSafely();
        }
        try {
            if (mBackgroundThread != null) {
                mBackgroundThread.join();
                mBackgroundThread = null;
            }
            if (mBackgroundHandler != null) {
                mBackgroundHandler = null;
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");

    private ImageReader.OnImageAvailableListener onImageAvailableListener = new ImageReader.OnImageAvailableListener() {
        @Override
        public void onImageAvailable(ImageReader reader) {
            Image image = reader.acquireNextImage();
            ByteBuffer byteBuffer = image.getPlanes()[0].getBuffer();
            byte[] bytes = new byte[byteBuffer.remaining()];
            byteBuffer.get(bytes);
            Bitmap temp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
//            Bitmap newBitmap = Bitmap.createBitmap(mPreviewSize.getWidth(), mPreviewSize.getHeight(), temp.getConfig());
//
//            Canvas canvas = new Canvas(newBitmap);
//            Matrix matrix = new Matrix();
//            //图片镜像并旋转90度
//            matrix.setScale(-1, 1);
//            matrix.postTranslate(temp.getWidth(), 0);
//            matrix.postRotate(90, temp.getWidth() / 2, temp.getHeight() / 2);
//            matrix.postTranslate(0, (temp.getWidth() - temp.getHeight()) / 2);
//            canvas.drawBitmap(temp, matrix, null);
            mPreviewTexture.drawFrame(temp);
//            try {
//                File newFile = new File(Environment.getExternalStorageDirectory(), "345.jpg");
//                BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(newFile));
//                newBitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
//                bos.flush();
//                bos.close();
//                temp.recycle();
//                newBitmap.recycle();
//            } catch (Exception e) {
//                e.printStackTrace();
//            }finally {
//                image.close();
//                byteBuffer.clear();
//            }
            byteBuffer.clear();
            image.close();
        }
    };

    /**
     * Saves a JPEG {@link Image} into the specified {@link File}.
     */
    private class ImageSaver implements Runnable {

        /**
         * The JPEG image
         */
        private final Image mImage;
        /**
         * The file we save the image into.
         */
        private final File mFile;

        ImageSaver(Image image, File file) {
            mImage = image;
            mFile = file;
        }

        @Override
        public void run() {
            ByteBuffer buffer = mImage.getPlanes()[0].getBuffer();
            byte[] bytes = new byte[buffer.remaining()];
            buffer.get(bytes);
            FileOutputStream output = null;
            try {
                output = new FileOutputStream(mFile);
                output.write(bytes);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                buffer.clear();
                mImage.close();
                if (null != output) {
                    try {
                        output.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            uiHandler.sendEmptyMessage(1);
        }

    }

    Handler uiHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    TipsUtil.toast(context, "拍照成功");
                    break;
                case 2:
                    TipsUtil.toast(context, "开始录像");
                    break;
            }
        }
    };

    /**
     * Capture a still picture. This method should be called when we get a response in
     * {@link #mCaptureCallback} from both {@link #lockFocus()}.
     */
    private void captureStillPicture() {
        try {
            if (null == mCameraDevice) {
                return;
            }
            // This is the CaptureRequest.Builder that we use to take a picture.
            CaptureRequest.Builder captureBuilder;
            if (isRecording) {
                // 录像时拍照请求。使用该模板创建的请求可以最大化地保证照片的质量同时不会破坏正在录制的视频质量。
                // 该模板创建的请求通常与CameraCaptureSession.capture(CaptureRequest request, CameraCaptureSession.CaptureCallback callback, Handler handler) 结合使用，
                // 其中这里的request必须是基于TEMPLATE_RECORD模板创建
                captureBuilder =
                        mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_VIDEO_SNAPSHOT);
            } else {
                captureBuilder =
                        mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            }
            captureBuilder.addTarget(mImageReader.getSurface());//拍照时，是将mImageReader.getSurface()作为目标
            // Use the same AE and AF modes as the preview.
            captureBuilder.set(CaptureRequest.CONTROL_AF_MODE,
                    CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
            captureBuilder.set(CaptureRequest.CONTROL_AE_MODE,
                    CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);
            // Orientation
            int rotation = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getRotation();
//            captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, ORIENTATION.get(rotation));
            captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, getOrientation(ORIENTATION, mSensorOrientation, rotation));
            if (!isRecording) {
                mCaptureSession.stopRepeating();
                mCaptureSession.abortCaptures();
            }
            mCaptureSession.capture(captureBuilder.build(), new CameraCaptureSession.CaptureCallback() {
                @Override
                public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request,
                                               TotalCaptureResult result) {
                    //Log.d("customCarmeraActivity", mFile.toString());
                    unlockFocus();//恢复预览
                }
            }, mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * Unlock the focus. This method should be called when still image capture sequence is
     * finished.
     */
    private void unlockFocus() {
        try {
            // Reset the auto-focus trigger
            mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER,
                    CameraMetadata.CONTROL_AF_TRIGGER_CANCEL);
            mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE,
                    CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);
     /*       mCaptureSession.capture(mPreviewBuilder.build(), mCaptureCallback,
                    mBackgroundHandler);*/
            // After this, the camera will go back to the normal state of preview.
            mState = STATE_PREVIEW;
            mCaptureSession.setRepeatingRequest(mPreviewRequestBuilder.build(), mCaptureCallback, mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * Lock the focus as the first step for a still image capture.
     */
    private void lockFocus() {
        try {
            // This is how to tell the camera to lock focus.
            mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER,
                    CameraMetadata.CONTROL_AF_TRIGGER_START);
            // Tell #mCaptureCallback to wait for the lock.
            mState = STATE_WAITING_PRECAPTURE;
            mCaptureSession.capture(mPreviewRequestBuilder.build(), mCaptureCallback,
                    mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }


    private CameraCaptureSession.CaptureCallback mCaptureCallback =
            new CameraCaptureSession.CaptureCallback() {
                @Override
                public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request,
                                               TotalCaptureResult result) {
                    //Log.d("linc","mSessionCaptureCallback, onCaptureCompleted");
                    mCaptureSession = session;
                    checkState(result);
                }

                @Override
                public void onCaptureProgressed(CameraCaptureSession session, CaptureRequest request,
                                                CaptureResult partialResult) {
                    Log.d("linc", "mSessionCaptureCallback,  onCaptureProgressed");
                    mCaptureSession = session;
                    checkState(partialResult);
                }

                private void checkState(CaptureResult result) {
                    switch (mState) {
                        case STATE_PREVIEW:
                            // We have nothing to do when the camera preview is working normally.
                            break;
                        case STATE_WAITING_PRECAPTURE:
                            Integer afState = result.get(CaptureResult.CONTROL_AF_STATE);
                            if (afState == null) {
                                captureStillPicture();
                            } else if (CaptureResult.CONTROL_AF_STATE_FOCUSED_LOCKED == afState || CaptureResult.CONTROL_AF_STATE_NOT_FOCUSED_LOCKED == afState) {
                                // CONTROL_AE_STATE can be null on some devices
                                Integer aeState = result.get(CaptureResult.CONTROL_AE_STATE);
                                if (aeState == null || aeState == CaptureResult.CONTROL_AE_STATE_CONVERGED) {
                                    //mState = STATE_PICTURE_TAKEN;
                                    captureStillPicture();
                                } else {
                                    //runPrecaptureSequence();//视频拍摄
                                }
                            }
                            break;
                    }
                }

            };


    /**
     * Retrieves the JPEG orientation from the specified screen rotation.
     *
     * @param rotation 屏幕的方向
     * @return JPEG的方向(例如：0, 90, 270, 360)
     */
    private int getOrientation(SparseIntArray ORIENTATIONS, int mSensorOrientation, int rotation) {
        // Sensor orientation is 90 for most devices, or 270 for some devices (eg. Nexus 5X)
        // We have to take that into account and rotate JPEG properly.
        // For devices with orientation of 90, we simply return our mapping from ORIENTATIONS.
        // For devices with orientation of 270, we need to rotate the JPEG 180 degrees.
        return (ORIENTATIONS.get(rotation) + mSensorOrientation + 270) % 360;
    }

    private CameraDevice.StateCallback mStateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(CameraDevice camera) {
            mCameraOpenCloseLock.release();
            mCameraDevice = camera;
            startPreview();
        }

        @Override
        public void onDisconnected(CameraDevice camera) {
            mCameraOpenCloseLock.release();
            camera.close();
            mCameraDevice = null;
        }

        @Override
        public void onError(CameraDevice camera, int error) {
            mCameraOpenCloseLock.release();
            camera.close();
            mCameraDevice = null;
        }
    };

    private void startPreview() {
        //停止之前的预览
        closePreviewSession();
        try {
            SurfaceTexture texture = mPreviewTexture.getSurfaceTexture();
            //assert(texture != null);

            // We configure the size of default buffer to be the size of camera preview we want.
            texture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
            // This is the output Surface we need to start preview.
//            Surface surface = new Surface(texture);

            mPreviewRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
//            mPreviewRequestBuilder.addTarget(surface);//预览时，是将Surface()作为目标
            //自己画预览效果时使用ImageReader来获取每一帧
            mPreviewRequestBuilder.addTarget(mImageReader.getSurface());
            mState = STATE_PREVIEW;
            mCameraDevice.createCaptureSession(
                    Arrays.asList(/*surface, */mImageReader.getSurface()), //如果是自己画每一帧的话不需要在设置那个surface，只需要mImageReader.getSurface()即可
                    new CameraCaptureSession.StateCallback() {
                        @Override
                        public void onConfigured(CameraCaptureSession cameraCaptureSession) {
                            // The camera is already closed
                            if (null == mCameraDevice) {
                                return;
                            }
                            mCaptureSession = cameraCaptureSession;
                            try {
                                mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE,
                                        CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
                                mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE,
                                        CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);
                                mPreviewRequest = mPreviewRequestBuilder.build();
                                mCaptureSession.setRepeatingRequest(mPreviewRequest, mCaptureCallback, mBackgroundHandler);
                            } catch (CameraAccessException e) {
                                e.printStackTrace();
                                Log.e("linc", "set preview builder failed." + e.getMessage());
                            }
                        }

                        @Override
                        public void onConfigureFailed(CameraCaptureSession cameraCaptureSession) {
                            TipsUtil.toast(context, "Camera configuration Failed");
                        }
                    }, mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();

        }
    }

    /**
     * 在 startPreView()之后执行用于更新相机预览界面
     */
    private void updatePreview() {
        if (null == mCameraDevice) {
            return;
        }
        try {
            mPreviewRequestBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
            mCaptureSession.setRepeatingRequest(mPreviewRequestBuilder.build(), null, mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    public void closePreviewSession() {
        if (mCaptureSession != null) {
            mCaptureSession.close();
            mCaptureSession = null;
        }
    }

    private MediaRecorder mMediaRecorder;

    /**
     * 设置媒体录制器的配置参数
     * <p>
     * 音频，视频格式，文件路径，频率，编码格式等等
     *
     * @throws IOException
     */
    private void setUpMediaRecorder() throws IOException {
        if (null == mMediaRecorder) {
            mMediaRecorder = new MediaRecorder();

            mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
            mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
//            mMediaRecorder.setOutputFile(FileUtil.createVideoDiskFile(context).getAbsolutePath());
            mMediaRecorder.setOutputFile("/mnt/sdcard/" + simpleDateFormat.format(new Date()) + ".mp4");
            mMediaRecorder.setVideoEncodingBitRate(10000000);
            //每秒30帧
            mMediaRecorder.setVideoFrameRate(30);
//            mMediaRecorder.setVideoSize(640, 480);
            mMediaRecorder.setVideoSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
            mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
            mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            //2018-10-22先试试自定义配置 //配置录制的视频为720p
//            mMediaRecorder.setProfile(CamcorderProfile.get(mCurrentFacing, CamcorderProfile.QUALITY_720P));
            int rotation = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getRotation();
            switch (mSensorOrientation) {
                case SENSOR_ORIENTATION_DEFAULT_DEGREES:
                    mMediaRecorder.setOrientationHint(DEFAULT_ORIENTATIONS.get(rotation));
                    break;
                case SENSOR_ORIENTATION_INVERSE_DEGREES:
                    mMediaRecorder.setOrientationHint(INVERSE_ORIENTATIONS.get(rotation));
                    break;
                default:
                    break;
            }
        } else {
            mMediaRecorder.reset();
        }
        mMediaRecorder.prepare();
    }


    public void release() {
        stopWorkThread();
        try {
            mCameraOpenCloseLock.acquire();
            //停止录像
            if (isRecording && null != mMediaRecorder) {
                isRecording = false;
                mMediaRecorder.stop();
                mMediaRecorder.release();
                mMediaRecorder = null;
            }
            //停止预览
            closePreviewSession();
            if (null != mCameraDevice) {
                mCameraDevice.close();
                mCameraDevice = null;
            }
            if (null != mImageReader) {
                mImageReader.close();
                mImageReader = null;
            }
        } catch (InterruptedException e) {
            throw new RuntimeException("Interrupted while trying to lock camera closing.", e);
        } finally {
            mCameraOpenCloseLock.release();
        }
    }
}
