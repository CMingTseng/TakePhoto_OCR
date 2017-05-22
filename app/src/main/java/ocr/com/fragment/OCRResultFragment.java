package ocr.com.fragment;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import java.io.ByteArrayOutputStream;
import java.io.File;

import idv.neo.utils.BitmapUtils;
import idv.neo.utils.GetBitmapTask;
import idv.neo.utils.GetOCRResultTask;
import ocr.com.C;
import ocr.com.R;

//http://mjbb.iteye.com/blog/1018006
public class OCRResultFragment extends Fragment {
    private final static String TAG = "OCRResultFragment";
    //OCR處理部分
    private static String LANGUAGE = "eng";

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final Context context = container.getContext();
        final Bundle arguments = getArguments();
        final View root = inflater.inflate(R.layout.fragment_ocr, container, false);
        final EditText etResult = (EditText) root.findViewById(R.id.tessmotoret001);
        final ImageView ivSelected = (ImageView) root.findViewById(R.id.iv_selected);
        final ImageView ivTreated = (ImageView) root.findViewById(R.id.iv_treated);
        final Button tessmotor001 = (Button) root.findViewById(R.id.tessmotorbt001);
        final Button tessmotor002 = (Button) root.findViewById(R.id.tessmotorbt002);
        tessmotor001.setEnabled(false);

        tessmotor001.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //    /**
//     * 新增或修改紀錄
//     */
            }
        });
        tessmotor002.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//    /**
//     * 重新拍照
//     */
                getFragmentManager().popBackStackImmediate();
            }
        });
        tessmotor001.setEnabled(false);

        tessmotor001.setEnabled(false);

        new GetBitmapTask(new GetBitmapTask.OnTaskCompleted() {
            @Override
            public void onTaskCompleted(final Bitmap bitmap) {
                final ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                Glide.with(context).load(new File(arguments.getString(C.PHOTO_PATH))).into(ivTreated);
                Bitmap treated = BitmapUtils.doPretreatment(bitmap);
//                treated = BitmapUtils.converyToGrayImg(bitmap);
                etResult.setText("識別中......");
                tessmotor001.setText("識別中......");
                treated.compress(Bitmap.CompressFormat.PNG, 100, stream);
                Glide.with(context).load(stream.toByteArray()).asBitmap().into(ivTreated);


                new GetOCRResultTask(new GetOCRResultTask.OnTaskCompleted() {
                    @Override
                    public void onTaskCompleted(String result) {
                        Log.d(TAG, "Show  onTaskCompleted : " + result);
                        if (result.equals("")) {
                            etResult.setText("識別失敗");
                            tessmotor001.setText("識別失敗");
                        } else {
                            etResult.setText(result);
                            tessmotor001.setText(result);
                        }
                    }
                }).execute(treated, LANGUAGE);
            }
        }).execute(context, new File(arguments.getString(C.OCR_PATH)));
        return root;
    }
}

