package org.timetable.schemester;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.PowerManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Objects;

public class PermanentActionActivity extends AppCompatActivity {
    ApplicationSchemester schemester;
    downloadUpdateApk downloadUpdateApkTask;
    PowerManager.WakeLock mWakeLock;
    Boolean isCompleted = false;
    private ProgressBar progressBar;
    TextView percentage, downsize;
    String vname, link;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Bundle bundle = getIntent().getExtras();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_permanent_action);
        if(bundle!=null){
            link = Objects.requireNonNull(bundle).getString("link");
            vname =Objects.requireNonNull(bundle).getString("vname");
        } else {
            finish();
        }
        schemester = (ApplicationSchemester) this.getApplication();
        setAppTheme();
        progressBar = findViewById(R.id.pdownloadProgresss);
        progressBar.setMax(100);
        percentage = findViewById(R.id.ppercentCompleted);
        downsize = findViewById(R.id.pappSize);
        downloadUpdateApkTask = new downloadUpdateApk(this);
        downloadUpdateApkTask.execute(link,vname);
        Button cancel  = findViewById(R.id.pcancelDownload);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                downloadUpdateApkTask.cancel(true);
                finish();
            }
        });
    }
    public class downloadUpdateApk  extends AsyncTask<String, Integer, Boolean> {
        private Context context;
        @RequiresApi(api = Build.VERSION_CODES.N)
        private PowerManager.WakeLock mWakeLock;
        String pathToFile;
        private downloadUpdateApk(Context context) {
            this.context = context;
        }
        @TargetApi(Build.VERSION_CODES.Q)
        @Override
        protected void onPreExecute() {
            PowerManager pm = (PowerManager) getApplicationContext().getSystemService(Context.POWER_SERVICE);
            mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                    getClass().getName());
            mWakeLock.acquire(5000);
            progressBar.setProgress(1);
            pathToFile = Environment.getExternalStorageDirectory()+"/Schemester";
            File dir = new File(pathToFile);
            try{
                if(dir.mkdir()) {
                    System.out.println("Directory created");
                } else {
                    System.out.println("Directory is not created");
                }
            }catch(Exception e){
                e.printStackTrace();
            }
            super.onPreExecute();
        }
        @Override
        protected Boolean doInBackground(String... furl) {
            int count;
            //Toast.makeText(getContext(), "In background", Toast.LENGTH_SHORT).show();
            InputStream input = null;
            OutputStream output = null;
            HttpURLConnection connection = null;
            try {
                URL url = new URL(furl[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();
                if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    Log.println( Log.WARN, "tag:","Server returned HTTP " + connection.getResponseCode()
                            + " " + connection.getResponseMessage());
                }
                int fileLength = connection.getContentLength();

                // download the file
                input = connection.getInputStream();
                output = new FileOutputStream(pathToFile+"/org.timetable.schemester-"+furl[1]+".apk");
                byte data[] = new byte[4096];
                long total = 0;
                while ((count = input.read(data)) != -1) {
                    if (isCancelled()) {
                        File file = new File(pathToFile+"/org.timetable.schemester-"+furl[1]+".apk");
                        file.delete();
                        input.close();
                        isCompleted = false;
                        return false;
                    }
                    total += count;
                    // publishing the progress....
                    if (fileLength > 0) // only if total length is known
                        publishProgress((int) (total * 100 / fileLength), fileLength);
                    output.write(data, 0, count);
                }
                isCompleted = true;
            } catch (Exception e) {
                e.printStackTrace();
                isCompleted = false;
            } finally {
                try {
                    if (output != null)
                        output.close();
                    if (input != null)
                        input.close();
                } catch (IOException ignored) {
                    ignored.printStackTrace();
                }
                if (connection != null)
                    connection.disconnect();
            }
            return isCompleted;
        }
        @Override
        protected void onProgressUpdate(Integer... progress) {
            float sizeMB =  progress[1]/1000000;
            String pc = progress[0].toString()+"%", size =" of "+ sizeMB + " MB";
            downsize.setText(size);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                //Toast.makeText(getContext(), "progress update", Toast.LENGTH_SHORT).show();
                progressBar.setProgress(progress[0], true);
            } else {
                //Toast.makeText(getContext(), "progress update", Toast.LENGTH_SHORT).show();
                progressBar.setProgress(progress[0]);
            }
            percentage.setText(pc);
        }

        @Override
        protected void onCancelled(Boolean aBoolean) {
            File file = new File(pathToFile+"/org.timetable.schemester-"+vname+".apk");
            file.delete();
            super.onCancelled(aBoolean);
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
        }
    }

    public void setAppTheme() {
        SharedPreferences mSharedPreferences = this.getSharedPreferences(schemester.getPREF_HEAD_THEME(), MODE_PRIVATE);
        switch (mSharedPreferences.getInt(schemester.getPREF_KEY_THEME(), 0)) {
            case ApplicationSchemester.CODE_THEME_INCOGNITO: setTheme(R.style.IncognitoTheme); break;
            case ApplicationSchemester.CODE_THEME_DARK: setTheme(R.style.DarkTheme);break;
            case ApplicationSchemester.CODE_THEME_LIGHT:
            default:setTheme(R.style.AppTheme);
        }
    }
}
