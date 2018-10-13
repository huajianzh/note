# Windows下配置Git服务器

标签（空格分隔）： git服务端

---

[toc]

---

##  Java环境配置
1、安装jdk
2、配置Java环境变量.
    右键”计算机” => ”属性” => ”高级系统设置” => ”高级” => “环境变量” => “系统变量”。

a.新建：变量名：JAVA_HOME
      变量值：C:\Program Files\Java\jdk1.8.0_101（具体要根据你自己的安装路径）
    
b.新建：变量名：CLASSPATH
变量值：.;%JAVA_HOME%/lib/dt.jar;%JAVA_HOME%/lib/tools.jar
    
c.添加：找到PATH变量，选择编辑。把%JAVA_HOME%/bin;%JAVA_HOME%/jre/bin添加到”变量值”的结尾处。

##  Gitblit安装和配置
### 安装Gitblit

1、下载Gitblit.下载地址：http://www.gitblit.com/
![图片.png-58.5kB][1]

2、解压缩下载的压缩包即可，无需安装。
![图片.png-58.9kB][2]

3、创建用于存储资料的文件夹
![图片.png-13.3kB][3]

### 配置
配置gitblit.properties文件

1、找到Git目录下的data文件下的gitblit.properties文件，“记事本”打开。
>注意：当前1.8版本的配置中有include = defaults.properties，这里默认配置放在defaults.properties中，所以可以直接打开defaults.properties来修改即可。

![图片.png-9.8kB][4]

2、在配置中找到git.repositoriesFolder(资料库路径)，赋值为安装中第3步创建好的文件目录。
![图片.png-12.2kB][5]

3、找到server.httpPort，设定http协议的端口号，如这里设置为10101
![图片.png-15.2kB][6]

4、找到server.httpBindInterface，设定服务器的IP地址。这里就设定你的服务器IP，本机可以直接使用localhost
![图片.png-11.8kB][7]

5、找到server.httpsBindInterface，设定为localhost
![图片.png-8.3kB][8]

6、保存，关闭文件。

### 运行
1、找到bitblit目录中的gitblit.cmd文件，双击。
![图片.png-60.1kB][9]

2、运行结果看到如下提示即可运行成功
![图片.png-6.7kB][10]

3、在浏览器中输入地址和端口看到如下界面即说明Git服务启动成功
![图片.png-18.7kB][11]

### 配置Git以系统服务的形式启动
1、在Gitblit目录下，找到installService.cmd文件，用记事本打开。
![图片.png-172.7kB][12]

2、修改 ARCH
32位系统：SET ARCH=x86
64位系统：SET ARCH=amd64

3、添加 CD 为程序目录
SET CD = D:\Git\gitblit-1.8.0(你的实际目录)
![图片.png-58.7kB][13]

4、修改StartParams里的启动参数，给空就可以了。
![图片.png-20.8kB][14]

5、保存，关闭文件。
6、找到Gitblit目录下的installService.cmd文件，以管理员身份运行。
![图片.png-64.9kB][15]

7、在服务器的服务管理下，就能看到已经存在的gitblit服务了。
![图片.png-28.2kB][16]
admin/admin

平时使用时，保持这个服务是启动状态就可以了。

  [1]: http://static.zybuluo.com/whunfand/r7w5p6aj8nh03yfv1hs9t6up/%E5%9B%BE%E7%89%87.png
  [2]: http://static.zybuluo.com/whunfand/60evjwij9v2gwpw3blwk0ju2/%E5%9B%BE%E7%89%87.png
  [3]: http://static.zybuluo.com/whunfand/qi0w2t3uckn64ceimdcuaq0v/%E5%9B%BE%E7%89%87.png
  [4]: http://static.zybuluo.com/whunfand/0okduazh6t64xwwb78wa7b4b/%E5%9B%BE%E7%89%87.png
  [5]: http://static.zybuluo.com/whunfand/0cwh9fymmbl8hip6jjdt49pu/%E5%9B%BE%E7%89%87.png
  [6]: http://static.zybuluo.com/whunfand/qhoonqkl6h7ol7wdha1chsj7/%E5%9B%BE%E7%89%87.png
  [7]: http://static.zybuluo.com/whunfand/82jkbp2sylks1bagj01pw8ax/%E5%9B%BE%E7%89%87.png
  [8]: http://static.zybuluo.com/whunfand/8wm4h5p91whffsae7nww2ivp/%E5%9B%BE%E7%89%87.png
  [9]: http://static.zybuluo.com/whunfand/ng8ux70fs82t402ft7un8x0g/%E5%9B%BE%E7%89%87.png
  [10]: http://static.zybuluo.com/whunfand/stplbn1lw8ckydcwiw6c4zjt/%E5%9B%BE%E7%89%87.png
  [11]: http://static.zybuluo.com/whunfand/oatva7knv52z4s1hkzs2y9qs/%E5%9B%BE%E7%89%87.png
  [12]: http://static.zybuluo.com/whunfand/z4rk57kvwo1mddmd86c6wc89/%E5%9B%BE%E7%89%87.png
  [13]: http://static.zybuluo.com/whunfand/d75k1s5b5o1ef083xoqg2lku/%E5%9B%BE%E7%89%87.png
  [14]: http://static.zybuluo.com/whunfand/yi3v2ya0zq9n3rsg4lxuuky2/%E5%9B%BE%E7%89%87.png
  [15]: http://static.zybuluo.com/whunfand/yapszsfqaztt0hu4jxlgm605/%E5%9B%BE%E7%89%87.png
  [16]: http://static.zybuluo.com/whunfand/xldkh54so7lequ9poc3sxfyq/%E5%9B%BE%E7%89%87.png