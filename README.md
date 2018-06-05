


文章目录：
- [前言](#前言)
- [说明](#说明)
- [如何使用](#如何使用)
- [其他说明](#其他说明) 
- [更多](#更多)
    - [TODO](##TODO)
    - [相关文章](##相关文章)
- [关于个人](#关于个人)


## 前言

<div >   
 <img src="http://i.imgur.com/4498nb3.jpg" width = "500" height = "250" alt="图片名称" align=center />
</div>




**皮！就是这么皮**


**什么?想用比较新比较火的组件化和MVP这么办?**


**十秒带你过山车式体验面向插件开发的快感** 



**组件化,MVP,阿里Atlas(插件化)都在这里了**


## 说明


 


- [componentPlugin](https://github.com/chengzichen/component) *是IDEA和Android Studio的上功能的插件，以[ComponentGradlePlugin](https://github.com/chengzichen/Flyabbit/blob/master/%E7%BB%84%E4%BB%B6%E5%8C%96%E7%9A%84%E4%BD%BF%E7%94%A8.md)和[FMVP-SDk](https://github.com/chengzichen/Flyabbit/blob/master/Fmvp%E4%BB%8B%E7%BB%8D.md)为基础实现的快速组件化和生成MVP模板的插件，到达Flyabbit架构的效果，或者更复杂的组件化架构,当然其也完全可以脱离该项目使用*

**特点:**


1. **会配置好gradle插件替代需要组件化Moudle中的```com.android.library```
和```com.android.application```，以及FMVPsdk相关依赖，hostMoudle，独立运行的入口**


2. **能够灵活配置任意的组件使用组件化，也可以灵活配置宿主hostmMoudle，并能检验是否配置了component，如果已经配置过，不再重复，反之。**



3. **一键使用MVP,提供了选择最新或者适合自己的mvpsdk依赖版本(Dagger2 以及ARouter)**


关于

- [ ![Download](https://api.bintray.com/packages/chengzichen/maven/component-plugin/images/download.svg) ](https://bintray.com/chengzichen/maven/component-plugin/_latestVersion)
[![License](https://img.shields.io/badge/License-Apache%202.0-orange.svg)](https://github.com/luojilab/DDComponentForAndroid/blob/master/LICENSE)[ComponentGradlePlugin](https://github.com/chengzichen/Flyabbit/blob/master/%E7%BB%84%E4%BB%B6%E5%8C%96%E7%9A%84%E4%BD%BF%E7%94%A8.md)**是保证各个模块能独立运行的关键**
    - [源码](https://github.com/chengzichen/Flyabbit/tree/master/build-gradle)
    - [文档](https://github.com/chengzichen/Flyabbit/blob/master/%E7%BB%84%E4%BB%B6%E5%8C%96%E7%9A%84%E4%BD%BF%E7%94%A8.md)

- [ ![Download](https://api.bintray.com/packages/chengzichen/maven/mvp/images/download.svg) ](https://bintray.com/chengzichen/maven/mvp/_latestVersion) [FMVP-SDk](https://github.com/chengzichen/Flyabbit/blob/master/Fmvp%E4%BB%8B%E7%BB%8D.md)**负责MVP,数据存储和处理，提供了灵活api快速实现mvp**
    - [源码](https://github.com/chengzichen/Flyabbit/tree/master/baselib/library)  
    - [文档](https://github.com/chengzichen/Flyabbit/blob/master/Fmvp%E4%BB%8B%E7%BB%8D.md)


- [Flyabbit](https://github.com/chengzichen/Flyabbit) **是一个集MVP,组件化,插件化等为一体的项目载体**     
    - [源码](https://github.com/chengzichen/Flyabbit)
   


   **开源不易,如果大家喜欢的话欢迎Star和Fork**

# 
## 如何使用

**只需要三步搞定**


1. **安装插件:** 
 

- **步骤:** File->Setting->Plugins->按下图搜索[componentPlugin](https://github.com/chengzichen/component)(或者[下载](https://github.com/chengzichen/component/blob/master/component.jar))
,安装完后重启Andriod Studio

![](https://i.imgur.com/WGafi2S.png)



2. **新建项目和新建模块**

- **新建或者使用自己的项目下为:参考**

![](https://i.imgur.com/Fylc1UU.png)
 
 
 你也可以clone [ComponentApplication](https://github.com/chengzichen/ComponentApplication)

3. **点击```Configure Component in Project```**

- **如下图:**

**步骤:**```Tool ->  Component ->  Configure Component in Project```

    
    
<div >   
 <img src="https://github.com/chengzichen/Photo/raw/master/gif/use1.gif" width = "1000" height = "500" alt="图片名称" align=cente/>
</div>

- componentPlugin会配置好gradle插件替代他mMoudle中的app和lib，以及MVPsdk依赖，hostMoudle，独立运行的入口



## 其他说明

- **可以配置任意模块使用组件化**

<div >   
 <img src="https://github.com/chengzichen/Photo/raw/master/gif/setPlugin.gif" width = "1000" height = "500" alt="图片名称" align=cente/>
</div>

- componentPlugin能够灵活配置任意的组件使用组件化，也可以灵活配置宿主hostmMoudle，并能检验是否配置了component，如果已经配置过，不再重复，反之。

---

- **可以配置FMVP模板**

    
```
    步骤: 在对应的目录下 -> new -> FMVPFile
```


<div >   
 <img src="https://github.com/chengzichen/Photo/raw/master/gif/newMVPFile2.gif" width = "1420" height = "700" alt="图片名称" align=cente/>
</div>

- componentPlugin提供了选择最新或者适合自己的mvpsdk依赖版本
- 自动添加注解依赖: Dagger2 以及ARouter ,对象得到有效的管理以及快速实现MVP以及路由页面跳转

**注意**:考虑到mvp模板路径的正确性，对模板生成的路径有限制（只有在Moudle下的jav路径下才能生效），更好的引导大家使用




 **十秒就搞定了所有的步骤,十秒为何不大胆的尝试一下呢?**
 
 

## 最后

*细心的同学最终会发现，这个插件和kotlin插件很像，没错，由于个人没有写过插件，所以我就看着是kotlin里面的代码撸了一个。但是这个我花费我不少业务的时间去调试。所以这个插件都是kotlin写的，对于学习kotlin也是非常有帮助，还有对于学习写插件也是一个很不错的选择，让你不再是只写百度上hello基础插件的新手。
这个插件我写了两个版本，一个是gradle配置版本，一个是插件的版本*


**为了达到十秒搞定这些组件化配置，我在这上面花费的时间可能远远不止10秒，可能是几何倍数，开源不易。您的star或者留言鼓励，可能是对我们的最大的鼓励。谢谢**


[https://github.com/chengzichen/Flyabbit](https://github.com/chengzichen/Flyabbit)

[https://github.com/chengzichen/component](https://github.com/chengzichen/component)

### TODO

- [ ] 分别添加单独配置组件化和mvp模板的快捷键,完全分开
- [ ] 更加优化代码
- [ ] 加上漂亮的图标
....

### 相关文章

- 关于FMVP:[https://github.com/chengzichen/Flyabbit/Fmvp](https://github.com/chengzichen/Flyabbit/blob/master/Fmvp%E4%BB%8B%E7%BB%8D.md)

第一篇-网络篇:

 - [[从零开始系列]AndroidApp研发之路(一) 网络请求的封装(一)](http://blog.csdn.net/chengzichen_/article/details/77659318)

第二篇-Retrofit源码解析

  - [[从零开始系列]AndroidApp研发之路-<楼外篇>Retrofit的刨根问底篇](http://blog.csdn.net/chengzichen_/article/details/77840996)
  
  更新中....
  
## 关于个人
     
   
  
  Github:[https://github.com/chengzichen](https://github.com/chengzichen)
  
  CSDN : [http://blog.csdn.net/chengzichen_](http://blog.csdn.net/chengzichen_)


<div  align="center"> 
本人一直都致力于组件化和插件化的研究如果大家有更好的想法可以联系我一起成长
</div>

<div  align="center">   
 <img src="https://i.imgur.com/J1LpBum.jpg" width = "200" height = "300" alt="图片名称" align=center />
</div>


