# zxing
一句话依赖就能实现扫一扫功能，包含闪光灯开关，选取本地图片解析，生成二维码(可带logo)功能等。

更新日志：
https://github.com/yuzhiqiang1993/zxing/wiki/update-log

博客： http://blog.csdn.net/yuzhiqiang_1993/article/details/78292004


扫描二维码下载APK体验一下
========
![扫描二维码](https://github.com/yuzhiqiang1993/zxing/blob/master/img/downloadApk.png "扫描下载APK")


首先来看看效果图
========


下图分别为 扫描二维码，扫描条码，闪光灯，解析二维码图片，解析条码图片，生成二维码
------------------------
![扫描二维码](https://github.com/yuzhiqiang1993/zxing/blob/master/img/scanEwm.gif "扫描二维码")
![扫描条码](https://github.com/yuzhiqiang1993/zxing/blob/master/img/scanTm.gif "扫描条码")
![闪光灯](https://github.com/yuzhiqiang1993/zxing/blob/master/img/flashlight.gif "闪光灯")
![解析二维码图片](https://github.com/yuzhiqiang1993/zxing/blob/master/img/decodeEWM.gif "解析二维码图片")
![解析条码图片](https://github.com/yuzhiqiang1993/zxing/blob/master/img/decodeTM.gif "解析条码图片")
![生成二维码图片](https://github.com/yuzhiqiang1993/zxing/blob/master/img/createEwm.gif "生成二维码图片")

>
使用方法
========


1.添加依赖
--------------------
先在 build.gradle(Project:XXXX) 的 repositories 添加:

```
allprojects {
    repositories {
        google()
        jcenter()
        maven { url 'https://jitpack.io' }
    }
}
```

然后在 build.gradle(Module:app) 的 dependencies 添加:
 ```
 dependencies {
    compile fileTree(dir: 'libs', include: ['*.jar'])
    compile 'com.android.support:appcompat-v7:26.1.0'
    
    /*添加依赖  android studio3.0及以上版本可用implementation代替compile*/
    compile 'com.github.yuzhiqiang1993:zxing:2.1.0'
}

 
 ```
 
 2.申请权限，需要申请的权限有：
 --------------
   ```
   Manifest.permission.CAMERA
   Manifest.permission.READ_EXTERNAL_STORAGE
   ```
 
3.跳转到扫一扫界面：
--------------

```
private int REQUEST_CODE_SCAN = 111;

/*ZxingConfig是配置类  可以设置是否显示底部布局，闪光灯，相册，是否播放提示音  震动等动能
* 也可以不传这个参数
* 不传的话  默认都为默认不震动  其他都为true
* */
//ZxingConfig config = new ZxingConfig();
//config.setShowbottomLayout(true);//底部布局（包括闪光灯和相册）
//config.setPlayBeep(true);//是否播放提示音
//config.setShake(true);//是否震动
//config.setShowAlbum(true);//是否显示相册
//config.setShowFlashLight(true);//是否显示闪光灯


//如果不传 ZxingConfig的话，两行代码就能搞定了
Intent intent = new Intent(MainActivity.this, CaptureActivity.class);
//intent.putExtra(Constant.INTENT_ZXING_CONFIG, config);
startActivityForResult(intent, REQUEST_CODE_SCAN);

```

4.接收扫描结果
-------------------------------------------
注意：Constant.CODED_CONTENT引的是这个com.yzq.zxinglibrary.common.Constant

```
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

```


5.生成二维码
-------------------------------
```
                String contentEtString = contentEt.getText().toString().trim();
                
                if (TextUtils.isEmpty(contentEtString)) {
                    Toast.makeText(this, "contentEtString不能为空", Toast.LENGTH_SHORT).show();
                    return;
                }
                Bitmap bitmap = null;
                try {
                    /*
                    * contentEtString：字符串内容
                    * w：图片的宽
                    * h：图片的高
                    * logo：不需要logo的话直接传null
                    * */

                    Bitmap logo = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);
                    bitmap = CodeCreator.createQRCode(contentEtString, 400, 400, logo);
                } catch (WriterException e) {
                    e.printStackTrace();
                }
                

```
>

ok,搞定了，就是这么简单，如果你觉得还行的话，麻烦给个start呦，有什么问题和建议直接提Issues,谢谢。
--------------------------
