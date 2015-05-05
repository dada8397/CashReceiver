package com.kymco.cashreceiver;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.support.v7.app.ActionBarActivity;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.printer.bluetooth.android.BluetoothPrinter;
import com.printer.bluetooth.android.PrinterType;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;

public class MainActivity extends ActionBarActivity {

    private final static String TAG = "CashReceiverLog";

    private EditText clientidvalue;
    private EditText cashvalue, cash2value;
    private Button buttonok;
    private Button setbutton;
    private Context mContext;

    //password dialog
    private Dialog dlgPassword;
    private Dialog firstPassword;
    private Dialog setting;
    public static final String PREFS_NAME = "MyPref";
    public static String password;

    public String pathid;
    public String gmailacc;
    public String gmailpass;
    public String mailre;
    public String firstps;

    public EditText pathidtx;
    public EditText emailretx;
    public EditText gmailacctx;
    public EditText gmailpasstx;
    public EditText printername;

    //Bluetooth Printer
    private TextView printerStatus;
    private Button btnOpen;

    // Name of the connected device
    private String mConnectedDeviceName = null;
    private String mprintername;
    // Local Bluetooth adapter
    private BluetoothAdapter mBluetoothAdapter = null;
    public static BluetoothPrinter mPrinter;
    private final int REQUEST_ENABLE_BT = 1;
    private boolean isConnected;
    private boolean isConnecting;

    private int SEED;

    String dts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mContext = this.getApplicationContext();

        setbutton = (Button) findViewById(R.id.settings);
        clientidvalue = (EditText) findViewById(R.id.clientidvalue);
        cashvalue = (EditText) findViewById(R.id.cashvalue);
        cash2value = (EditText) findViewById(R.id.cash2value);
        buttonok = (Button) findViewById(R.id.ok);
        printerStatus = (TextView) findViewById(R.id.printerStatus);
        btnOpen = (Button) findViewById(R.id.btnOpen);

        setbutton.setOnClickListener(settingOnClick);
        buttonok.setOnClickListener(okOnClick);
        btnOpen.setOnClickListener(printerConnection);

        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                .detectDiskReads().detectDiskWrites().detectNetwork()
                .penaltyLog().build());

        //檔案存取
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        if (settings.getString("PASSWORD", "0000").equals("0000")) {
            firstPassword = new Dialog(MainActivity.this);
            firstPassword.setTitle("第一次使用，請輸入密碼");
            firstPassword.setCancelable(false);
            firstPassword.setContentView(R.layout.firstpass_layout);
            Button firstpsbtn = (Button) firstPassword.findViewById(R.id.firstpsbtn);
            firstpsbtn.setOnClickListener(firstbtnok);
            firstPassword.show();
        }

        //Bluetooth Printer
        // Get local Bluetooth adapter
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // If the adapter is null, then Bluetooth is not supported
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, R.string.bt_not_enabled, Toast.LENGTH_LONG).show();
            finish();
        }
    }

    protected void onStart() {
        super.onStart();
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        mprintername = settings.getString("PRINTER_NAME", "T9 BT Printer");
        if (!isConnected) {
            // 取得目前已經配對過的裝置
            Set<BluetoothDevice> setPairedDevices = mBluetoothAdapter.getBondedDevices();

            // 如果已經有配對過的裝置
            if (setPairedDevices.size() > 0) {
                // 把裝置名稱以及MAC Address印出來
                for (BluetoothDevice device : setPairedDevices) {
                    if (device.getName().equals(mprintername)) {
                        mConnectedDeviceName = device.getName();
                        initPrinter(device);
                    }
                }
            }
        }
    }

    private View.OnClickListener firstbtnok = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
            SharedPreferences.Editor editor = settings.edit();
            EditText pswd1 = (EditText) firstPassword.findViewById(R.id.firstps1);
            EditText pswd2 = (EditText) firstPassword.findViewById(R.id.firstps2);
            if (pswd1.getText().toString().equals(pswd2.getText().toString())) {
                firstps = pswd1.getText().toString();
                editor.putString("PASSWORD", firstps);
                editor.commit();
                AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
                dialog.setTitle("設定成功！");
                dialog.setPositiveButton(R.string.ok_label, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialoginterface, int i) {
                        firstPassword.cancel();
                    }
                });
                dialog.show();
            } else {
                AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
                dialog.setTitle("兩次密碼輸入錯誤");
                //dialog.setMessage("您輸入的密碼是"+pwdstring+"正確密碼為"+password);
                dialog.setMessage("兩次密碼不同！請重新嘗試！");
                dialog.setPositiveButton(R.string.ok_label, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialoginterface, int i) {
                    }
                });
                dialog.show();
            }
        }
    };

    public View.OnClickListener settingOnClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            dlgPassword = new Dialog(MainActivity.this);
            dlgPassword.setTitle("請輸入密碼");
            dlgPassword.setCancelable(false);
            dlgPassword.setContentView(R.layout.password_layout);
            Button btnOK = (Button) dlgPassword.findViewById(R.id.btnOK);
            Button btnCAN = (Button) dlgPassword.findViewById(R.id.btnCAN);
            btnOK.setOnClickListener(btnoklis);
            btnCAN.setOnClickListener(btncanlis);
            dlgPassword.show();
        }
    };

    private View.OnClickListener btnoklis = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
            password = settings.getString("PASSWORD", "0");

            EditText passwd = (EditText) dlgPassword.findViewById(R.id.passwd);
            String pwdstring = passwd.getText().toString();
            if (pwdstring.equals(password)) {
                AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
                dialog.setTitle("登入成功！");
                dialog.setPositiveButton(R.string.ok_label, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialoginterface, int i) {
                        dlgPassword.cancel();
                        settingview();
                    }
                });
                dialog.show();
            } else {
                AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
                dialog.setTitle("登入失敗！請重新輸入");
                //dialog.setMessage("您輸入的密碼是"+pwdstring+"正確密碼為"+password);
                dialog.setMessage("密碼錯誤！請重新嘗試");
                dialog.setPositiveButton(R.string.ok_label, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialoginterface, int i) {
                    }
                });
                dialog.show();

            }
        }
    };

    private View.OnClickListener btncanlis = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            dlgPassword.cancel();
        }
    };

    private View.OnClickListener printerConnection = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            // If the adapter is null, then Bluetooth is not supported
            if (mBluetoothAdapter == null) {
                Toast.makeText(MainActivity.this, R.string.bt_not_enabled, Toast.LENGTH_LONG).show();
            } else if (!mBluetoothAdapter.isEnabled()) {
                Intent mOpenBT = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(mOpenBT, REQUEST_ENABLE_BT);
            }

            SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
            mprintername = settings.getString("PRINTER_NAME", "T9 BT Printer");

            if (!isConnected) {
                // 取得目前已經配對過的裝置
                Set<BluetoothDevice> setPairedDevices = mBluetoothAdapter.getBondedDevices();

                // 如果已經有配對過的裝置
                if (setPairedDevices.size() > 0) {
                    // 把裝置名稱以及MAC Address印出來
                    for (BluetoothDevice device : setPairedDevices) {
                        if (device.getName().equals(mprintername)) {
                            mConnectedDeviceName = device.getName();
                            initPrinter(device);
                        }
                    }
                } else {
                    // 註冊一個BroadcastReceiver，等等會用來接收搜尋到裝置的消息
                    IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
                    registerReceiver(mReceiver, filter);
                    mBluetoothAdapter.startDiscovery(); //開始搜尋裝置
                }
            } else {
                mPrinter.closeConnection();
                mPrinter.setHandler(mHandler);
            }
        }
    };

    // use device to init printer.
    private void initPrinter(BluetoothDevice device) {
        mPrinter = new BluetoothPrinter(device);
        mPrinter.setCurrentPrintType(PrinterType.T9);
        //set handler for receive message of connect state from sdk.
        mPrinter.setHandler(mHandler);
        mPrinter.openConnection();
        mPrinter.setEncoding("BIG5");
        mPrinter.setTitle("客戶收據", "客戶收據", null);
    }

    // The Handler that gets information back from the bluetooth printer.
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Log.i(TAG, "msg.what is : " + msg.what);
            switch (msg.what) {
                case BluetoothPrinter.Handler_Connect_Connecting:
                    isConnecting = true;
                    printerStatus.setText(R.string.title_connecting);
                    btnOpen.setText("連線中");
                    //Toast.makeText(getApplicationContext(), R.string.bt_connecting,Toast.LENGTH_SHORT).show();
                    break;
                case BluetoothPrinter.Handler_Connect_Success:
                    isConnected = true;
                    isConnecting = false;
                    printerStatus.setText(getString(R.string.title_connected) + ": " + mPrinter.getPrinterName());
                    btnOpen.setText("中斷印表機連線");
                    //Toast.makeText(getApplicationContext(), R.string.bt_connect_success,Toast.LENGTH_SHORT).show();
                    break;
                case BluetoothPrinter.Handler_Connect_Failed:
                    isConnected = false;
                    isConnecting = false;
                    printerStatus.setText(R.string.title_not_connected);
                    btnOpen.setText("開啟印表機連線");
                    //Toast.makeText(getApplicationContext(), R.string.bt_connect_failed, Toast.LENGTH_SHORT).show();
                    break;
                case BluetoothPrinter.Handler_Connect_Closed:
                    isConnected = false;
                    isConnecting = false;
                    printerStatus.setText(R.string.title_not_connected);
                    btnOpen.setText("開啟印表機連線");
                    //Toast.makeText(getApplicationContext(), R.string.bt_connect_closed, Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
            mprintername = settings.getString("PRINTER_NAME", "T9 BT Printer");
            // 當收尋到裝置時
            if (BluetoothDevice.ACTION_FOUND.equals(intent.getAction())) {
                // 取得藍芽裝置這個物件
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                // 判斷那個裝置是不是你要連結的裝置，根據藍芽裝置名稱判斷
                if (device.getName().equals(mprintername)) {
                    mBluetoothAdapter.cancelDiscovery();
                    mConnectedDeviceName = device.getName();
                    initPrinter(device);
                }
            }
        }
    };


    public View.OnClickListener okOnClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
            if (settings.getString("PATH_ID", "0").equals("0")) {
                AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
                dialog.setTitle("尚未設定資訊");
                dialog.setMessage("請點選設定資訊以設定路線代號、郵件帳號及密碼");
                dialog.setPositiveButton(R.string.ok_label, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialoginterface, int i) {
                    }
                });
                dialog.show();
            } else if (clientidvalue.getText().toString().equals("") && cash2value.getText().toString().equals("") && cashvalue.getText().toString().equals("")) {
                AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
                dialog.setTitle("傳送失敗！");
                dialog.setMessage("未輸入資料！");
                dialog.setPositiveButton(R.string.ok_label, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialoginterface, int i) {
                    }
                });
                dialog.show();
            } else if (cash2value.getText().toString().equals("") && cashvalue.getText().toString().equals("")) {
                AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
                dialog.setTitle("傳送失敗！");
                dialog.setMessage("未輸入現金額與票據額");
                dialog.setPositiveButton(R.string.ok_label, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialoginterface, int i) {
                    }
                });
                dialog.show();
            } else if (clientidvalue.getText().toString().equals("") && cashvalue.getText().toString().equals("")) {
                AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
                dialog.setTitle("傳送失敗！");
                dialog.setMessage("未輸入客戶代號與現金額");
                dialog.setPositiveButton(R.string.ok_label, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialoginterface, int i) {
                    }
                });
                dialog.show();
            } else if (cash2value.getText().toString().equals("") && clientidvalue.getText().toString().equals("")) {
                AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
                dialog.setTitle("傳送失敗！");
                dialog.setMessage("未輸入客戶代號與票據額");
                dialog.setPositiveButton(R.string.ok_label, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialoginterface, int i) {
                    }
                });
                dialog.show();
            } else if (clientidvalue.getText().toString().equals("")) {
                AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
                dialog.setTitle("傳送失敗！");
                dialog.setMessage("未輸入客戶代號");
                dialog.setPositiveButton(R.string.ok_label, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialoginterface, int i) {
                    }
                });
                dialog.show();
            } else if (cashvalue.getText().toString().equals("")) {
                AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
                dialog.setTitle("傳送失敗！");
                dialog.setMessage("未輸入現金額");
                dialog.setPositiveButton(R.string.ok_label, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialoginterface, int i) {
                    }
                });
                dialog.show();
            } else if (cash2value.getText().toString().equals("")) {
                AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
                dialog.setTitle("傳送失敗！");
                dialog.setMessage("未輸入票據額");
                dialog.setPositiveButton(R.string.ok_label, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialoginterface, int i) {
                    }
                });
                dialog.show();
            } else {
                //日期時間
                //先行定義時間格式
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
                SimpleDateFormat sdf2 = new SimpleDateFormat("HH");
                SimpleDateFormat sdf3 = new SimpleDateFormat("mm");
                SimpleDateFormat sdf4 = new SimpleDateFormat("ss");
                //取得現在時間
                Date dt = new Date();
                //透過SimpleDateFormat的format方法將Date轉為字串
                dts = sdf.format(dt) + " " + sdf2.format(dt) + ":" + sdf3.format(dt) + ":" + sdf4.format(dt);
                SEED = Integer.parseInt(sdf2.format(dt)) * (Integer.parseInt(sdf3.format(dt)) + Integer.parseInt(sdf4.format(dt)));

                pathid = settings.getString("PATH_ID", "001");
                gmailacc = settings.getString("GMAIL_ACC", "0");
                gmailpass = settings.getString("GMAIL_PASS", "0");
                mailre = settings.getString("EMAIL_RE", "0");
                String mid = "路線代號：" + pathid;
                String idv = "客戶代號：" + clientidvalue.getText().toString();
                String cav = "現金額：" + cashvalue.getText().toString();
                String ca2v = "票據額：" + cash2value.getText().toString();

                String tit = mid + "," + idv + "," + cav + "," + ca2v;
                String con = dts + "\n" + mid + "\n" + idv + "\n" + cav + "\n" + ca2v;

                String pass = cashvalue.getText().toString();
                String pass2 = cash2value.getText().toString();

//                BigInteger hash1 = new BigInteger(pass);
//                BigInteger hash2 = new BigInteger(SEED);
//                BigInteger hash = hash1.xor(hash2);

                int passs = Integer.parseInt(pass) + Integer.parseInt(pass2);
                int hash = SEED * passs;
                String hashs = String.valueOf(hash);

                //mPrinter.setHandler(mHandler);
                //Email
                if (checkStatus() && isConnected) {
                    try {
                        GMailSender sender = new GMailSender(gmailacc, gmailpass); //寄件者(開發方)帳號與密碼
                        sender.sendMail(tit,   //信件標題
                                con,   //信件內容
                                gmailacc,   //寄件者
                                mailre);   //收件者

                        AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
                        dialog.setTitle("傳送成功！");
                        dialog.setMessage(con);
                        dialog.setPositiveButton(R.string.ok_label, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialoginterface, int i) {
                            }
                        });
                        dialog.show();

                        try{
                            FileWriter fw = new FileWriter("/sdcard/output.txt", true);
                            BufferedWriter bw = new BufferedWriter(fw); //將BufferedWeiter與FileWrite物件做連結
                            bw.write(con + " 已傳送郵件");
                            bw.newLine();
                            bw.close();
                        }catch(IOException e){
                            e.printStackTrace();
                        }

                        Resources res = getResources();
                        Bitmap bmp = BitmapFactory.decodeResource(res, R.drawable.print_logo);

                        mPrinter.printImage(bmp);
                        mPrinter.setTitle("順奇企業　收款證明", "", null);
                        mPrinter.printTitle();
                        mPrinter.setPrinter(BluetoothPrinter.COMM_PRINT_AND_NEWLINE);
                        mPrinter.setCharacterMultiple(1, 1);
                        mPrinter.printText(dts + "\n");
                        mPrinter.setPrinter(BluetoothPrinter.COMM_PRINT_AND_NEWLINE);
                        mPrinter.setCharacterMultiple(0, 0);
                        mPrinter.printText(hashs + "\n");
                        mPrinter.setPrinter(BluetoothPrinter.COMM_PRINT_AND_NEWLINE);
                        mPrinter.setCharacterMultiple(1, 1);
                        mPrinter.printText(idv + "\n" + cav + "\n" + ca2v + "\n");
                        mPrinter.setPrinter(BluetoothPrinter.COMM_PRINT_AND_NEWLINE);
                        mPrinter.setCharacterMultiple(0, 0);
                        mPrinter.printText("\n\n" + hashs);
                        mPrinter.setPrinter(BluetoothPrinter.COMM_PRINT_AND_NEWLINE);
                        mPrinter.setPrinter(BluetoothPrinter.COMM_PRINT_AND_NEWLINE);
                        mPrinter.setPrinter(BluetoothPrinter.COMM_PRINT_AND_NEWLINE);
                        mPrinter.setPrinter(BluetoothPrinter.COMM_PRINT_AND_NEWLINE);
                        mPrinter.setPrinter(BluetoothPrinter.COMM_PRINT_AND_NEWLINE);
                        mPrinter.setPrinter(BluetoothPrinter.COMM_PRINT_AND_NEWLINE);
                        mPrinter.setPrinter(BluetoothPrinter.COMM_PRINT_AND_NEWLINE);
                        clientidvalue.setText("");
                        cash2value.setText("");
                        cashvalue.setText("");
                    } catch (Exception e) {
                        Log.e("SendMail", e.getMessage(), e);
                    }
                } else if (!checkStatus() && isConnected) {
                    try {
                        try{
                            FileWriter fw = new FileWriter("/sdcard/output.txt", true);
                            BufferedWriter bw = new BufferedWriter(fw); //將BufferedWeiter與FileWrite物件做連結
                            bw.write(con + " 未傳送郵件");
                            bw.newLine();
                            bw.close();
                        }catch(IOException e){
                            e.printStackTrace();
                        }

                        AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
                        dialog.setTitle("傳送失敗！");
                        dialog.setMessage("未連接網路。收據已印出。");
                        dialog.setPositiveButton(R.string.ok_label, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialoginterface, int i) {
                            }
                        });
                        dialog.show();

                        Resources res = getResources();
                        Bitmap bmp = BitmapFactory.decodeResource(res, R.drawable.print_logo);

                        mPrinter.printImage(bmp);
                        mPrinter.setTitle("順奇企業　收款證明", "", null);
                        mPrinter.printTitle();
                        mPrinter.setPrinter(BluetoothPrinter.COMM_PRINT_AND_NEWLINE);
                        mPrinter.setCharacterMultiple(1, 1);
                        mPrinter.printText(dts + "\n");
                        mPrinter.setPrinter(BluetoothPrinter.COMM_PRINT_AND_NEWLINE);
                        mPrinter.setCharacterMultiple(0, 0);
                        mPrinter.printText(hashs + "\n");
                        mPrinter.setPrinter(BluetoothPrinter.COMM_PRINT_AND_NEWLINE);
                        mPrinter.setCharacterMultiple(1, 1);
                        mPrinter.printText(idv + "\n" + cav + "\n" + ca2v + "\n");
                        mPrinter.setPrinter(BluetoothPrinter.COMM_PRINT_AND_NEWLINE);
                        mPrinter.setCharacterMultiple(0, 0);
                        mPrinter.printText("\n\n" + hashs);
                        mPrinter.setPrinter(BluetoothPrinter.COMM_PRINT_AND_NEWLINE);
                        mPrinter.setPrinter(BluetoothPrinter.COMM_PRINT_AND_NEWLINE);
                        mPrinter.setPrinter(BluetoothPrinter.COMM_PRINT_AND_NEWLINE);
                        mPrinter.setPrinter(BluetoothPrinter.COMM_PRINT_AND_NEWLINE);
                        mPrinter.setPrinter(BluetoothPrinter.COMM_PRINT_AND_NEWLINE);
                        mPrinter.setPrinter(BluetoothPrinter.COMM_PRINT_AND_NEWLINE);
                        mPrinter.setPrinter(BluetoothPrinter.COMM_PRINT_AND_NEWLINE);
                        clientidvalue.setText("");
                        cash2value.setText("");
                        cashvalue.setText("");
                    } catch (Exception e) {
                        Log.e("NoSendMail", e.getMessage(), e);
                    }
                } else {
                    try{
                        FileWriter fw = new FileWriter("/sdcard/output.txt", true);
                        BufferedWriter bw = new BufferedWriter(fw); //將BufferedWeiter與FileWrite物件做連結
                        bw.write(con + " 未列印收據");
                        bw.newLine();
                        bw.close();
                    }catch(IOException e){
                        e.printStackTrace();
                    }

                    AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
                    dialog.setTitle("傳送失敗！");
                    dialog.setMessage("請連接印表機。");
                    dialog.setPositiveButton(R.string.ok_label, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialoginterface, int i) {
                        }
                    });
                    dialog.show();
                }
            }
        }
    };

    //writeToFile 方法如下
    private void writeToFile(File fout, String data) {
        FileOutputStream osw = null;
        try {
            osw = new FileOutputStream(fout);
            osw.write(data.getBytes());
            osw.flush();
        } catch (Exception e) {
            ;
        } finally {
            try {
                osw.close();
            } catch (Exception e) {
                ;
            }
        }
    }

    //檢查外部儲存體是否可以進行寫入
    public boolean isExtStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    //檢查外部儲存體是否可以進行讀取
    public boolean isExtStorageReadable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }
        return false;
    }

    public boolean checkStatus() {
        final ConnectivityManager connMgr = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        final android.net.NetworkInfo mobile = connMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        if (mobile.isConnected()) return true;
        else return false;
    }

    public void settingview() {
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);

        setting = new Dialog(MainActivity.this);
        setting.setCancelable(false);
        setting.setTitle("設定資訊");
        setting.setContentView(R.layout.setting_layout);
        pathidtx = (EditText) setting.findViewById(R.id.pathid);
        emailretx = (EditText) setting.findViewById(R.id.emailre);
        gmailacctx = (EditText) setting.findViewById(R.id.gmailacc);
        gmailpasstx = (EditText) setting.findViewById(R.id.gmailpass);
        printername = (EditText) setting.findViewById(R.id.printername);

        pathidtx.setText(settings.getString("PATH_ID", "尚未設定"));
        emailretx.setText(settings.getString("EMAIL_RE", "尚未設定"));
        gmailacctx.setText(settings.getString("GMAIL_ACC", "尚未設定"));
        gmailpasstx.setText(settings.getString("GMAIL_PASS", "尚未設定"));
        printername.setText(settings.getString("PRINTER_NAME", "T9 BT Printer"));


        Button setokbtn = (Button) setting.findViewById(R.id.setokbtn);
        setokbtn.setOnClickListener(okbtnlis2);
        Button changebtn = (Button) setting.findViewById(R.id.changebtn);
        changebtn.setOnClickListener(changebtnlis);
        Button cancelbtn2 = (Button) setting.findViewById(R.id.cancelbtn2);
        cancelbtn2.setOnClickListener(btncanlis2);
        setting.show();
    }

    private View.OnClickListener btncanlis2 = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            setting.cancel();
        }
    };

    private View.OnClickListener changebtnlis = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            //更改密碼
            firstPassword = new Dialog(MainActivity.this);
            firstPassword.setTitle("更改密碼");
            firstPassword.setCancelable(false);
            firstPassword.setContentView(R.layout.firstpass_layout);
            Button firstpsbtn = (Button) firstPassword.findViewById(R.id.firstpsbtn);
            firstpsbtn.setOnClickListener(firstbtnok);
            firstPassword.show();
            setting.cancel();
        }
    };

    private View.OnClickListener okbtnlis2 = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
            SharedPreferences.Editor editor = settings.edit();
            editor.putString("PATH_ID", pathidtx.getText().toString());
            editor.putString("EMAIL_RE", emailretx.getText().toString());
            editor.putString("GMAIL_ACC", gmailacctx.getText().toString());
            editor.putString("GMAIL_PASS", gmailpasstx.getText().toString());
            editor.putString("PRINTER_NAME", printername.getText().toString());
            editor.commit();
            setting.cancel();
        }
    };

}
