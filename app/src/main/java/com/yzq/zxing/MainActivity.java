package com.yzq.zxing;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.PermissionListener;
import com.yzq.zxinglibrary.Consants;
import com.yzq.zxinglibrary.android.CaptureActivity;

import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Button btn;
    private TextView resultTv;


    /*自己随便定义请求码*/
    private final int REQUEST_CODE_SCAN = 555;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btn = (Button) findViewById(R.id.btn);
        btn.setOnClickListener(this);

        resultTv = (TextView) findViewById(R.id.resultTv);
    }

    @Override
    public void onClick(View view) {

        /*先申请相机权限*/
        AndPermission.with(this)
                .permission(Manifest.permission.CAMERA)
                .callback(new PermissionListener() {
                    @Override
                    public void onSucceed(int requestCode, @NonNull List<String> grantPermissions) {
                        Intent intent = new Intent(MainActivity.this,
                                CaptureActivity.class);
                        startActivityForResult(intent, REQUEST_CODE_SCAN);
                    }

                    @Override
                    public void onFailed(int requestCode, @NonNull List<String> deniedPermissions) {
                        Toast.makeText(MainActivity.this, "您没有给权限，请检查", Toast.LENGTH_LONG).show();
                    }
                }).start();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // 扫描二维码/条码回传
        if (requestCode == REQUEST_CODE_SCAN && resultCode == RESULT_OK) {
            if (data != null) {
                String content = data.getStringExtra(Consants.CODED_CONTENT);
                resultTv.setText("扫描结果为：" + content);
            }
        }
    }
}
