package org.timetable.schemester;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.PowerManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatDialog;

import com.google.android.material.tabs.TabLayout;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.security.spec.ECFieldF2m;
import java.util.Objects;

public class CustomDownloadLoadDialog extends AppCompatDialog {
    OnDialogDownloadLoadListener onDialogDownloadLoadListener;
    CustomDownloadLoadDialog(Context context, OnDialogDownloadLoadListener onDialogDownloadLoadListener){
        super(context);
        this.onDialogDownloadLoadListener = onDialogDownloadLoadListener;
    }
    private downloadUpdateApk mdownloadUpdateApk;
    PowerManager.WakeLock mWakeLock;
    Boolean isCompleted = false;
    private ProgressBar progressBar;
    TextView percentage, downsize;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.custom_live_load_dialog);
        this.setCanceledOnTouchOutside(false);
        Objects.requireNonNull(this.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        progressBar = findViewById(R.id.downloadProgress);
        progressBar.setMax(100);
        percentage = findViewById(R.id.percentCompleted);
        downsize = findViewById(R.id.appSize);
        mdownloadUpdateApk = new downloadUpdateApk(getContext());
        Button cancel = findViewById(R.id.cancelDownload);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mdownloadUpdateApk.cancel(true);
                onDialogDownloadLoadListener.afterFinish(false);
                dismiss();
            }
        });

        mdownloadUpdateApk.execute(onDialogDownloadLoadListener.getLink());
    }

    public class downloadUpdateApk  extends AsyncTask<String, Integer, Boolean> {
        @RequiresApi(api = Build.VERSION_CODES.N)
        private Context context;
        private PowerManager.WakeLock mWakeLock;
        String pathToFile;
        @TargetApi(Build.VERSION_CODES.Q)
        private downloadUpdateApk(Context context) {
            this.context = context;
        }
        @Override
        protected void onPreExecute() {
            PowerManager pm = (PowerManager) getContext().getSystemService(Context.POWER_SERVICE);
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
                    output = new FileOutputStream(pathToFile+"/org.timetable.schemester-"+onDialogDownloadLoadListener.getVersion()+".apk");
                    byte data[] = new byte[4096];
                    long total = 0;
                    while ((count = input.read(data)) != -1) {
                        if (isCancelled()) {
                            File file = new File(pathToFile+"/org.timetable.schemester-"+onDialogDownloadLoadListener.getVersion()+".apk");
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
            File file = new File(pathToFile+"/org.timetable.schemester-"+onDialogDownloadLoadListener.getVersion()+".apk");
            file.delete();
            super.onCancelled(aBoolean);
        }

        @Override
        protected void onPostExecute(Boolean result) {
                onDialogDownloadLoadListener.afterFinish(result);
                dismiss();
                super.onPostExecute(result);
        }
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            mdownloadUpdateApk.cancel(true);
            File file = new File(Environment.getExternalStorageDirectory()+"/Schemester/org.timetable.schemester-"+onDialogDownloadLoadListener.getVersion()+".apk");
            file.delete();
            onDialogDownloadLoadListener.afterFinish(false);
        }
        return super.onKeyDown(keyCode, event);
    }
}
