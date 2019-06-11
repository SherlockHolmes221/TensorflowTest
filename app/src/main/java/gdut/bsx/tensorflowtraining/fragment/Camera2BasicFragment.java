/* Copyright 2017 The TensorFlow Authors. All Rights Reserved.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
==============================================================================*/

package gdut.bsx.tensorflowtraining.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.ImageReader;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.MessageQueue;
import android.support.annotation.NonNull;
import android.support.v13.app.FragmentCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatDelegate;
import android.util.Log;
import android.util.Size;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Timer;
import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import gdut.bsx.tensorflowtraining.R;
import gdut.bsx.tensorflowtraining.activity.ScoreActivity;
import gdut.bsx.tensorflowtraining.ternsorflow.Classifier;
import gdut.bsx.tensorflowtraining.ternsorflow.TensorFlowImageClassifier;
import gdut.bsx.tensorflowtraining.widget.AutoFitTextureView;

import static gdut.bsx.tensorflowtraining.utils.Configure.IMAGE_MEAN;
import static gdut.bsx.tensorflowtraining.utils.Configure.IMAGE_STD;
import static gdut.bsx.tensorflowtraining.utils.Configure.INPUT_NAME;
import static gdut.bsx.tensorflowtraining.utils.Configure.INPUT_SIZE;
import static gdut.bsx.tensorflowtraining.utils.Configure.LABEL_FILE;
import static gdut.bsx.tensorflowtraining.utils.Configure.MODEL_FILE;
import static gdut.bsx.tensorflowtraining.utils.Configure.OUTPUT_NAME;

/** Basic fragments for the Camera. */
public class Camera2BasicFragment extends Fragment
    implements FragmentCompat.OnRequestPermissionsResultCallback {

  /** Tag for the {@link Log}. */
  private static final String TAG = "TfLiteCameraDemo";

  private static final String FRAGMENT_DIALOG = "dialog";

  private static final String HANDLE_THREAD_NAME = "CameraBackground";

  private static final int PERMISSIONS_REQUEST_CODE = 1;

  private final Object lock = new Object();
  private boolean runClassifier = false;
  private boolean checkedPermissions = false;

  /** Max preview width that is guaranteed by Camera2 API */
  private static final int MAX_PREVIEW_WIDTH = 1920;

  /** Max preview height that is guaranteed by Camera2 API */
  private static final int MAX_PREVIEW_HEIGHT = 1080;

  private boolean isFrontCamera = true;

  private ImageView actionImage;

  private VideoView videoView;
  private int curPic = 0;
  Timer timer = new Timer();
  boolean isSetVisableGone = false;

  float[] score = new float[10];

  boolean isShowVideo = false;

    /**
     * 显示动作框
     * 第一个动作在39-44s
     * 第二个动作在1min23-1min28
     * 第三个动作在1min30-1min35
     * 第四个动作在1min45-1min50
     * 第五个动作在2min58-3min03
     * 第六个动作在3min20-3min25
     * 第七个动作在3min30-3min35
     * 第八个动作在4min08-4min13
     * 第九个动作在4min35-4min40
     * 第十个动作在4min53-4min58
     *
     */
  private void showChangePic(){
      getActivity().runOnUiThread(new Runnable() {		// UI thread
          @Override
          public void run() {
              //Log.e(TAG, "run: ");
              if(isSetVisableGone) {
                  stop();
                  actionImage.setVisibility(View.GONE);

                  if(curPic == 0){
                      if(isShowVideo){
                          float sum  = 0;
                          for(int i = 0 ; i < 10 ;i++){
                              sum += score[i] * 100;
                          }
                          sum /= 10.0;
                          //最后一张结束了，处理成绩，跳转了
                          Intent intent = new Intent();
                          intent.putExtra("score",sum);
                          intent.setClass(getActivity(), ScoreActivity.class);
                          startActivity(intent);
                          getActivity().finish();
                      }else {
                          //这里是video开始的
                          videoView.setVisibility(View.VISIBLE);
                          videoView.start();
                          //Log.e(TAG, "videoView start");
                          //test
                          startTimer(39000);
                          isShowVideo = true;
                      }

                  }
                  else if(curPic == 1){
                      if(isShowVideo){
                          startTimer(38000);
                      }else {
                          startTimer(5000);
                      }

                  }
                  else if(curPic == 2){
                      if(isShowVideo)
                          startTimer(2000);
                      else
                          startTimer(5000);
                  }
                  else if(curPic == 3){
                      if(isShowVideo)
                          startTimer(9000);
                      else
                          startTimer(5000);

                  }
                  else if(curPic == 4){
                      if(isShowVideo)
                          startTimer(68000);
                      else
                          startTimer(5000);

                  }
                  else if(curPic == 5){
                      if(isShowVideo)
                          startTimer(17000);
                      else
                          startTimer(5000);

                  }
                  else if(curPic == 6){
                      if(isShowVideo)
                          startTimer(5000);
                      else
                          startTimer(5000);

                  }
                  else if(curPic == 7){
                      if(isShowVideo)
                          startTimer(33000);
                      else
                          startTimer(5000);

                  }
                  else if(curPic == 8){
                      if(isShowVideo)
                          startTimer(22000);
                      else
                          startTimer(5000);

                  }
                  else if(curPic == 9){
                      if(isShowVideo)
                          startTimer(10000);
                      else
                          startTimer(5000);

                  }
              }

              else {
                  begin();
                  actionImage.setVisibility(View.VISIBLE);
                  if(curPic == 0){
                      actionImage.setImageResource(R.drawable.img_1);
                  }else if(curPic == 1){
                      actionImage.setImageResource(R.drawable.img_2);
                  }
                  else if(curPic == 2){
                      actionImage.setImageResource(R.drawable.img_3);
                  }
                  else if(curPic == 3){
                      actionImage.setImageResource(R.drawable.img_4);
                  }
                  else if(curPic == 4){
                      actionImage.setImageResource(R.drawable.img_5);
                  }
                  else if(curPic == 5){
                      actionImage.setImageResource(R.drawable.img_6);
                  }
                  else if(curPic == 6){
                      actionImage.setImageResource(R.drawable.img_7);
                  }
                  else if(curPic == 7){
                      actionImage.setImageResource(R.drawable.img_8);
                  }
                  else if(curPic == 8){
                      actionImage.setImageResource(R.drawable.img_9);
                  }
                  else if(curPic == 9){
                      actionImage.setImageResource(R.drawable.img_10);
                  }
                  curPic = (curPic+1) % 10;

                  if(isShowVideo){
                      if(curPic == 6){
                          startTimer(3000);
                      }else if(curPic == 0){
                          startTimer(10000);
                      }else
                          startTimer(5000);
                  }else
                      startTimer(5000);

              }

              isSetVisableGone =  ! isSetVisableGone;
          }

      });
  }

  private void begin(){
      synchronized (lock) {
          runClassifier = true;
      }
  }

  private void stop(){
      synchronized (lock) {
          //runClassifier = false;
      }
  }

    /**
     * 执行定时任务
     * @param time
     */
  private void startTimer(int time){
      Handler handler = new Handler();
      handler.postDelayed(new Runnable() {
          @Override
          public void run() {
              showChangePic();
          }
      },time);
  }

  /**
   * {@link TextureView.SurfaceTextureListener} handles several lifecycle events on a {@link
   * TextureView}.
   */
  private final TextureView.SurfaceTextureListener surfaceTextureListener =
      new TextureView.SurfaceTextureListener() {

        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture texture, int width, int height) {
          //打开相机
          openCamera(width, height);
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture texture, int width, int height) {
          configureTransform(width, height);
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture texture) {
          return true;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture texture) {}
      };




  /** ID of the current {@link CameraDevice}. */
  private String cameraId;

  /** An {@link AutoFitTextureView} for camera preview. */
  private AutoFitTextureView textureView;

  /** A {@link CameraCaptureSession } for camera preview. */
  private CameraCaptureSession captureSession;

  /** A reference to the opened {@link CameraDevice}. */
  private CameraDevice cameraDevice;

  /** The {@link android.util.Size} of camera preview. */
  private Size previewSize;

  /** {@link CameraDevice.StateCallback} is called when {@link CameraDevice} changes its state. */
  private final CameraDevice.StateCallback stateCallback =
      new CameraDevice.StateCallback() {

        @Override
        public void onOpened(@NonNull CameraDevice currentCameraDevice) {
          // This method is called when the camera is opened.  We start camera preview here.
          cameraOpenCloseLock.release();
          cameraDevice = currentCameraDevice;
          createCameraPreviewSession();
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice currentCameraDevice) {
          cameraOpenCloseLock.release();
          currentCameraDevice.close();
          cameraDevice = null;
        }

        @Override
        public void onError(@NonNull CameraDevice currentCameraDevice, int error) {
          cameraOpenCloseLock.release();
          currentCameraDevice.close();
          cameraDevice = null;
          Activity activity = getActivity();
          if (null != activity) {
            activity.finish();
          }
        }
      };

  /** An additional thread for running tasks that shouldn't block the UI. */
  private HandlerThread backgroundThread;

  /** A {@link Handler} for running tasks in the background. */
  private Handler backgroundHandler;

  /** An {@link ImageReader} that handles image capture. */
  private ImageReader imageReader;

  /** {@link CaptureRequest.Builder} for the camera preview */
  private CaptureRequest.Builder previewRequestBuilder;

  /** {@link CaptureRequest} generated by {@link #previewRequestBuilder} */
  private CaptureRequest previewRequest;

  /** A {@link Semaphore} to prevent the app from exiting before closing the camera. */
  private Semaphore cameraOpenCloseLock = new Semaphore(1);

  /** A {@link CameraCaptureSession.CaptureCallback} that handles events related to capture. */
  private CameraCaptureSession.CaptureCallback captureCallback =
      new CameraCaptureSession.CaptureCallback() {

        @Override
        public void onCaptureProgressed(
            @NonNull CameraCaptureSession session,
            @NonNull CaptureRequest request,
            @NonNull CaptureResult partialResult) {}

        @Override
        public void onCaptureCompleted(
            @NonNull CameraCaptureSession session,
            @NonNull CaptureRequest request,
            @NonNull TotalCaptureResult result) {}
      };

  /**
   * Shows a {@link Toast} on the UI thread for the classification results.
   *
   * @param s The message to show
   */


  private Executor executor;
  private Uri currentTakePhotoUri;

  private TextView result;
  private Button changeCameraBtn;
 // private Button setActionBtn;
  private ImageView actionImg;

  private Classifier classifier;

  static {
    AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
  }


  private void showToast(final String s) {
    final Activity activity = getActivity();
    if (activity != null) {
      activity.runOnUiThread(
          new Runnable() {
            @Override
            public void run() {
             // result.setText(s);
              Toast.makeText(activity,s,Toast.LENGTH_SHORT).show();
            }
          });
    }
  }

  private void showResult(final String s) {
    final Activity activity = getActivity();
    if (activity != null) {
      activity.runOnUiThread(
              new Runnable() {
                @Override
                public void run() {
                  result.setText(s);
                }
              });
    }
  }

  /**
   * Resizes image.
   *
   * Attempting to use too large a preview size could  exceed the camera bus' bandwidth limitation,
   * resulting in gorgeous previews but the storage of garbage capture data.
   *
   * Given {@code choices} of {@code Size}s supported by a camera, choose the smallest one that is
   * at least as large as the respective texture view size, and that is at most as large as the
   * respective max size, and whose aspect ratio matches with the specified value. If such size
   * doesn't exist, choose the largest one that is at most as large as the respective max size, and
   * whose aspect ratio matches with the specified value.
   *
   * @param choices The list of sizes that the camera supports for the intended output class
   * @param textureViewWidth The width of the texture view relative to sensor coordinate
   * @param textureViewHeight The height of the texture view relative to sensor coordinate
   * @param maxWidth The maximum width that can be chosen
   * @param maxHeight The maximum height that can be chosen
   * @param aspectRatio The aspect ratio
   * @return The optimal {@code Size}, or an arbitrary one if none were big enough
   */
  private static Size chooseOptimalSize(
      Size[] choices,
      int textureViewWidth,
      int textureViewHeight,
      int maxWidth,
      int maxHeight,
      Size aspectRatio) {

    // Collect the supported resolutions that are at least as big as the preview Surface
    List<Size> bigEnough = new ArrayList<>();
    // Collect the supported resolutions that are smaller than the preview Surface
    List<Size> notBigEnough = new ArrayList<>();
    int w = aspectRatio.getWidth();
    int h = aspectRatio.getHeight();
    for (Size option : choices) {
      if (option.getWidth() <= maxWidth
          && option.getHeight() <= maxHeight
          && option.getHeight() == option.getWidth() * h / w) {
        if (option.getWidth() >= textureViewWidth && option.getHeight() >= textureViewHeight) {
          bigEnough.add(option);
        } else {
          notBigEnough.add(option);
        }
      }
    }

    // Pick the smallest of those big enough. If there is no one big enough, pick the
    // largest of those not big enough.
    if (bigEnough.size() > 0) {
      return Collections.min(bigEnough, new CompareSizesByArea());
    } else if (notBigEnough.size() > 0) {
      return Collections.max(notBigEnough, new CompareSizesByArea());
    } else {
      Log.e(TAG, "Couldn't find any suitable preview size");
      return choices[0];
    }
  }

  public static Camera2BasicFragment newInstance() {
    return new Camera2BasicFragment();
  }



  /** Layout the preview and buttons. */
  @Override
  public View onCreateView(
          LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    // 避免耗时任务占用 CPU 时间片造成UI绘制卡顿，提升启动页面加载速度
    Looper.myQueue().addIdleHandler(idleHandler);

    return inflater.inflate(R.layout.fragment_camera2_basic, container, false);

  }


  /**
   *  主线程消息队列空闲时（视图第一帧绘制完成时）处理耗时事件
   */
  MessageQueue.IdleHandler idleHandler = new MessageQueue.IdleHandler() {
    @Override
    public boolean queueIdle() {

      if (classifier == null) {
        // 创建 Classifier
        classifier = TensorFlowImageClassifier.create(getActivity().getAssets(),
                MODEL_FILE, LABEL_FILE, INPUT_SIZE, IMAGE_MEAN, IMAGE_STD, INPUT_NAME, OUTPUT_NAME);
      }

      // 初始化线程池
      executor = new ScheduledThreadPoolExecutor(1, new ThreadFactory() {
        @Override
        public Thread newThread(@NonNull Runnable r) {
          Thread thread = new Thread(r);
          thread.setDaemon(true);
          thread.setName("ThreadPool-ImageClassifier");
          return thread;
        }
      });

      return false;
    }
  };


  /** Connect the buttons to their event handler. */
  @Override
  public void onViewCreated(final View view, final Bundle savedInstanceState) {
    // Get references to widgets.
    textureView = (AutoFitTextureView) view.findViewById(R.id.texture);


    result = view.findViewById(R.id.result);
    actionImg = view.findViewById(R.id.action_image);
    changeCameraBtn = view.findViewById(R.id.change_camera);

    actionImage = view.findViewById(R.id.action_image);
    videoView = view.findViewById(R.id.video_view);


    String uri = "android.resource://" + getActivity().getPackageName() + "/" + R.raw.yoga;
    videoView.setVideoURI(Uri.parse(uri));

      startTimer(2000);

//      //这里是video开始的
//      videoView.setVisibility(View.VISIBLE);
//    videoView.start();
//    //Log.e(TAG, "videoView start");
//    //test
//    startTimer(39000);

    changeCameraBtn.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        Log.e(TAG, "changeCameraBtn:" );
        isFrontCamera = !isFrontCamera;
        closeCamera();
        stopBackgroundThread();
        System.gc();

        openCamera(textureView.getWidth(),textureView.getHeight());
        startBackgroundThread();
      }
    });
    // Start initial model.
  }


  /** Load the model and labels. */
  @Override
  public void onActivityCreated(Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);
  }

  @Override
  public void onResume() {
    super.onResume();
    startBackgroundThread();

    // When the screen is turned off and turned back on, the SurfaceTexture is already
    // available, and "onSurfaceTextureAvailable" will not be called. In that case, we can open
    // a camera and start preview from here (otherwise, we wait until the surface is ready in
    // the SurfaceTextureListener).
    if (textureView.isAvailable()) {
      openCamera(textureView.getWidth(), textureView.getHeight());
    } else {
      textureView.setSurfaceTextureListener(surfaceTextureListener);
    }
  }

  @Override
  public void onPause() {
    closeCamera();
    stopBackgroundThread();
    super.onPause();
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
//    RefWatcher refWatcher = MyApplication.getRefWatcher(getActivity());
//    refWatcher.watch(this);
  }

  /**
   * Sets up member variables related to camera.
   *
   * @param width The width of available size for camera preview
   * @param height The height of available size for camera preview
   */
  private void setUpCameraOutputs(int width, int height,boolean isFrontCamera) {
    Activity activity = getActivity();
    CameraManager manager = (CameraManager) activity.getSystemService(Context.CAMERA_SERVICE);
    try {
      for (String cameraId : manager.getCameraIdList()) {
        CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);

        // We don't use a front facing camera in this sample.
        Integer facing = characteristics.get(CameraCharacteristics.LENS_FACING);
        if (isFrontCamera && facing != null && facing == CameraCharacteristics.LENS_FACING_BACK) {
          continue;
        }

        if (!isFrontCamera && facing != null && facing == CameraCharacteristics.LENS_FACING_FRONT) {
          continue;
        }

        StreamConfigurationMap map =
            characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
        if (map == null) {
          continue;
        }

        // // For still image captures, we use the largest available size.
        Size largest =
            Collections.max(
                Arrays.asList(map.getOutputSizes(ImageFormat.JPEG)), new CompareSizesByArea());
        imageReader =
            ImageReader.newInstance(
                largest.getWidth(), largest.getHeight(), ImageFormat.JPEG, /*maxImages*/ 2);

        // Find out if we need to swap dimension to get the preview size relative to sensor
        // coordinate.
        int displayRotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        // noinspection ConstantConditions
        /* Orientation of the camera sensor */
        int sensorOrientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
        boolean swappedDimensions = false;
        switch (displayRotation) {
          case Surface.ROTATION_0:
          case Surface.ROTATION_180:
            if (sensorOrientation == 90 || sensorOrientation == 270) {
              swappedDimensions = true;
            }
            break;
          case Surface.ROTATION_90:
          case Surface.ROTATION_270:
            if (sensorOrientation == 0 || sensorOrientation == 180) {
              swappedDimensions = true;
            }
            break;
          default:
            Log.e(TAG, "Display rotation is invalid: " + displayRotation);
        }

        Point displaySize = new Point();
        activity.getWindowManager().getDefaultDisplay().getSize(displaySize);
        int rotatedPreviewWidth = width;
        int rotatedPreviewHeight = height;
        int maxPreviewWidth = displaySize.x;
        int maxPreviewHeight = displaySize.y;

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

        previewSize =
            chooseOptimalSize(
                map.getOutputSizes(SurfaceTexture.class),
                rotatedPreviewWidth,
                rotatedPreviewHeight,
                maxPreviewWidth,
                maxPreviewHeight,
                largest);

        // We fit the aspect ratio of TextureView to the size of preview we picked.
        int orientation = getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
          textureView.setAspectRatio(previewSize.getWidth(), previewSize.getHeight());
        } else {
          textureView.setAspectRatio(previewSize.getHeight(), previewSize.getWidth());
        }

        this.cameraId = cameraId;
        Log.e(TAG, "cameraId: "+cameraId );
        return;
      }
    } catch (CameraAccessException e) {
      Log.e(TAG, "Failed to access Camera", e);
    } catch (NullPointerException e) {
      // Currently an NPE is thrown when the Camera2API is used but not supported on the
      // device this code runs.
      ErrorDialog.newInstance(getString(R.string.camera_error))
          .show(getChildFragmentManager(), FRAGMENT_DIALOG);
    }
  }

  private String[] getRequiredPermissions() {
    Activity activity = getActivity();
    try {
      PackageInfo info =
          activity
              .getPackageManager()
              .getPackageInfo(activity.getPackageName(), PackageManager.GET_PERMISSIONS);
      String[] ps = info.requestedPermissions;
      if (ps != null && ps.length > 0) {
        return ps;
      } else {
        return new String[0];
      }
    } catch (Exception e) {
      return new String[0];
    }
  }

  /** Opens the camera specified by {@link Camera2BasicFragment#cameraId}. */
  private void openCamera(int width, int height) {
    if (!checkedPermissions && !allPermissionsGranted()) {
      FragmentCompat.requestPermissions(this, getRequiredPermissions(), PERMISSIONS_REQUEST_CODE);
      return;
    } else {
      checkedPermissions = true;
    }
    setUpCameraOutputs(width, height,isFrontCamera);
    configureTransform(width, height);
    Activity activity = getActivity();
    CameraManager manager = (CameraManager) activity.getSystemService(Context.CAMERA_SERVICE);
    try {
      if (!cameraOpenCloseLock.tryAcquire(2500, TimeUnit.MILLISECONDS)) {
        throw new RuntimeException("Time out waiting to lock camera opening.");
      }
      manager.openCamera(cameraId, stateCallback, backgroundHandler);
    } catch (CameraAccessException e) {
      Log.e(TAG, "Failed to open Camera", e);
    } catch (InterruptedException e) {
      throw new RuntimeException("Interrupted while trying to lock camera opening.", e);
    }
  }

  private boolean allPermissionsGranted() {
    for (String permission : getRequiredPermissions()) {
      if (ContextCompat.checkSelfPermission(getActivity(), permission)
          != PackageManager.PERMISSION_GRANTED) {
        return false;
      }
    }
    return true;
  }

  @Override
  public void onRequestPermissionsResult(
          int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
  }

  /** Closes the current {@link CameraDevice}. */
  private void closeCamera() {
    try {
      cameraOpenCloseLock.acquire();
      if (null != captureSession) {
        captureSession.close();
        captureSession = null;
      }
      if (null != cameraDevice) {
        cameraDevice.close();
        cameraDevice = null;
      }
      if (null != imageReader) {
        imageReader.close();
        imageReader = null;
      }
    } catch (InterruptedException e) {
      throw new RuntimeException("Interrupted while trying to lock camera closing.", e);
    } finally {
      cameraOpenCloseLock.release();
    }
  }

  /** Starts a background thread and its {@link Handler}. */
  private void startBackgroundThread() {
    backgroundThread = new HandlerThread(HANDLE_THREAD_NAME);
    backgroundThread.start();
    backgroundHandler = new Handler(backgroundThread.getLooper());
    // Start the classification train & load an initial model.
//    synchronized (lock) {
//      runClassifier = true;
//    }
    backgroundHandler.post(periodicClassify);
  }

  /** Stops the background thread and its {@link Handler}. */
  private void stopBackgroundThread() {
    backgroundThread.quitSafely();
    try {
      backgroundThread.join();
      backgroundThread = null;
      backgroundHandler = null;
      synchronized (lock) {
        runClassifier = false;
      }
    } catch (InterruptedException e) {
      Log.e(TAG, "Interrupted when stopping background thread", e);
    }
  }

  /** Takes photos and classify them periodically. */
  private Runnable periodicClassify =
      new Runnable() {
        @Override
        public void run() {
          synchronized (lock) {
            if (runClassifier) {
              classifyFrame();
            }
          }
          backgroundHandler.postDelayed(periodicClassify,500);
        }
      };

  /** Classifies a frame from the preview stream. */
  private void classifyFrame() {
      Log.e(TAG, "classifyFrame: ");
    if (classifier == null || getActivity() == null || cameraDevice == null) {
      // It's important to not call showToast every frame, or else the app will starve and
      // hang. updateActiveModel() already puts a error message up with showToast.
      // showToast("Uninitialized Classifier or invalid context.");
      return;
    }
    Bitmap bitmap = textureView.getBitmap(INPUT_SIZE, INPUT_SIZE);
    startImageClassifier(bitmap);

  }

  /**
   * 旋转图片
   * @param bitmap 要处理的Bitmap
   * @return 处理后的Bitmap
   */
  public static Bitmap rotaingImageView(Bitmap bitmap) {
    // 旋转图片 动作
    Matrix matrix = new Matrix();
    matrix.postRotate(90);
    // 创建新的图片
    Bitmap resizedBitmap = Bitmap.createBitmap(bitmap, 0, 0,
            bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    if (resizedBitmap != bitmap && bitmap != null && !bitmap.isRecycled()){
      bitmap.recycle();
      bitmap = null;
    }
    return resizedBitmap;
  }

  /**
   * 从Assets中读取图片
   */
  private Bitmap getImageFromAssetsFile(String fileName) {
    Bitmap image = null;
    AssetManager am = getResources().getAssets();
    try {
      InputStream is = am.open(fileName);
      image = BitmapFactory.decodeStream(is);
      is.close();
    }
    catch (IOException e) {
      e.printStackTrace();
    }
    return image;
  }

  /** Creates a new {@link CameraCaptureSession} for camera preview. */
  private void createCameraPreviewSession() {
    try {
      SurfaceTexture texture = textureView.getSurfaceTexture();
      assert texture != null;

      // We configure the size of default buffer to be the size of camera preview we want.
      texture.setDefaultBufferSize(previewSize.getWidth(), previewSize.getHeight());

      // This is the output Surface we need to start preview.
      Surface surface = new Surface(texture);

      // We set up a CaptureRequest.Builder with the output Surface.
      previewRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
      previewRequestBuilder.addTarget(surface);

      // Here, we create a CameraCaptureSession for camera preview.
      cameraDevice.createCaptureSession(
          Arrays.asList(surface),
          new CameraCaptureSession.StateCallback() {

            @Override
            public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
              // The camera is already closed
              if (null == cameraDevice) {
                return;
              }

              // When the session is ready, we start displaying the preview.
              captureSession = cameraCaptureSession;
              try {
                // Auto focus should be continuous for camera preview.
                previewRequestBuilder.set(
                    CaptureRequest.CONTROL_AF_MODE,
                    CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);

                // Finally, we start displaying the camera preview.
                previewRequest = previewRequestBuilder.build();
                captureSession.setRepeatingRequest(
                    previewRequest, captureCallback, backgroundHandler);
              } catch (CameraAccessException e) {
                Log.e(TAG, "Failed to set up config to capture Camera", e);
              }
            }

            @Override
            public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
              showToast("Failed");
            }
          },
          null);
    } catch (CameraAccessException e) {
      Log.e(TAG, "Failed to preview Camera", e);
    }
  }

  /**
   * Configures the necessary {@link android.graphics.Matrix} transformation to `textureView`. This
   * method should be called after the camera preview size is determined in setUpCameraOutputs and
   * also the size of `textureView` is fixed.
   *
   * @param viewWidth The width of `textureView`
   * @param viewHeight The height of `textureView`
   */
  private void configureTransform(int viewWidth, int viewHeight) {
    Activity activity = getActivity();
    if (null == textureView || null == previewSize || null == activity) {
      return;
    }
    int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
    Matrix matrix = new Matrix();
    RectF viewRect = new RectF(0, 0, viewWidth, viewHeight);
    RectF bufferRect = new RectF(0, 0, previewSize.getHeight(), previewSize.getWidth());
    float centerX = viewRect.centerX();
    float centerY = viewRect.centerY();
    if (Surface.ROTATION_90 == rotation || Surface.ROTATION_270 == rotation) {
      bufferRect.offset(centerX - bufferRect.centerX(), centerY - bufferRect.centerY());
      matrix.setRectToRect(viewRect, bufferRect, Matrix.ScaleToFit.FILL);
      float scale =
          Math.max(
              (float) viewHeight / previewSize.getHeight(),
              (float) viewWidth / previewSize.getWidth());
      matrix.postScale(scale, scale, centerX, centerY);
      matrix.postRotate(90 * (rotation - 2), centerX, centerY);
    } else if (Surface.ROTATION_180 == rotation) {
      matrix.postRotate(180, centerX, centerY);
    }
    textureView.setTransform(matrix);
  }


  /** Compares two {@code Size}s based on their areas. */
  private static class CompareSizesByArea implements Comparator<Size> {

    @Override
    public int compare(Size lhs, Size rhs) {
      // We cast here to ensure the multiplications won't overflow
      return Long.signum(
          (long) lhs.getWidth() * lhs.getHeight() - (long) rhs.getWidth() * rhs.getHeight());
    }
  }

  /** Shows an error message dialog. */
  public static class ErrorDialog extends DialogFragment {

    private static final String ARG_MESSAGE = "message";

    public static ErrorDialog newInstance(String message) {
      ErrorDialog dialog = new ErrorDialog();
      Bundle args = new Bundle();
      args.putString(ARG_MESSAGE, message);
      dialog.setArguments(args);
      return dialog;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
      final Activity activity = getActivity();
      return new AlertDialog.Builder(activity)
          .setMessage(getArguments().getString(ARG_MESSAGE))
          .setPositiveButton(
              android.R.string.ok,
              new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                  activity.finish();
                }
              })
          .create();
    }
  }


  /**
   * 开始图片识别匹配
   * @param bitmap
   */
  private void startImageClassifier(final Bitmap bitmap) {
    if(executor == null)
      Log.e(TAG,"executor null");

    executor.execute(new Runnable() {
      @Override
      public void run() {
        try {
          Log.i(TAG, Thread.currentThread().getName() + " startImageClassifier");
         // Bitmap croppedBitmap = getScaleBitmap(bitmap, INPUT_SIZE);

          final List<Classifier.Recognition> results = classifier.recognizeImage(bitmap);

//          showResult(String.format("results: %s", results));
          countScore(results);

        }catch (Exception e){
          e.printStackTrace();
        }finally {
          bitmap.recycle();
        }
      }
    });
  }

    private void countScore(List<Classifier.Recognition> result) {
      Log.e(TAG, String.valueOf(curPic));
      for(Classifier.Recognition recognition: result){
          Log.e(TAG, recognition.getId() + recognition.getConfidence());
      }

      Log.e(TAG, "countScore: "+result.get(0).getId());

      if(curPic == 1){
          showResult("downward"+" "+result.get(0).getId() +""+
                  result.get(0).getTitle()+" " +
                  result.get(0).getConfidence());

      }else if(curPic == 2){
          showResult("plank"+" "+result.get(0).getId() +""+
                  result.get(0).getTitle()+" " +
                  result.get(0).getConfidence());

      }else if(curPic == 3){
          showResult("chaturanga"+" "+result.get(0).getId() +""+
                  result.get(0).getTitle()+" " +
                  result.get(0).getConfidence());
      }
      else if(curPic == 4){
          showResult("forward"+" "+result.get(0).getId() +""+
                  result.get(0).getTitle()+" " +
                  result.get(0).getConfidence());
      }
      else if(curPic == 5){
            showResult("huanyi"+" "+result.get(0).getId() +""+
                    result.get(0).getTitle()+" " +
                    result.get(0).getConfidence());
      }
      else if(curPic == 6){
          showResult("niu"+" "+result.get(0).getId() +""+
                  result.get(0).getTitle()+" " +
                  result.get(0).getConfidence());
      }
      else if(curPic == 7){
          showResult("high"+" "+result.get(0).getId() +""+
                  result.get(0).getTitle()+" " +
                  result.get(0).getConfidence());
      }
      else if(curPic == 8){
          showResult("xinyue"+" "+result.get(0).getId() +""+
                  result.get(0).getTitle()+" " +
                  result.get(0).getConfidence());
      }
      else if(curPic == 9){
          showResult("low"+" "+result.get(0).getId() +""+
                  result.get(0).getTitle()+" " +
                  result.get(0).getConfidence());
      }
      else if(curPic == 0){
          showResult("picha"+""+result.get(0).getId() +""+
                  result.get(0).getTitle()+" " +
                  result.get(0).getConfidence());
      }

      if(result.get(0).getId().equals("1") && curPic == 1){
          score[curPic]  = Math.max(score[curPic],result.get(0).getConfidence()) ;
          showToast("excellent!!!" + result.get(0).getConfidence());
      }else if(result.get(0).getId().equals("7") && curPic == 2){
          score[curPic]  = Math.max(score[curPic],result.get(0).getConfidence()) ;
          showToast("excellent!!!" + result.get(0).getConfidence());
      }else if(result.get(0).getId().equals("0") && curPic == 3){
          score[curPic]  = Math.max(score[curPic],result.get(0).getConfidence()) ;
          showToast("excellent!!!" + result.get(0).getConfidence());
      }else if(result.get(0).getId().equals("2") && curPic == 4){
          score[curPic]  = Math.max(score[curPic],result.get(0).getConfidence()) ;
          showToast("excellent!!!" + result.get(0).getConfidence());
      }else if(result.get(0).getId().equals("4") && curPic == 5){
          score[curPic]  = Math.max(score[curPic],result.get(0).getConfidence()) ;
          showToast("excellent!!!" + result.get(0).getConfidence());
      }else if(result.get(0).getId().equals("5") && curPic == 6){
          score[curPic]  = Math.max(score[curPic],result.get(0).getConfidence()) ;
          showToast("excellent!!!" + result.get(0).getConfidence());
      }else if(result.get(0).getId().equals("3") && curPic == 7){
          score[curPic]  = Math.max(score[curPic],result.get(0).getConfidence()) ;
          showToast("excellent!!!" + result.get(0).getConfidence());
      }else if(result.get(0).getId().equals("9") && curPic == 8){
          score[curPic]  = Math.max(score[curPic],result.get(0).getConfidence()) ;
          showToast("excellent!!!" + result.get(0).getConfidence());
      }else if(result.get(0).getId().equals("8") && curPic == 9){
          score[curPic]  = Math.max(score[curPic],result.get(0).getConfidence()) ;
          showToast("excellent!!!" + result.get(0).getConfidence());
      }else if(result.get(0).getId().equals("6") && curPic == 0){
          score[curPic]  = Math.max(score[curPic],result.get(0).getConfidence()) ;
          showToast("excellent!!!" + result.get(0).getConfidence());
      }
    }

    /**
   * 对图片进行缩放
   * @param bitmap
   * @param size
   * @return
   * @throws IOException
   */
  private static Bitmap getScaleBitmap(Bitmap bitmap, int size) throws IOException {
    int width = bitmap.getWidth();
    int height = bitmap.getHeight();
    float scaleWidth = ((float) size) / width;
    float scaleHeight = ((float) size) / height;
    Matrix matrix = new Matrix();
    matrix.postScale(scaleWidth, scaleHeight);
    return Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
  }

}
