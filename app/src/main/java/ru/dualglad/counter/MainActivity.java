package ru.dualglad.counter;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
    private static final String ACTION_COUNTER = "ACTION_COUNTER"; // unique string as message ID

    private BroadcastReceiver broadcastReceiver; // receives UI updates
    private Runnable runnable; // counter
    private Thread thread; // counter thread
    private boolean enable; // enables counter
    private int count; // current value

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // layout to draw

        /* UI init */
        findViewById(R.id.button_open).setOnClickListener(new View.OnClickListener() { // set click action
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), DialogActivity.class)); // call Dialog activity
            }
        });
        final TextView textView = findViewById(R.id.textview); // text box

        /* IPC and other communications */
        broadcastReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if ((action != null) && (action.equals(ACTION_COUNTER))) { // check what we've received
                    ++count;
                    textView.setText(String.valueOf(count)); // update text field
                }
            }
        };
        registerReceiver(broadcastReceiver, new IntentFilter(ACTION_COUNTER)); // register a receiver of exact message

        /* Threads */
        runnable = new Runnable() {
            public void run() {
                while (enable) { // counter loop
                    Intent intent = new Intent(ACTION_COUNTER);
                    sendBroadcast(intent); // send update message in the whole system

                    if (!enable) { // don't sleep if we are halting
                        break;
                    }
                    try {
                        Thread.sleep(1000); // 1 sec delay
                    } catch (InterruptedException e) {
                        break;
                    }
                }
            }
        };

        /* It'll show user-friendly log-popup on the device */
        Toast.makeText(this, "onCreate()", Toast.LENGTH_SHORT).show();
    }

    protected void onStart() {
        super.onStart();

        enable = true; // start counter only if we can see app window
        thread = new Thread(runnable);
        thread.start(); // spawn thread

        Toast.makeText(this, "onStart()", Toast.LENGTH_SHORT).show();
    }

    protected void onResume() {
        super.onResume();
        Toast.makeText(this, "onResume()", Toast.LENGTH_SHORT).show();
    }

    protected void onPause() {
        super.onPause();
        Toast.makeText(this, "onPause()", Toast.LENGTH_SHORT).show();
    }

    protected void onStop() {
        super.onStop();

        enable = false; // stop counter
        thread.interrupt(); // halt the thread
        try {
            thread.join();
        } catch (InterruptedException ignored) { }

        Toast.makeText(this, "onStop()", Toast.LENGTH_SHORT).show();
    }

    protected void onDestroy() {
        super.onDestroy();

        unregisterReceiver(broadcastReceiver); // remove receiver from the system

        Toast.makeText(this, "onDestroy()", Toast.LENGTH_SHORT).show();
    }
}
