package ocr.com.activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import ocr.com.R;
import ocr.com.fragment.MainFragment;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setFragment(new MainFragment());
    }

    private void setFragment(Fragment fragment) {
        try {
            getSupportFragmentManager().beginTransaction().setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out, android.R.anim.fade_in, android.R.anim.fade_out)
                    .replace(R.id.main_content, fragment)
                    .commit();
        } catch (IllegalStateException ex) {
            Log.d(TAG, "setFragment(): illegal state: " + ex.getMessage());
        }
    }
}
