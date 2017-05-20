package ocr.com;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class Main extends Activity {

	private Intent intent = new Intent();
	private Button button1;
	
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        intent=this.getIntent();
        String MotorID = intent.getStringExtra("MotorID");	
        button1=(Button)findViewById(R.id.bt001);      
     
        
        Toast.makeText(getApplicationContext(), MotorID, 1000).show();
        button1.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent it=new Intent();
				it.setClass(Main.this, MotorScan.class);
				startActivity(it);
			}
		});
        
        /**加載字庫,首先判斷字庫檔是否已創建     */
		File engFile = new File("/mnt/sdcard/OCR-Photo/tessdata/eng.traineddata");
		if(!engFile.exists()) {  
			//字庫檔未創建，判斷tessdata是否創建
			 String dirPath="/mnt/sdcard/OCR-Photo/tessdata";
		     File dir = new File(dirPath);
		     if(!dir.exists()) {
		    	 dir.mkdir();
		     }
		     //字庫檔案eng.traineddata，先創造eng.traineddata file並將內容導入
		     File file = new File(dir, "eng.traineddata");
		     try {
			      if(!file.exists()) {
			       file.createNewFile();
			      }		 
				  InputStream is = this.getApplicationContext().getResources().openRawResource(R.raw.eng);
			      FileOutputStream fos = new FileOutputStream(file);
			      byte[] buffere=new byte[is.available()];
			      is.read(buffere);
			      fos.write(buffere);
			      is.close();
			      fos.close();
			 }catch(FileNotFoundException  e){
			      e.printStackTrace();
			 }catch(IOException e) {
			      e.printStackTrace(); }    
		}
    }        
    
}
