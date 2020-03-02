package org.timetable.schemester;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import java.io.IOException;

public class CheckNet extends AsyncTask<Void,Void,Boolean> {
    private Context ctx;
    CheckNet(Context context){
        this.ctx = context;
    }
    @Override
    protected Boolean doInBackground(Void... voids) {
        return isInternetAvailable();
    }
    @Override
    protected void onPostExecute(Boolean aBoolean) {
        if(!aBoolean) Toast.makeText(ctx,"Connection error", Toast.LENGTH_SHORT).show();
        super.onPostExecute(aBoolean);
    }

    private boolean isInternetAvailable() {
        Runtime runtime = Runtime.getRuntime();
        try {
            Process ipProcess = runtime.exec("/system/bin/ping -c 1 8.8.8.8");
            int exitValue = ipProcess.waitFor();
            return (exitValue == 0);
        }
        catch (IOException | InterruptedException e) { e.printStackTrace(); }
        return false;
    }
}