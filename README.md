# zxing


基于Google zxing的一个简化版的二维码扫描。
之前在csdn上写了一篇集成的方法，比较麻烦，导致有些同学不知道哪个地方出问题了。
所以特地给封装了下，要想使用的话直接添加依赖就可以了。

首先来看看效果图：
![enter description here][1]
  
  
 
 
 使用步骤：
 1.在你project moudle中的build.gradle中的repositories添加

``` stylus
allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
```


2.在app moudle下添加依赖

``` stylus
dependencies {
	        compile 'com.github.yuzhiqiang1993:zxing:1.0'
	}
```

3、在清单文件中声明activity


  [1]: ./images/20161021114316025.gif "20161021114316025"