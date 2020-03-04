package org.timetable.schemester.dialog;

import android.annotation.TargetApi;
import android.content.Context;
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

import org.timetable.schemester.listener.OnDialogDownloadLoadListener;
import org.timetable.schemester.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Objects;

public class CustomDownloadLoadDialog extends AppCompatDialog {
    OnDialogDownloadLoadListener onDialogDownloadLoadListener;
    public CustomDownloadLoadDialog(Context context, OnDialogDownloadLoadListener onDialogDownloadLoadListener){
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
        assert progressBar != null;
        progressBar.setMax(100);
        percentage = findViewById(R.id.percentCompleted);
        downsize = findViewById(R.id.appSize);
        mdownloadUpdateApk = new downloadUpdateApk(getContext());
        Button cancel = findViewById(R.id.cancelDownload);
        assert cancel != null;
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
            if (pm != null) {
                mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                        getClass().getName());
            }
            mWakeLock.acquire(5000);
            progressBar.setProgress(1);
            pathToFile = Environment.getExternalStorageDirectory()+"/Schemester";
            try{
                boolean mkdir = new File(pathToFile).mkdir();
            }catch(Exception e){
                e.printStackTrace();
            }
            super.onPreExecute();
        }

        @Override
        protected Boolean doInBackground(String... furl) {
            int count;
            InputStream input = null;
            OutputStream output = null;
            HttpURLConnection connection = null;
            try {
                connection = (HttpURLConnection) new URL(furl[0]).openConnection();
                connection.connect();
                if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    Toast.makeText(getContext(),"Some internal error (it's not you, it's us)",Toast.LENGTH_SHORT).show();
                }
                int fileLength = connection.getContentLength();
                input = connection.getInputStream();
                output = new FileOutputStream(pathToFile+"/org.timetable.schemester-"+onDialogDownloadLoadListener.getVersion()+".apk");
                byte[] data = new byte[4096];
                long total = 0;
                while ((count = input.read(data)) != -1) {
                    if (isCancelled()) {
                         isCompleted = (!new File(pathToFile+"/org.timetable.schemester-"+onDialogDownloadLoadListener.getVersion()+".apk")
                                 .delete());
                        input.close();
                        return false;
                    }
                    total += count;
                    if (fileLength > 0)
                        publishProgress((int) (total * 100 / fileLength), fileLength);
                    output.write(data, 0, count);
                }
                isCompleted = true;
            } catch (Exception e) {
                e.printStackTrace();
                isCompleted = false;
            } finally {
                try {
                    if (output != null && input != null) {
                        output.close();
                        input.close();
                    }
                } catch (IOException ignored) {}
                if (connection != null)
                    connection.disconnect();
            }
            return isCompleted;
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            String pc = progress[0].toString()+"% ",
                    size ="of "+ progress[1]/1000000 + " MB";
            downsize.setText(size);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                progressBar.setProgress(progress[0], true);
            } else { progressBar.setProgress(progress[0]); }
            percentage.setText(pc);
        }

        @Override
        protected void onCancelled(Boolean aBoolean) {
            super.onCancelled(aBoolean);
            aBoolean = new File(pathToFile + "/org.timetable.schemester-" + onDialogDownloadLoadListener.getVersion() + ".apk")
                    .delete();
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
            boolean res = new File(Environment.getExternalStorageDirectory()+"/Schemester/org.timetable.schemester-"+onDialogDownloadLoadListener.getVersion()+".apk")
                    .delete();
            onDialogDownloadLoadListener.afterFinish(!res);
        }
        return super.onKeyDown(keyCode, event);
    }
}
