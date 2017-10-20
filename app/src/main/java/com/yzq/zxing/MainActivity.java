package com.yzq.zxing;

import android.Manifest;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.WriterException;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.PermissionListener;
import com.yzq.zxinglibrary.android.CaptureActivity;
import com.yzq.zxinglibrary.bean.ZxingConfig;
import com.yzq.zxinglibrary.common.Constant;
import com.yzq.zxinglibrary.encode.CodeCreator;

import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Button scanBtn;
    private TextView result;
    private EditText contentEt;
    private Button encodeBtn;
    private ImageView contentIv;
    private int REQUEST_CODE_SCAN = 111;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }


    private void initView() {
        /*扫描按钮*/
        scanBtn = (Button) findViewById(R.id.scanBtn);
        scanBtn.setOnClickListener(this);
        /*扫描结果*/
        result = (TextView) findViewById(R.id.result);

        /*要生成二维码的输入框*/
        contentEt = (EditText) findViewById(R.id.contentEt);
        /*生成按钮*/
        encodeBtn = (Button) findViewById(R.id.encodeBtn);
        encodeBtn.setOnClickListener(this);
        /*生成的图片*/
        contentIv = (ImageView) findViewById(R.id.contentIv);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.scanBtn:

                AndPermission.with(this)
                        .permission(Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE)
                        .callback(new PermissionListener() {
                            @Override
                            public void onSucceed(int requestCode, @NonNull List<String> grantPermissions) {
                                Intent intent = new Intent(MainActivity.this, CaptureActivity.class);

                                ZxingConfig config = new ZxingConfig();
                                config.setPlayBeep(true);
                                config.setShake(true);
                                intent.putExtra(Constant.INTENT_ZXING_CONFIG, config);

                                startActivityForResult(intent,REQUEST_CODE_SCAN);

                            }

                            @Override
                            public void onFailed(int requestCode, @NonNull List<String> deniedPermissions) {

                            }
                        }).start();


                break;
            case R.id.encodeBtn:
                String contentEtString = contentEt.getText().toString().trim();
                if (TextUtils.isEmpty(contentEtString)) {
                    Toast.makeText(this, "contentEtString不能为空", Toast.LENGTH_SHORT).show();
                    return;
                }
                Bitmap bitmap = null;
                try {
                    bitmap = CodeCreator.createQRCode(contentEtString, 400, 400, null);
                } catch (WriterException e) {
                    e.printStackTrace();
                }
                if (bitmap != null) {
                    contentIv.setImageBitmap(bitmap);
                }

                break;
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // 扫描二维码/条码回传
        if (requestCode == REQUEST_CODE_SCAN && resultCode == RESULT_OK) {
            if (data != null) {

                String content = data.getStringExtra(Constant.CODED_CONTENT);
                result.setText("扫描结果为：" + content);
            }
        }
    }

}
