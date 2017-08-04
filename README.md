# zxing


基于Google zxing的一个简化版的二维码扫描。
之前在csdn上写了一篇集成的方法，需要复制一些资源文件，然后还要修改源码等，很是麻烦。

[csdn博客地址](http://blog.csdn.net/yuzhiqiang_1993/article/details/52805057)

所以特地给封装了下，要想使用的话直接添加依赖就可以了。

效果图如下：

</br>

![图示](https://github.com/yuzhiqiang1993/zxing/blob/master/20161021114316025.gif "gif")  

# 使用步骤
 
 1.首先，打开你Project 中build.gradle,在repositories中添加
 
 		 maven { url 'https://jitpack.io' }
 
 
示例：
```
 allprojects {
    repositories {
        jcenter()
        maven { url 'https://jitpack.io' }
    }
}
```


2.在你的app  moudle下添加依赖
```
  compile 'com.github.yuzhiqiang1993:zxing:1.5'
```

3.在AndroidManifest.xml中声明相机权限

```
 <uses-permission android:name="android.permission.CAMERA"/>

```

声明activity

```

   
        <activity
            android:name="com.yzq.zxinglibrary.android.CaptureActivity"
            android:screenOrientation="portrait"
            android:theme="@android:style/Theme.NoTitleBar"
            android:windowSoftInputMode="adjustPan|stateHidden">

        </activity>
```



4.在你需要扫描二维码地方启动扫描即可,注意在6.0以上的手机需要先给相机权限，否则会报错。
```
Intent intent = new Intent(MainActivity.this,
                                CaptureActivity.class);
                        startActivityForResult(intent, REQUEST_CODE_SCAN);
```


5.接收扫描结果，注意，这里的 Consants 是 com.yzq.zxinglibrary; 包下的，不要引错了。

```
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

```


