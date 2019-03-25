package com.yzq.zxing;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.RelativeLayout;

import com.yzq.zxinglibrary.common.Constant;

public class FragmentActivity extends AppCompatActivity {

    private Toolbar toolbar;

    private ScanFragment scanFragment=new ScanFragment();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragment);
        initView();


    }

    private void initView() {
        toolbar = findViewById(R.id.toolbar);

        toolbar.setTitle("在Fragment中使用扫一扫");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        getSupportFragmentManager().beginTransaction().replace(R.id.container,scanFragment).commit();


    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode==1111){
            if (data != null) {

                String content = data.getStringExtra(Constant.CODED_CONTENT);
               Log.i("扫描结果为:", content);

               scanFragment.onActivityResult(requestCode,resultCode,data);

            }
        }

    }
}
