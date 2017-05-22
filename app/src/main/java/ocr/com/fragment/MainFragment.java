package ocr.com.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import idv.neo.utils.FolderFileUtils;
import ocr.com.BuildConfig;
import ocr.com.R;

/**
 * Created by Neo on 2017/5/21.
 */

public class MainFragment extends Fragment {
    private static final String TAG = MainFragment.class.getSimpleName();

    @Override
    public void onAttach(final Context context) {
        super.onAttach(context);
        onHiddenChanged(false);
        /**加載字庫,首先判斷字庫檔是否已創建     */
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (!FolderFileUtils.checkExternalFolderFileExist(File.separator + BuildConfig.OCRPHOTOFOLDER + File.separator + BuildConfig.OCRTESTDATAFOLDER)) {
                    FolderFileUtils.createFolderFile(File.separator + BuildConfig.OCRPHOTOFOLDER + File.separator + BuildConfig.OCRTESTDATAFOLDER);
                }
                final ArrayList<File> folders = FolderFileUtils.getExternalStorageDirectorys(new ArrayList<File>());
                File target = null;
                for (File folder : folders) {
                    if (FolderFileUtils.isTesseractOCRFolder(folder)) {
                        target = folder;
                        break;
                    }
                }
                //http://stackoverflow.com/questions/15912825/how-to-read-file-from-res-raw-by-name
//                Log.d(TAG,"Show name  :" +getResources().getIdentifier("FILENAME_WITHOUT_EXTENSION","raw", getPackageName());
                //字庫檔未創建，判斷tessdata是否創建
                //字庫檔案eng.traineddata，先創造eng.traineddata file並將內容導入
                final File outFile = new File(target.toString(), "eng.traineddata");
                if (!outFile.exists()) {
                    InputStream in = null;
                    OutputStream out = null;
                    try {
                        in = context.getResources().openRawResource(R.raw.eng);
                        out = new FileOutputStream(outFile);
                        FolderFileUtils.writeFile(in, out);
                    } catch (IOException e) {
                        Log.e(TAG, "Failed to copy asset file: " + context.getResources().openRawResource(R.raw.eng).toString(), e);
                    } finally {
                        if (in != null) {
                            try {
                                in.close();
                            } catch (IOException e) {
                                // NOOP
                            }
                        }
                        if (out != null) {
                            try {
                                out.flush();
                                out.close();
                            } catch (IOException e) {
                                // NOOP
                            }
                        }
                    }
                }
            }
        }).start();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View root = inflater.inflate(R.layout.fragment_main, container, false);
        final Button takephotopreview = (Button) root.findViewById(R.id.takephotopreview);
        takephotopreview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFragmentManager().beginTransaction()
                        .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out, android.R.anim.fade_in, android.R.anim.fade_out)
                        .addToBackStack(MainFragment.class.getSimpleName())
                        .replace(R.id.main_content, new MotorScanFragment())
                        .commit();
            }
        });
        return root;
    }
}
