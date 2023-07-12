package com.nyax.tech.copy_folders_app;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {
    Button btncopy_files;
    TextView lblmsglog;
    TextView lbltimedisplay;
    ProgressBar progress_bar;
    static final Timer timer = new Timer("current time");
    long _start_time = System.nanoTime();
    ArrayList<notificationdto> _lstnotificationdto = new ArrayList<notificationdto>();
    final String TAG = MainActivity.class.getSimpleName();
    final String MANAGE_EXTERNAL_STORAGE_PERMISSION = "Manage External Storage Permission";
    final String SHARED_PREFS_FILE = "SHARED_PREFS_FILE_COPY_FILES";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btncopy_files = (Button) findViewById(R.id.btncopy_files);
        lbltimedisplay = (TextView) findViewById(R.id.lbltimedisplay);
        lblmsglog = (TextView) findViewById(R.id.lblmsglog);
        progress_bar = (ProgressBar) findViewById(R.id.progress_bar);

        btncopy_files.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                progress_bar.setVisibility(View.VISIBLE);
                btncopy_files.setText("working...");

                try {

                    String permission_status = get_permission_from_shared_prefs();

                    if (permission_status.equals("DENIED")) {
                        Intent intent = new Intent();
                        intent.setAction(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                        Uri uri = Uri.fromParts("package", getApplicationContext().getPackageName(), null);
                        intent.setData(uri);
                        storageActivityResultLauncher.launch(intent);
                    }

                    copy_files(getApplicationContext());

                    progress_bar.setVisibility(View.GONE);
                    btncopy_files.setText(R.string.btncopy_files);

                } catch (Exception ex) {
                    btncopy_files.setText(R.string.btncopy_files);
                    Log.e(TAG, ex.toString());
                    log_error_messages(ex.toString());
                }

            }
        });

        display_running_time();

        log_info_messages("finished main_activity initialization.");

    }

    public void copy_files(final Context context) {
//        Handler handler = new Handler();
//        Runnable runnable = new Runnable() {
//            @Override
//            public void run() {
        try {
            int counta = 0;
            File whatsapp_dir = new File("/storage/self/primary/Android/media/com.whatsapp/WhatsApp/Media");

            if (!whatsapp_dir.exists())
                return;

            File media_back_up_dir = new File("/storage/0E6F-2D71/Android/backup/Media/WhatsApp");

            if (!media_back_up_dir.exists())
                media_back_up_dir.mkdirs();

            File[] whats_app_dirs = whatsapp_dir.listFiles();

            for (File media_dir : whats_app_dirs) {

                try {

                    if (media_dir.isDirectory()) {
                        String dir_path = media_dir.getPath();
                        Log.e(TAG, dir_path);

                        File whats_up_dir = new File(dir_path);

                        File[] whats_app_files_in_dir = whats_up_dir.listFiles();

                        counta += whats_app_files_in_dir.length;

                        for (File current_file_being_moved : whats_app_files_in_dir) {

                            try {

                                if (current_file_being_moved.isDirectory()) {


                                } else {

                                    int folder_counta = whats_app_files_in_dir.length;

                                    if(folder_counta <= 3)
                                    {
                                        break;
                                    }

                                    String media_file = current_file_being_moved.getPath();
                                    Log.e(TAG, media_file);

                                    File file_media = new File(media_file);

                                    String current_folder = file_media.getParent();

                                    String[] current_dir_arr = current_folder.split("/");
                                    String current_dir_name = current_dir_arr[current_dir_arr.length - 1];// Get last value in array

                                    Log.e(TAG, "current_dir_name: " + current_dir_name);
                                    Toast.makeText(context, "current_dir_name: " + current_dir_name, Toast.LENGTH_LONG).show();
                                    log_info_messages("current_dir_name: " + current_dir_name);

                                    File destination_dir = new File(media_back_up_dir.getPath(), current_dir_name);

                                    if (!destination_dir.exists())
                                        destination_dir.mkdirs();

                                    String current_file = file_media.getPath();

                                    String[] current_file_arr = current_file.split("/");
                                    String current_file_name = current_file_arr[current_file_arr.length - 1];// Get last value in array

                                    Log.e(TAG, "current_file_name: " + current_file_name);
                                    Toast.makeText(context, "current_file_name: " + current_file_name, Toast.LENGTH_LONG).show();
                                    log_info_messages("current_file_name: " + current_file_name);

                                    if (current_file_name.equals(".nomedia")) {
                                        continue;
                                    }

                                    File destination_file = new File(destination_dir.getPath(), current_file_name);

                                    if (!destination_file.exists())
                                        destination_file.createNewFile();

                                    Log.e(TAG, "destination file: " + destination_file.getPath());
                                    Toast.makeText(context, "destination file: " + destination_file.getPath(), Toast.LENGTH_LONG).show();
                                    log_info_messages("destination file: " + destination_file.getPath());

                                    Log.e(TAG, "current file: " + current_file_being_moved.getPath());
                                    Toast.makeText(context, "current file: " + current_file_being_moved.getPath(), Toast.LENGTH_LONG).show();
                                    log_info_messages("current file: " + current_file_being_moved.getPath());

                                    InputStream is = new FileInputStream(current_file_being_moved);
                                    OutputStream os = new FileOutputStream(destination_file);

                                    byte[] buffer = new byte[1024];
                                    int length;
                                    while ((length = is.read(buffer)) > 0) {
                                        os.write(buffer, 0, length);
                                    }

                                    boolean isdeleted = current_file_being_moved.delete();

                                    if (isdeleted) {
                                        Log.e(TAG, "deleted file: " + current_file_being_moved.getPath());
                                        Toast.makeText(context, "deleted file: " + current_file_being_moved.getPath(), Toast.LENGTH_LONG).show();
                                        log_info_messages("deleted file: " + current_file_being_moved.getPath());
                                    }
                                    File[] files_remaining_in_current_whats_app_dir = whats_up_dir.listFiles();
                                    Log.e(TAG, "files remaining in directory [ " + current_dir_name + " ] " + files_remaining_in_current_whats_app_dir.length);
                                    Toast.makeText(context, "files remaining in directory [ " + current_dir_name + " ] " + files_remaining_in_current_whats_app_dir.length, Toast.LENGTH_LONG).show();
                                    log_info_messages("files remaining in directory [ " + current_dir_name + " ] " + files_remaining_in_current_whats_app_dir.length);

                                    File[] files_in_destination_dir = destination_dir.listFiles();
                                    Log.e(TAG, "files in destination: " + files_in_destination_dir.length);
                                    Toast.makeText(context, "files in destination: " + files_in_destination_dir.length, Toast.LENGTH_LONG).show();
                                    log_info_messages("files in destination: " + files_in_destination_dir.length);

                                    if (is != null) {
                                        try {
                                            is.close();
                                        } catch (Exception ex) {
                                            Log.e(TAG, ex.toString());
                                            log_error_messages(ex.toString());
                                            is = null;
                                        }
                                    }
                                    if (os != null) {
                                        try {
                                            os.close();
                                        } catch (Exception ex) {
                                            Log.e(TAG, ex.toString());
                                            log_error_messages(ex.toString());
                                            os = null;
                                        }
                                    }

                                    save_to_database();

                                }

                            } catch (Exception ex) {
                                Log.e(TAG, ex.toString());
                                log_error_messages(ex.toString());
                            }
                        }
                    }

                } catch (Exception ex) {
                    Log.e(TAG, ex.toString());
                    log_error_messages(ex.toString());
                }
            }

            if (counta > 33) {
                Log.e(TAG, "counta: " + counta);
                Toast.makeText(context, "counta: " + counta, Toast.LENGTH_LONG).show();
                log_info_messages("counta: " + counta);
                copy_files(context);
            } else {
                Log.e(TAG, "counta: " + counta);
                Toast.makeText(context, "counta: " + counta, Toast.LENGTH_LONG).show();
                log_info_messages("counta: " + counta);
                //return;
            }

        } catch (Exception ex) {
            Log.e(TAG, ex.toString());
            log_error_messages(ex.toString());
        }
//            }
//        };
//        handler.post(runnable);

    }

    private ActivityResultLauncher<Intent> storageActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    Log.d(TAG, "onActivityResult: ");
                    String msg = "";
                    //here we will handle the result of our intent
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        //Android is 11(R) or above
                        if (Environment.isExternalStorageManager()) {
                            //Manage External Storage Permission is granted
                            save_permission_in_shared_prefs("GRANTED");
                            msg = "Manage External Storage Permission is granted";
                            Log.e(TAG, "onActivityResult: Manage External Storage Permission is granted");
                            Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
                            log_info_messages(msg);
                        } else {
                            //Manage External Storage Permission is denied
                            save_permission_in_shared_prefs("DENIED");
                            msg = "Manage External Storage Permission is denied";
                            Log.e(TAG, "onActivityResult: Manage External Storage Permission is denied");
                            Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
                            log_info_messages(msg);
                        }
                    } else {
                        //Android is below 11(R)
                    }
                }
            }
    );

    private void log_info_messages(String msg) {
        try {
            msg = Utils.format_info_spannable_string(msg).toString();
            notificationdto _notificationdto = new notificationdto();

            Log.e(TAG, msg);

            String dateTimenow = Utils.get_current_datetime();

            String _logtext = Utils.get_new_line() + "[ " + dateTimenow + " ]   " + msg;

            _notificationdto._notification_message = _logtext;
            _notificationdto._created_datetime = dateTimenow;
            _notificationdto.TAG = TAG;

            _lstnotificationdto.add(_notificationdto);

            Collections.sort(_lstnotificationdto, notificationdto.dateComparator);

            ArrayList<String> _lstmsgdto = new ArrayList<String>();
            for (notificationdto _msg : _lstnotificationdto) {
                _lstmsgdto.add(_msg._notification_message);
            }

            lblmsglog.setText(_lstmsgdto.toString());

            Utils.log_messages_to_file(msg);

        } catch (IOException ex) {
            Log.e(TAG, ex.toString());
            log_error_messages(ex.toString());
        }
    }

    private void log_error_messages(String msg) {
        try {
            msg = Utils.format_error_spannable_string(msg).toString();
            notificationdto _notificationdto = new notificationdto();

            Log.e(TAG, msg);

            String dateTimenow = Utils.get_current_datetime();

            String _logtext = Utils.get_new_line() + "[ " + dateTimenow + " ]   " + msg;

            _notificationdto._notification_message = _logtext;
            _notificationdto._created_datetime = dateTimenow;
            _notificationdto.TAG = TAG;

            _lstnotificationdto.add(_notificationdto);

            Collections.sort(_lstnotificationdto, notificationdto.dateComparator);

            ArrayList<String> _lstmsgdto = new ArrayList<String>();
            for (notificationdto _msg : _lstnotificationdto) {
                _lstmsgdto.add(_msg._notification_message);
            }

            lblmsglog.setText(_lstmsgdto.toString());

            Utils.log_messages_to_file(msg);

        } catch (IOException ex) {
            Log.e(TAG, ex.toString());
        }
    }


    private void display_running_time() {
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                //task to be executed every second
                try {

                    long _current_time = System.nanoTime();

                    long timeElapsed = _current_time - _start_time;

                    timeElapsed = timeElapsed / 1000000;

                    String _days = String.valueOf(Math.abs(timeElapsed) / (1000 * 60 * 60 * 24));
                    String _hours = String.valueOf(Math.abs(timeElapsed) / (1000 * 60 * 60) % 24);
                    String _minutes = String.valueOf(Math.abs(timeElapsed) / (1000 * 60) % 60);
                    String _seconds = String.valueOf(Math.abs(timeElapsed) / (1000) % 60);

                    String days = "";
                    String hours = "";
                    String minutes = "";
                    String seconds = "";

                    if (_days.length() < 2)
                        days = "0" + _days;
                    else
                        days = _days;

                    if (_hours.length() < 2)
                        hours = "0" + _hours;
                    else
                        hours = _hours;

                    if (_minutes.length() < 2)
                        minutes = "0" + _minutes;
                    else
                        minutes = _minutes;

                    if (_seconds.length() < 2)
                        seconds = "0" + _seconds;
                    else
                        seconds = _seconds;

                    String _elapsed_time = days + ':' + hours + ':' + minutes + ':' + seconds;

                    String current_date_time = Utils.get_current_datetime();
                    lbltimedisplay.setText(current_date_time);

                    String running_time = current_date_time + Utils.get_new_line() + _elapsed_time;
                    Log.e(TAG, running_time);

                    try {
                        lbltimedisplay.setText(running_time);
                    } catch (Exception ex) {
                        Log.e(TAG, ex.toString());
                    }

                } catch (Exception ex) {
                    Log.e(TAG, ex.toString());
                    log_error_messages(ex.toString());
                }
            }
        };

        //this will invoke the timer every second
        timer.scheduleAtFixedRate(task, 1000, 1000);
    }

    public void save_permission_in_shared_prefs(String permission_status) {
        try {

            final String PREFS_FILE = SHARED_PREFS_FILE;
            // PREFS_MODE defines which apps can access the file
            final int PREFS_MODE = Context.MODE_PRIVATE;

            SharedPreferences sharedPreferences = getSharedPreferences(PREFS_FILE, PREFS_MODE);
            SharedPreferences.Editor editor = sharedPreferences.edit();

            // write string
            String string_dto = sharedPreferences.getString(MANAGE_EXTERNAL_STORAGE_PERMISSION, "DENIED");

            editor.putString(MANAGE_EXTERNAL_STORAGE_PERMISSION, permission_status);

            // This will asynchronously save the shared preferences without holding the current thread.
            editor.apply();

            log_info_messages("successfully saved record in shared preferences.");

        } catch (Exception ex) {
            Log.e(TAG, ex.toString());
            log_error_messages(ex.toString());
        }
    }

    public String get_permission_from_shared_prefs() {
        try {

            final String PREFS_FILE = SHARED_PREFS_FILE;
            // PREFS_MODE defines which apps can access the file
            final int PREFS_MODE = Context.MODE_PRIVATE;

            SharedPreferences sharedPreferences = getSharedPreferences(PREFS_FILE, PREFS_MODE);
            SharedPreferences.Editor editor = sharedPreferences.edit();

            // write string
            String permission_status = sharedPreferences.getString(MANAGE_EXTERNAL_STORAGE_PERMISSION, "DENIED");

            return permission_status;

        } catch (Exception ex) {
            Log.e(TAG, ex.toString());
            log_error_messages(ex.toString());
            return null;
        }
    }


    private void save_to_database() {
        try {


        } catch (Exception ex) {
            Log.e(TAG, ex.toString());
            log_error_messages(ex.toString());
        }
    }

}