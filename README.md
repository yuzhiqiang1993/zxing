[![](https://jitpack.io/v/yuzhiqiang1993/zxing.svg)](https://jitpack.io/#yuzhiqiang1993/zxing)

# zxing
一句话依赖就能实现扫一扫功能，包含闪光灯开关，选取本地图片解析，生成二维码(可带logo)功能等。

如果直接依赖不满足你的需求，需要自己修改样式或源码的话，可以将该库作为module集成到你的项目中，集成方法参考我的这篇博客：https://blog.csdn.net/yuzhiqiang_1993/article/details/52805057


版本说明：
https://github.com/yuzhiqiang1993/zxing/releases

博客： http://blog.csdn.net/yuzhiqiang_1993/article/details/78292004


扫描二维码下载APK体验一下
========
![扫描二维码](https://github.com/yuzhiqiang1993/zxing/blob/master/img/downloadApk.png "扫描下载APK")


首先来看看效果图
========


下图分别为 完整示例，扫描二维码，扫描条码，闪光灯，解析二维码图片，解析条码图片，生成二维码,修改扫描框四个角的颜色，修改扫描框边线颜色
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
先在 build.gradle(Project:XXXX) 的 repositories 添加``` maven { url 'https://jitpack.io' }```
一定要加上这个，否则会提示依赖失败

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

最新版本：https://github.com/yuzhiqiang1993/zxing/releases
[![](https://jitpack.io/v/yuzhiqiang1993/zxing.svg)](https://jitpack.io/#yuzhiqiang1993/zxing)
 ```
 dependencies {
    compile fileTree(dir: 'libs', include: ['*.jar'])
    compile 'com.android.support:appcompat-v7:26.1.0'
    
    /*添加依赖*/
    implementation 'com.github.yuzhiqiang1993:zxing:2.2.5'
}

 
 ```
 
 2.权限
 --------------
 
 demo使用的权限申请是严大的一个开源库，地址：https://github.com/yanzhenjie/AndPermission 感谢严大！
 
 需要申请的权限有：
 
   ```
   Manifest.permission.CAMERA
   Manifest.permission.READ_EXTERNAL_STORAGE
  
   ```
   
   
   项目中用到的所有权限
   
   ```
   <uses-permission android:name="android.permission.CAMERA" />
   <uses-permission android:name="android.permission.FLASHLIGHT" />
   <uses-feature android:name="android.hardware.camera" />
   <uses-feature android:name="android.hardware.camera.autofocus" />
   <uses-permission android:name="android.permission.VIBRATE" />
   <uses-permission android:name="android.permission.WAKE_LOCK" />
   <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
   <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
   ```

 
3.跳转到扫一扫界面：
--------------

1.使用默认配置项，两行代码即可

```
Intent intent = new Intent(MainActivity.this, CaptureActivity.class);
startActivityForResult(intent, REQUEST_CODE_SCAN);
```

2.自定义配置项
```
Intent intent = new Intent(MainActivity.this, CaptureActivity.class);
/*ZxingConfig是配置类
*可以设置是否显示底部布局，闪光灯，相册，
* 是否播放提示音  震动
* 设置扫描框颜色等
* 也可以不传这个参数
* */
ZxingConfig config = new ZxingConfig();
config.setPlayBeep(true);//是否播放扫描声音 默认为true
config.setShake(true);//是否震动  默认为true
config.setDecodeBarCode(true);//是否扫描条形码 默认为true
config.setReactColor(R.color.colorAccent);//设置扫描框四个角的颜色 默认为白色
config.setFrameLineColor(R.color.colorAccent);//设置扫描框边框颜色 默认无色
config.setScanLineColor(R.color.colorAccent);//设置扫描线的颜色 默认白色
config.setFullScreenScan(false);//是否全屏扫描  默认为true  设为false则只会在扫描框中扫描
intent.putExtra(Constant.INTENT_ZXING_CONFIG, config);
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
         
                    /*
                    * contentEtString：字符串内容
                    * w：图片的宽
                    * h：图片的高
                    * logo：不需要logo的话直接传null
                    * */

                    Bitmap logo = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);
                    bitmap = CodeCreator.createQRCode(contentEtString, 400, 400, logo);
             
              
```
>

ok,搞定了，就是这么简单，如果你觉得还行的话，麻烦给个start呦，有什么问题和建议直接提Issues,谢谢。
--------------------------
