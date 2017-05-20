package ocr.com.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import ocr.com.R;

/**
 * Created by Neo on 2017/5/21.
 */

public class MainFragment extends Fragment {
    private static final String TAG = MainFragment.class.getSimpleName();
    private Intent intent = new Intent();
    private Button button1;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final Context context = container.getContext();
        final View root = inflater.inflate(R.layout.fragment_main, container, false);
        intent = getActivity().getIntent();
        String MotorID = intent.getStringExtra("MotorID");
        button1 = (Button) root.findViewById(R.id.bt001);

        Toast.makeText(context, MotorID, Toast.LENGTH_LONG).show();
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Intent it = new Intent();
//                it.setClass(context, MotorScanActivity.class);
//                startActivity(it);
            }
        });

        /**加載字庫,首先判斷字庫檔是否已創建     */
        File engFile = new File("/mnt/sdcard/OCR-Photo/tessdata/eng.traineddata");
        if (!engFile.exists()) {
            //字庫檔未創建，判斷tessdata是否創建
            String dirPath = "/mnt/sdcard/OCR-Photo/tessdata";
            File dir = new File(dirPath);
            if (!dir.exists()) {
                dir.mkdir();
            }
            //字庫檔案eng.traineddata，先創造eng.traineddata file並將內容導入
            File file = new File(dir, "eng.traineddata");
            try {
                if (!file.exists()) {
                    file.createNewFile();
                }
                InputStream is = context.getResources().openRawResource(R.raw.eng);
                FileOutputStream fos = new FileOutputStream(file);
                byte[] buffere = new byte[is.available()];
                is.read(buffere);
                fos.write(buffere);
                is.close();
                fos.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return root;
    }
}
