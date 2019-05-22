
 ## 目的
 
本竞品分析主要通过对**腾讯WiFi管家**和**WiFi万能钥匙**的比较，分析两款产品在**响应、搜索WiFi能力、内存分析、GPU渲染、耗电量、市场竞争力、功能分析、用户交互分析**等8个方面的优劣势，从而得出目前两款产品的目前现状和发展前景。


测试机型参数的说明：

设备参数名字|参数内容
:---:|:--:
设备型号 |HUAWEI CAZ-TL220C01B387|
Android版本| 7.0 API24
处理器|8核 2GHz
运行内存|4.0GB
闪存|64GB
WiFi支持|频率：2.4G和5G，协议：802.11a/b/g/n/ac。


## 响应
主要测试点及测试结果：
### 1. 冷启动：首次启动app的时间间隔
   指标：
- ThisTime：该Activity的启动耗时

- TotalTime: 应用自身启动耗时, ThisTime+应用application等资源启动时间

- WaitTime:系统启动应用耗时, TotalTime+系统资源启动时间
(一般主要看TotalTime)


在命令行执行：
``` 
adb shell am start -W com.tencent.wifimanager/com.tencent.wifimanager.MainActivity
adb shell am start -W com.snda.wifilocating/com.lantern.launcher.ui.MainActivity
```

三次运行的结果：

指标|腾讯WiFi管家|WiFi万能钥匙
:---:|:--:|:---:
ThisTime|466 509 547 |995 1007 980   
TotalTime|768  810  830 |995 1007 980    
WaitTime|793 829 850|1015 1022 1000

分析：从数据上看，腾讯WiFi管家的冷启动时间小于WiFi万能钥匙。从用户体验上来，在冷启动时，腾讯WiFi管家会出现一个Logo，然后能比较快进去主界面。WiFi万能钥匙冷启动时也会有一个Logo界面，然后是广告界面，再进入到主界面。从用户体验来看，个人感觉WiFi万能钥匙加载速度要慢一些。

从数据上来看会有一个发现：
发现一个情况：由于只是启动一个Activity的测试，正常情况下：displayStartTime与mLaunchStartTime指向的是同一时间点，也就是ThisTime应该等于TotalTime，WiFi万能钥匙的数据满足这一点。但是我们看到腾讯WiFi管家的ThisTime<TotalTime，这猜想是腾讯WiFi管家的MainActivity是后台而不是页面加载的Activity)


### 2. 热启动：非首次启动app的时间间隔
用home键来模拟热启动，同样是测试三次

指标|腾讯WiFi管家|WiFi万能钥匙
:---:|:--:|:---:
ThisTime|132 153 139 |130 130 149 
TotalTime|183 202 188  |143 143 165
WaitTime|224  235 224|150 150 162

可以看到两个应用的热启动时间相差不大，整体的感觉也加载很流畅。

## 搜索WiFi能力
### 1. 搜索WiFi的数量
在不同的地点来测试两个应用能搜索到WiFi的数量

地点| 腾讯WiFi管家|WiFi万能钥匙
:---:|:--:|:--:
南方通信大厦三楼|33个(12个免费WiFi)|31个(11个免费WiFi）
天河客运站|27个(5个免费WiFi)|21个(3个免费WiFi）
岗顶地铁站C出口麦当劳|28个(12个免费WiFi)|20个(8个免费WiFi）

从数据上来看，腾讯WiFi管家无论是从免费WiFi的数量还是总的WiFi数量上都要优于WiFi万能钥匙。

### 2. 连接免费WiFi的成功率

地点| 腾讯WiFi管家|WiFi万能钥匙
:---:|:--:|:--:
免费WiFi数量|10|9
成功连接数量|7|6
成功率|70%|67%

从数据来看，虽然一次测试的数据可能不是很具有代表性，整体感觉两个应用能连接的免费WiFi差不多，需要认证的WiFi也会自动弹出认证页面，对用户比较友好。

### 3. 连接WiFi速度测试(Tencent-StaffWiFI)

产品 | 腾讯WiFi管家|WiFi万能钥匙
:---:|:--:|:--:
测速|301KB/s|282KB/s

wifi的实际网速在290KB/s左右，两个应用测得的wifi速度和实际值的偏差都不大。

## 内存分析
正常情况下，应用不应占用过多的内存资源，且能够及时释放内存，保证整个应用内的稳定性和流畅性。
下面是刚打开应用时的测试：
用as的Android Monitor去查CPU内存占用情况，执行下面的命令：
``` 
adb shell dumpsys meminfo com.tencent.wifimanager
adb shell dumpsys meminfo  com.snda.wifilocating
```
可以看到两个应用的各个部分的内存和总内存数据：

![](https://upload-images.jianshu.io/upload_images/7769570-3005bddff2250fac.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

用Total(实际占用的内存值)作为指标，可以看出两者占用内存的明显的差异:
腾讯WiFi管家占用内存为48078KB，WiFi万能钥匙占用内存为136185KB。

下面用可视化的方式比较两个应用的内存使用情况进一步：

利用GT对两款应用进行对比，指标选取PSS0(主程序内存)作为指标，下图为对比结果：
![](https://upload-images.jianshu.io/upload_images/7769570-2a3dd9716bcd5f00.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)


同样可以看出两者占用内存的明显的差异:
腾讯WiFi管家的PPS0平均值在48047KB，而WiFii万能钥匙的PPS0平均值高达130066KB

分析：腾讯WiFi管家的内存占用明显要比WiFi万能钥匙少。

## GPU渲染
### 过度渲染分析
过度绘制是指同一个像素位置里一次刷新进行了多次绘制。过度绘制会浪费大量的CPU以及GPU资源。
蓝色，淡绿，淡红，深红代表了4种不同程度的Overdraw情况。

测试指标：
1. 控制过渡绘制为2x
2. 不允许存在4x过渡绘制
3. 不允许存在面积超过屏幕1/4的3x过渡绘制

#### 腾讯WiFi管家几个主要界面的过度绘制如下图所示：
![](https://upload-images.jianshu.io/upload_images/7769570-53def79a0ec1ad36.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

在这里对各个页面的渲染情况做一个分析：
1. 发现界面
这个页面应该是稍微过度绘制。页面背景直接就是2x绘制，应该是背景渲染了一层然后listview也渲染了一层。所以listview的Item的图片和文字就已经到了3x绘制。

2. 连接WiFi界面
连接WiFi界面有一个很神奇的情况，上图有体现，显示WiFi列表的listview是2x绘制，但是下拉的时候有个固定区域永远是1x绘制。

3. 我的界面
我的界面是相对来说过度渲染情况最少的，背景为蓝色，1x绘制，下面的view渲染是2x绘制，子view里面的item是3x绘制。可以说是比较好的UI渲染。

#### WiFi万能钥匙几个主要界面的过度绘制如下图所示：
![](https://upload-images.jianshu.io/upload_images/7769570-0d981589e3dccc4d.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

下面简单分析WiFi万能钥匙的渲染情况：
1. 小说页面和我的页面整体渲染情况较好，背景渲染了一层，大多数是2x渲染，没有4x渲染。
2. 但是连接页面的资讯列表明显呈现深红色，应该是WiFi连接的listview是2x渲染，资讯的在WiFi的listview基础上又渲染了一层导致3+x渲染。

### 流畅度分析
![腾讯WiFi管家 vs WiFi万能钥匙](https://upload-images.jianshu.io/upload_images/7769570-4d28ed6ee0719f92.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/600)

每帧快于16ms即为流畅，而这根绿线所标示的高度即为16ms线，低于绿线即为流畅。

我们对两款应用的WiFi连接界面的流畅度进行分析，可以看出，腾讯WiFi管家大多数竖线在绿色横线一下，能保证每帧快于16ms，较为流畅。而WiFi万能钥匙有一部分远远超出绿线。可见腾讯WiFi管家的流畅度好于WiFi万能钥匙。

## 耗电分析
用PowerTutor对两款应用作耗电量分析，场景为打开应用，之后一直亮着屏幕，两个应用切到后台，比较两个应用的耗电量。

产品|腾讯WiFi管家|WiFi万能钥匙
:---:|:--:|:---:
20min的耗电量|11.3J|5.3J

从数据上来看，WiFi万能钥匙在后台时耗电量较少。

## 市场竞争力
先对两款产品做一个简单的对比：

产品|腾讯WiFi管家|WiFi万能钥匙
:---:|:--:|:---:
总下载量 |17.15亿|301.98亿
安装包大小|15.78MB|30.83MB
版本|3.7.5|4.3.61
最多权限数量|35|38
开启进程数|4|3
主打功能|免费连WiFi(硬核的功能比较多)|免费连WiFi
竞品没有的拓展功能|微信清理|读书

(数据来源于酷传http://android.kuchuan.com)

简单分析：
1. 在总下载量方面，腾讯WiFi管家不及WiFi万能钥匙，这与推出时间有关，WiFi万能钥匙在2012年推出，腾讯WiFi管家是2016年，WiFi万能钥匙是同类应用中最早出现的，占领了一大批用户。
2. 在安装包大小方面，我们的WiFi管家的大小仅有WiFi万能钥匙的一半大小，这对于手机内存较小的用户来说是较有竞争力的。但是，又出现了主打内存小的WiFi万能钥匙极速版。
3. 获取用户权限方面：两者需要的用户权限数量差不多，但差异比较大。但从引导用户开启权限这方面明显腾讯WiFi管家的用户体验更好。


## 功能分析
用户选择下载应用的目的主要是连接WiFi，当然拓展功能也很大程度上能够吸粉。下面将对两款产品的主要功能作列举和对比分析。

### 功能框架
![腾讯WiFi管家](https://upload-images.jianshu.io/upload_images/7769570-c91cf578c588e91a.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)


![WiFi万能钥匙](https://upload-images.jianshu.io/upload_images/7769570-8db73f20ab508f24.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

分析和说明：
1. 红色部分是两个应用都有的功能(其中安全检测这一个功能虽然说两个应用都有，但是腾讯WiFi管家的安全检测相对来说功能更加全面，包括WiFi安全加固、发现网上垃圾等等。WiFi万能钥匙的安全检测只有检测网络安全)，可以看出满足大多数用户使用需求的功能，比如说自动识别免费WiFi、安全检测等两者都有。
2. 个人感觉腾讯WiFi管家在专业性这方面更强。对WiFi的一些硬核的功能提供得比较多也比较全面。
3. 对比两个应用，两张图中淡绿色给出的是另外一个应用没有的核心功能。可以看出，腾讯WiFi管家的核心功能较多。


## 用户交互分析
### 腾讯WiFi管家：

优点：
1. 从UI上看，腾讯WiFi管家淡绿色的风格给人的感觉较为舒服，WiFi列表对比WiFi万能钥匙不会太紧凑。
2. 广告相对来说少，对不想看广告的用户体检较好。
3. 底部导航栏的突出安全一键连接的功能，引导用户连接免费WiFi。用户引导方面做的比较好
4. 资讯页面的文案提示人性化。

个人认为存在的一些问题：
1. 登录：可以选择微信QQ和邮箱登录，但是要用账号登录没有拉起一键登录的选项。
2. 深度清理的时候没有提示自动下载了腾讯清理大师。


### WiFi万能钥匙：

个人认为存在的一些问题：
1. 把小说放在底部导航栏中间有点本末倒置的感觉
2. 对于WiFi安全，加速等核心功能位置不明显，提供的硬核的功能较少。
3. 联动应用较多，缺乏自身应用的特点(对比腾讯WiFi管家自己的积分机制等)
4. 广告较多，资讯内容引起不适

## 结论
1. WiFi万能钥匙的安装包大小是腾讯WiFi管家的两倍，安装包体积小会是腾讯WiFi管家吸引用户的一个点。
2. 腾讯WiFi管家在内存占用大小上远远好于WiFi万能钥匙。
3. 腾讯WiFi管家的过度渲染情况比WiFi万能钥匙好，大多能控制在3x绘制以下。
4. 腾讯WiFi管家的流畅度明显好于WiFi万能钥匙。
5. 腾讯WiFi管家在UI设计和用户使用引导方面设得比较合理，开启权限等会有动画引导或者提示用户操作。
6. 腾讯WiFi管家在产品功能定位上的把握较为准确，和WiFi相关的核心功能较多而且在应用中的位置也比较明显，相比之下WiFi万能钥匙更像是以吸引用户到其他应用上为主。
7. 腾讯WiFi管家的下载量远远不如WiFi万能钥匙，这可能和投入市场时间有关。因此腾讯WiFi管家需要以后不断发展一些核心的有特色的业务来赢取用户。
