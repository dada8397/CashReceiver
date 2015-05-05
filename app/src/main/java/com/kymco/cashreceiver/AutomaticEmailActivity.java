package com.kymco.cashreceiver;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

public class AutomaticEmailActivity extends Activity {
    private Mail m;
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(MainActivity);
        m = new Mail("vincent8397@gmail.com", "ak47vincent1415926");
    }

    public void sendEmail(View view){
        String[] toArr = {"vincent8397@gmail.com"}; // This is an array, you can add more emails, just separate them with a coma
        m.setTo(toArr); // load array to setTo function
        m.setFrom("vincent8397@gmail.com"); // who is sending the email
        m.setSubject("subject");
        m.setBody("your message goes here");

        try {
            m.addAttachment("/sdcard/myPicture.jpg");  // path to file you want to attach
            if(m.send()) {
                // success
                Toast.makeText(AutomaticEmailActivity.this, "Email was sent successfully.", Toast.LENGTH_LONG).show();
            } else {
                // failure
                Toast.makeText(AutomaticEmailActivity.this, "Email was not sent.", Toast.LENGTH_LONG).show();
            }
        } catch(Exception e) {
            // some other problem
            Toast.makeText(AutomaticEmailActivity.this, "There was a problem sending the email.", Toast.LENGTH_LONG).show();
        }

    }
}