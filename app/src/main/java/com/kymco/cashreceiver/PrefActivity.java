package com.kymco.cashreceiver;


import android.os.Bundle;
import android.preference.PreferenceActivity;

public class PrefActivity extends PreferenceActivity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 指定使用的設定畫面配置資源
        // 這行敘述從API Level 11開始會產生警告訊息
        // 不過不會影響應用程式的運作
        addPreferencesFromResource(R.xml.mypreference);
    }
}
