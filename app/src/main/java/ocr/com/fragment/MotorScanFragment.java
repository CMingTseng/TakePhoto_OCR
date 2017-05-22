package ocr.com.fragment;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import idv.neo.utils.FolderFileUtils;
import idv.neo.widget.Guides;
import ocr.com.BuildConfig;
import ocr.com.C;
import ocr.com.R;

//http://mjbb.iteye.com/blog/1018006
public class MotorScanFragment extends Fragment {
    private final static String TAG = "MotorScanFragment";
    private Camera mCamera;
    private int cnt = 1;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        onHiddenChanged(false);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final Context context = container.getContext();
        final View root = inflater.inflate(R.layout.fragment_motorscan, container, false);
        //-------呼叫Guides class 對 指定layout 作 addView(mGuides)畫框框---------------------
        final Guides guidesuides = new Guides(context);
        ((RelativeLayout) root.findViewById(R.id.camerarl02)).addView(guidesuides);
        guidesuides.getLayoutParams().width = RelativeLayout.LayoutParams.FILL_PARENT;
        guidesuides.getLayoutParams().height = RelativeLayout.LayoutParams.FILL_PARENT;

//--------- SurfaceHolder設定---------------------------------------------------
        final SurfaceView surfaceView = (SurfaceView) root.findViewById(R.id.surfaceview);
        final ImageButton takephoto = (ImageButton) root.findViewById(R.id.cameraImgBtn01);
        final SurfaceHolder holder = surfaceView.getHolder();
        holder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                Log.d(TAG, "surfaceCreated");
                try {
                    mCamera = Camera.open();
                    if (mCamera != null) {
                        mCamera.setPreviewDisplay(holder);
                        takephoto.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                         /* 告動對焦後拍照 */
                                mCamera.autoFocus(new Camera.AutoFocusCallback() {
                                    /**
                                     * 當自動對焦時候調用
                                     */
                                    @Override
                                    public void onAutoFocus(boolean success, Camera camera) {
                                    /* 對到焦點拍照 */
                                        if (success) {
                                            takePicture();
                                        }
                                    }
                                });
                            }
                        });
                    }
                } catch (IOException exception) {
                    mCamera.release();
                    mCamera = null;
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            /* 相機初始化 */
                Log.d(TAG, "surfaceChanged");
                initCamera();
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                Log.d(TAG, "surfaceDestroyed");
                if (mCamera != null) {
                    stopCamera();
                    mCamera.release();
                    mCamera = null;
                }
            }
        });
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        return root;
    }

    /* 相機初始化的method */
    private void initCamera() {
        try {
            Camera.Parameters parameters = mCamera.getParameters();
           /*
            * 設定相片 格式為JPG
            *
			*/
            parameters.setPictureFormat(PixelFormat.JPEG);
            final List<Camera.Size> previewSizes = parameters.getSupportedPreviewSizes();
            for (Camera.Size size : previewSizes) {
                if (size != null) {
           /*
            * 設定相片大小為1024*768
            *  parameters.setPictureSize(1024, 768);//FIXME　will get setParameters failed not use  need get support size
			*/
                    parameters.setPictureSize(size.width, size.height);
                    break;
                }
            }
            mCamera.setParameters(parameters);
           /* 開啟預覽畫面 */
            mCamera.startPreview();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /* 停止相機的method */
    private void stopCamera() {
        try {
            /* 停止預覽 */
            mCamera.stopPreview();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 當拍攝相片的時候調用，該介面具有一個void onPictureTaken(byte[] data,Camera camera)函數;
     * 參數和預覽的一樣。在android中主要有三個類實現了這個介面，
     * 分別是PostViewPictureCallback、 	RawPictureCallback、
     * JepgPictureCallback。我們可以根據需要定義自己需要的類
     */
    private void takePicture() {
        if (mCamera != null) {
            Log.i(TAG, "takePicture");
            mCamera.takePicture(shutterCallback, rawCallback, jpegCallback);
        }
    }

    /**
     * 在圖像預覽的時候調用，這個介面具有一個void onShutter();可以在改函數中通知使用者快門已經關閉，例如播放一個聲音
     */
    private Camera.ShutterCallback shutterCallback = new Camera.ShutterCallback() {
        public void onShutter() {

        }
    };
    private Camera.PictureCallback rawCallback = new Camera.PictureCallback() {
        public void onPictureTaken(byte[] _data, Camera _camera) {
        }
    };

    private Camera.PictureCallback jpegCallback = new Camera.PictureCallback() {
        public void onPictureTaken(byte[] _data, Camera _camera) {
            /* 取得相仞 */
            try {
                savePhoto(BitmapFactory.decodeByteArray(_data, 0, _data.length));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    };

    /**
     * 直接儲存並切換至picture.class 並將裁切好之圖片存為camera2.jpg
     */
    private void savePhoto(Bitmap bmp) {
        Context context = getContext();
        Bundle arguments = new Bundle();
        if (bmp != null) {
        /* 檢查SDCard是否存在 */
            if (!FolderFileUtils.checkSDCardExist()) {
            /* SD卡不存在，顯示Toast資訊 */
                Toast.makeText(context, "SD卡不存在!無法保存相片,請插入SD卡。", Toast.LENGTH_LONG).show();
            } else {
                try {
                /* 檢查資料夾是否存在不存在就創建 */
                    if (!!FolderFileUtils.checkExternalFolderFileExist(File.separator + BuildConfig.OCRPHOTOFOLDER)) {
                        FolderFileUtils.createFolderFile(File.separator + BuildConfig.OCRPHOTOFOLDER);
                    }
                 /* 保存相片檔 String.valueOf( Calendar.getInstance().get(Calendar.MILLISECOND))+ */
                    File n = new File(FolderFileUtils.getSDPath(), File.separator + BuildConfig.OCRPHOTOFOLDER + File.separator + BuildConfig.TEMPPHOTOFILE);
                    arguments.putString(C.PHOTO_PATH, n.toString());
                    FileOutputStream bos = new FileOutputStream(n.getAbsolutePath());
                /* 檔轉換 */
                    bmp.compress(Bitmap.CompressFormat.JPEG, 100, bos);
                /* 調用flush()方法，更新BufferStream */
                    bos.flush();
                /* 結束OutputStream */
                    bos.close();
                    //Toast.makeText(MotorScanActivity.this,path1 + "保存成功!", Toast.LENGTH_LONG).show();
                    cnt++;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        //另存辨識用裁切圖片
//        final File sdcardTempFile = new File(FolderFileUtils.getSDPath(), File.separator + BuildConfig.OCRPHOTOFOLDER + File.separator + BuildConfig.TEMPPHOTOFILE);
//        //bitmap 設置圖片尺寸，避免 記憶體溢出 OutOfMemoryError的優化方法
//        final BitmapFactory.Options options = new BitmapFactory.Options();
//        options.inSampleSize = 2;
//        Bitmap tempbitmap = BitmapFactory.decodeFile(sdcardTempFile.getAbsolutePath());
//        int w = tempbitmap.getWidth();
//        int h = tempbitmap.getHeight();
//        int[] pixels = new int[w * h];
//        for (int i = 0; i < w * h; i++) {
//            pixels[i] = -1000000;
//        }
//
//	    /*getPixels(int[] pixels, int offset, int stride,int x, int y, int width, int height)
//	        pixels圖片背景色,offset偏移量,stride須大於bmp的寬度可為負值
//	        x 坐標 , y 坐標, width bmp的寬度,height bmp的高度     */
//
//        tempbitmap.getPixels(pixels, 0, w, w / 2, 0, 300, h);
//        tempbitmap = Bitmap.createBitmap(pixels, 0, w, 300, h,Bitmap.Config.ARGB_8888);
        // Bitmap 旋轉
        Matrix matrix = new Matrix();
        matrix.setRotate(90);

        Bitmap vB2 = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth()   // 寬度
                , bmp.getHeight()  // 高度
                , matrix, true);
        try {
            // 輸出的圖檔位置
            File f = new File(FolderFileUtils.getSDPath(), File.separator + BuildConfig.OCRPHOTOFOLDER + File.separator + BuildConfig.OCRPHOTOFILE);
            FileOutputStream fos = new FileOutputStream(f);
            arguments.putString(C.OCR_PATH, f.toString());
            // 將 Bitmap 儲存成 PNG / JPEG 檔案格式
            vB2.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            // 釋放
            fos.close();
        } catch (IOException e) {
        }
        Fragment fragment = new OCRResultFragment();
        fragment.setArguments(arguments);
        getFragmentManager().beginTransaction()
                .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out, android.R.anim.fade_in, android.R.anim.fade_out)
                .addToBackStack(MotorScanFragment.class.getSimpleName())
                .replace(R.id.main_content, fragment)
                .commit();
    }
}