package com.kymco.cashreceiver;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.widget.Button;
import android.widget.EditText;

public class SettingActivity extends ActionBarActivity {

    public static final String PREFS_NAME = "MyPref";

    private EditText pathid;
    private EditText emailre;
    private EditText gmailacc;
    private EditText gmailpass;
    private EditText printername;
    private Button okbtn;
    private Button changebtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();

        pathid = (EditText) findViewById(R.id.pathid);
        emailre = (EditText) findViewById(R.id.emailre);
        gmailacc = (EditText) findViewById(R.id.gmailacc);
        gmailpass = (EditText) findViewById(R.id.gmailpass);
        printername = (EditText) findViewById(R.id.printername);
        okbtn = (Button) findViewById(R.id.setokbtn);
        changebtn = (Button) findViewById(R.id.changebtn);
    }
}
