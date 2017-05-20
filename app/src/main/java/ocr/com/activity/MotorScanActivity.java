package ocr.com.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.googlecode.tesseract.android.TessBaseAPI;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;

import idv.neo.utils.BitmapUtils;
import idv.neo.widget.Guides;
import ocr.com.R;

//http://mjbb.iteye.com/blog/1018006
public class MotorScanActivity extends Activity implements SurfaceHolder.Callback {
    private static String TAG = "MotorScanActivity";
    private Camera mCamera;
    private ImageButton cameraImgBtn01;
    //	private ImageButton camImgBtn01;
    private SurfaceView mSurfaceView;
    private SurfaceHolder holder;
    private AutoFocusCallback mAutoFocusCallback = new AutoFocusCallback();
    private String path = "OCR-Photo";//SD Card的目錄
    private String path1;//照片名稱
    private Bitmap bmp, bmpt;
    private int cnt = 1;
    private Calendar c;

    Guides mGuides;//畫線
    private File sdcardTempFile;
    private static final String LOGGER = MotorScanActivity.class.getName();
    //OCR處理部分
    public static Handler ocrHandler;
    private static String LANGUAGE = "eng";
    private static EditText etResult;
    private static ImageView ivSelected;
    private static ImageView ivTreated;
    private static String textResult;
    private static Bitmap bitmapSelected;
    private static Bitmap bitmapTreated;
    private static final int SHOWRESULT = 0x101;
    private static final int SHOWTREATEDIMG = 0x102;
    private static Button tessmotor001, tessmotor002;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.camera1);
//-------呼叫Guides class 對 指定layout 作 addView(mGuides)畫框框---------------------
        mGuides = new Guides(this);
        ((RelativeLayout) findViewById(R.id.camerarl02)).addView(mGuides);
        mGuides.getLayoutParams().width = LayoutParams.FILL_PARENT;
        mGuides.getLayoutParams().height = LayoutParams.FILL_PARENT;

//--------- SurfaceHolder設定---------------------------------------------------
        mSurfaceView = (SurfaceView) findViewById(R.id.mSurfaceView);
        holder = mSurfaceView.getHolder();
        holder.addCallback(MotorScanActivity.this);
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        c = Calendar.getInstance();
        //--------- Button初始化---------------------------------------------------
        cameraImgBtn01 = (ImageButton) findViewById(R.id.cameraImgBtn01);
        cameraImgBtn01.setOnClickListener(CameraBtn01Click);
    }

    /* 拍照Button的事件處理 */
    private Button.OnClickListener CameraBtn01Click = new Button.OnClickListener() {

        @Override
        public void onClick(View v) {
            // TODO Auto-generated method stub
                    /* 告動對焦後拍照 */
            mCamera.autoFocus(mAutoFocusCallback);
        }
    };

    /**
     * 當自動對焦時候調用
     */
    public final class AutoFocusCallback implements android.hardware.Camera.AutoFocusCallback {
        public void onAutoFocus(boolean focused, Camera camera) {
            /* 對到焦點拍照 */
            if (focused) {
                takePicture();
            }
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
    private ShutterCallback shutterCallback = new ShutterCallback() {
        public void onShutter() {

        }
    };

    private PictureCallback rawCallback = new PictureCallback() {
        public void onPictureTaken(byte[] _data, Camera _camera) {
        }
    };

    private PictureCallback jpegCallback = new PictureCallback() {
        public void onPictureTaken(byte[] _data, Camera _camera) {
            /* 取得相仞 */
            try {
                bmp = BitmapFactory.decodeByteArray(_data, 0, _data.length);
                btn2();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    };

    /**
     * 直接儲存並切換至picture.class 並將裁切好之圖片存為camera2.jpg
     */
    private void btn2() {
        Log.i(TAG, "click button2");
        if (bmp != null) {
		/* 檢查SDCard是否存在 */
            if (!Environment.MEDIA_MOUNTED.equals(Environment
                    .getExternalStorageState())) {
			/* SD卡不存在，顯示Toast資訊 */
                Toast.makeText(MotorScanActivity.this,
                        "SD卡不存在!無法保存相片,請插入SD卡。", Toast.LENGTH_LONG)
                        .show();
            } else {
                try {
				/* 檔不存在就創建 */
                    File f = new File(Environment
                            .getExternalStorageDirectory(), path);
                    Log.i(TAG, "click button2:" + f.getAbsolutePath());
                    if (!f.exists()) {
                        f.mkdir();
                    }
				/* 保存相片檔 String.valueOf(c.get(Calendar.MILLISECOND))+ */
                    path1 = "camera.jpg";
                    File n = new File(f, path1);
                    FileOutputStream bos = new FileOutputStream(n
                            .getAbsolutePath());
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
	/* 重新設定Camera */
        stopCamera();
        initCamera();
        sdcardTempFile = new File("/mnt/sdcard/OCR-Photo/camera.jpg");
        //bitmap 設置圖片尺寸，避免 記憶體溢出 OutOfMemoryError的優化方法
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 2;
        bmpt = BitmapFactory.decodeFile(sdcardTempFile.getAbsolutePath());
        int w = bmpt.getWidth();
        int h = bmpt.getHeight();
        int[] pixels = new int[w * h];
        for (int i = 0; i < w * h; i++) {
            pixels[i] = -1000000;
        }
	    /*getPixels(int[] pixels, int offset, int stride, 
	                int x, int y, int width, int height)
	        pixels圖片背景色,offset偏移量,stride須大於bmp的寬度可為負值
	        x 坐標 , y 坐標, width bmp的寬度,height bmp的高度     */

        bmpt.getPixels(pixels, 0, w, w / 2, 0, 300, h);
        bmpt = Bitmap.createBitmap(pixels, 0, w, 300, h,
                Bitmap.Config.ARGB_8888);
        // Bitmap 旋轉
        Matrix vMatrix = new Matrix();
        vMatrix.setRotate(90);

        Bitmap vB2 = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth()   // 寬度
                , bmp.getHeight()  // 高度
                , vMatrix, true);
        try {
            // 輸出的圖檔位置
            FileOutputStream fos = new FileOutputStream("/mnt/sdcard/OCR-Photo/camera2.jpg");
            // 將 Bitmap 儲存成 PNG / JPEG 檔案格式
            vB2.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            // 釋放
            fos.close();
        } catch (IOException e) {
        }
        ocrHandler = new Handler() {

            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case SHOWRESULT:
                        if (textResult.equals("")) {
                            etResult.setText("識別失敗");
                            tessmotor001.setText("識別失敗");
                            tessmotor001.setEnabled(false);
                        } else {
                            etResult.setText(textResult);
                            if (etResult.getText().length() < 10 && etResult.getText().length() > 6) {
                                tessmotor001.setText("新增或註冊");
                                tessmotor001.setEnabled(true);

                            } else {
                                tessmotor001.setText("識別失敗");
                                tessmotor001.setEnabled(false);
                            }
                        }
                        break;
                    case SHOWTREATEDIMG:
                        etResult.setText("識別中......");
                        tessmotor001.setText("識別中......");
                        tessmotor001.setEnabled(false);
                        showPicture(ivTreated, bitmapTreated);
                        break;
                }
                super.handleMessage(msg);
            }


        };
        ////設置完成Handler後讓OCR辨識時的提示UI出現.改用Thread應該可免UI
        setContentView(R.layout.tessmotor);
        etResult = (EditText) findViewById(R.id.tessmotoret001);
        ivSelected = (ImageView) findViewById(R.id.iv_selected);
        ivTreated = (ImageView) findViewById(R.id.iv_treated);
        tessmotor001 = (Button) findViewById(R.id.tessmotorbt001);
        tessmotor002 = (Button) findViewById(R.id.tessmotorbt002);
        tessmotor001.setEnabled(false);

        tessmotor001.setOnClickListener(tess1OnClick);
        tessmotor002.setOnClickListener(tess2OnClick);
        tessmotor001.setEnabled(false);
        Ocr();
    }

    private void Ocr() {
        bitmapSelected = BitmapFactory.decodeFile("/mnt/sdcard/OCR-Photo/camera2.jpg");
        // 顯示選擇圖片
        showPicture(ivSelected, bitmapSelected);
        // 處理識別的緒
        new Thread(new Runnable() {
            @Override
            public void run() {
                bitmapTreated = BitmapUtils.converyToGrayImg(bitmapSelected);
                Message msg = new Message();
                msg.what = SHOWTREATEDIMG;
                ocrHandler.sendMessage(msg);
                textResult = doOcr(bitmapTreated, LANGUAGE);
                Message msg2 = new Message();
                msg2.what = SHOWRESULT;
                ocrHandler.sendMessage(msg2);
            }
        }).start();
    }

    // 將圖片顯示在view中
    public static void showPicture(ImageView iv, Bitmap bmp) {
        iv.setImageBitmap(bmp);
    }

    public String doOcr(Bitmap bitmap, String language) {
        TessBaseAPI baseApi = new TessBaseAPI();
        baseApi.init("/mnt/sdcard/OCR-Photo/", language);
        //baseApi.init(getSDPath(), language);
        // tess-two要求BMP須有此配置
        bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        baseApi.setImage(bitmap);//識別圖片
        String text = baseApi.getUTF8Text();//識別語言
        baseApi.clear();
        baseApi.end();
        return text;//回傳結果
    }

    /**
     * 新增或修改紀錄
     */
    Button.OnClickListener tess1OnClick = new Button.OnClickListener() {
        @Override
        public void onClick(View v) {
            // TODO Auto-generated method stub
            Intent intent = new Intent();
            String str = etResult.getText().toString();
            intent.putExtra("MotorID", str);
            intent.setClass(MotorScanActivity.this, MainActivity.class);
            startActivity(intent);
        }
    };

    /**
     * 重新拍照
     */
    Button.OnClickListener tess2OnClick = new Button.OnClickListener() {

        @Override
        public void onClick(View v) {
            MotorScanActivity.this.finish();
        }
    };

    @Override
    public void surfaceCreated(SurfaceHolder surfaceholder) {
        try {
				/* 打開相機， */
            mCamera = Camera.open();
            mCamera.setPreviewDisplay(holder);

        } catch (IOException exception) {
            mCamera.release();
            mCamera = null;
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceholder, int format, int w, int h) {
			/* 相機初始化 */
        Log.i(TAG, "init camera");
        initCamera();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceholder) {
        Log.i(TAG, "destoryed camera");
        stopCamera();
        mCamera.release();
        mCamera = null;
    }

    /* 相機初始化的method */
    private void initCamera() {
        if (mCamera != null) {
            try {
                Camera.Parameters parameters = mCamera.getParameters();
					/*
					 * 設定相片大小為1024*768， 格式為JPG
					 */
                parameters.setPictureFormat(PixelFormat.JPEG);
                parameters.setPictureSize(1024, 768);
                mCamera.setParameters(parameters);
					/* 開啟預覽畫面 */
                mCamera.startPreview();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /* 停止相機的method */
    private void stopCamera() {
        if (mCamera != null) {
            try {
					/* 停止預覽 */
                mCamera.stopPreview();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
