# Windows下配置Git客户端

标签（空格分隔）： git客户端

---

[toc]

---

## 安装msysgit
1、访问https://git-for-windows.github.io/
2、点击Download
![图片.png-115.8kB][1]

3、运行Git-2.11.0-64-bit.exe，然后一路next即可
![图片.png-13kB][2]

##  下载TortoiseGit
1、访问TortoiseGit下载网站：https://tortoisegit.org/download/
2、根据系统性质下载对应的安装包即可，32位机下载32位的，64位机可以下载64位的安装包。（我这里以64位为例）
![图片.png-40.9kB][3]

3、安装
![图片.png-185.5kB][4]

一路next
![图片.png-62.4kB][5]
next
![图片.png-81.4kB][6]
这里可以选择要安装的路径（我就直接默认C盘），然后next
最后可能需要重启一下电脑

4、下载语言包（这里直接下载中文语言包，点击SetUp即可下载）
![图片.png-46.3kB][7]

5、安装语言包
![图片.png-187.6kB][8]
![图片.png-187.7kB][9]

##  配置TortoiseGit
1、安装TortoiseGit成功后，可以在（桌面）任何地方右单机-->TortoiseGit-->Settings
![图片.png-17.5kB][10]
![图片.png-114.6kB][11]

##  使用TortoiseGit
1、建立版本库
在任意空的文件夹点击鼠标右键，选择“Git 在这里创建版本库”
![图片.png-22.5kB][12]
![图片.png-9.7kB][13]
![图片.png-24.2kB][14]
直接确定
![图片.png-22.2kB][15]
ok，这样就可以创建了本地版本库

2、clone库
在上面的文件夹点击右键，选择“拉取（Pull）”
![图片.png-14.3kB][16]
![图片.png-17.9kB][17]

2.1、管理远端
![图片.png-36.1kB][18]

2.2、输入远端Url
![图片.png-95kB][19]

2.3、本地生成Putty密，在“开始”菜单，选择“TortoiseGit”中的“PuttyGen”
![图片.png-66.1kB][20]
点击“Generate”按钮，然后，鼠标在图中红色方框的区域内不停的移动，即可生成密钥。
![图片.png-52.8kB][21]
点击“Save private key”按钮，把生成的密钥保存为PPK文件。
![图片.png-101.8kB][22]
确定生效，选择保存路径
![图片.png-136.6kB][23]

2.4、配置Putty密
![图片.png-110.6kB][24]
注意地址的配置
![QQ截图20170724180118.png-213.9kB][25]

3、完成clone库


  [1]: http://static.zybuluo.com/whunfand/gg362eitl4nybu9iev3m254p/%E5%9B%BE%E7%89%87.png
  [2]: http://static.zybuluo.com/whunfand/71i9c998wj87yvr6khpbfluu/%E5%9B%BE%E7%89%87.png
  [3]: http://static.zybuluo.com/whunfand/be63srrf7q10mawbdgycw07n/%E5%9B%BE%E7%89%87.png
  [4]: http://static.zybuluo.com/whunfand/gxreyththj3wfuwwce4xwtij/%E5%9B%BE%E7%89%87.png
  [5]: http://static.zybuluo.com/whunfand/mobvfph4ytoqeia3dk62fzez/%E5%9B%BE%E7%89%87.png
  [6]: http://static.zybuluo.com/whunfand/4r4is0yf4gbfb6o7h0pvuecd/%E5%9B%BE%E7%89%87.png
  [7]: http://static.zybuluo.com/whunfand/2gkj0rk9i46252dn59auyiko/%E5%9B%BE%E7%89%87.png
  [8]: http://static.zybuluo.com/whunfand/xrijuvjr101o2m8uzcy3ohto/%E5%9B%BE%E7%89%87.png
  [9]: http://static.zybuluo.com/whunfand/7dkt70lro5sjapv4golrxpl8/%E5%9B%BE%E7%89%87.png
  [10]: http://static.zybuluo.com/whunfand/6uq5vf56bunjxavtmty3bthj/%E5%9B%BE%E7%89%87.png
  [11]: http://static.zybuluo.com/whunfand/pfif354cq9a4zuj707nt2vqz/%E5%9B%BE%E7%89%87.png
  [12]: http://static.zybuluo.com/whunfand/dm4wfske54gm8t53lot96bxy/%E5%9B%BE%E7%89%87.png
  [13]: http://static.zybuluo.com/whunfand/rek64j679beizumrf8hqxtbm/%E5%9B%BE%E7%89%87.png
  [14]: http://static.zybuluo.com/whunfand/gmrvd4wiilyu0z6so181b31o/%E5%9B%BE%E7%89%87.png
  [15]: http://static.zybuluo.com/whunfand/hbwqzhh4op7m78m5527zwnds/%E5%9B%BE%E7%89%87.png
  [16]: http://static.zybuluo.com/whunfand/ji8of1syhxa8htdgp7oo8cac/%E5%9B%BE%E7%89%87.png
  [17]: http://static.zybuluo.com/whunfand/ac1z8rf887u7nu6h7f697014/%E5%9B%BE%E7%89%87.png
  [18]: http://static.zybuluo.com/whunfand/l5jxi0q8su1c8os2v699dsvc/%E5%9B%BE%E7%89%87.png
  [19]: http://static.zybuluo.com/whunfand/htnllu6usb6kz644g7sk6xks/%E5%9B%BE%E7%89%87.png
  [20]: http://static.zybuluo.com/whunfand/3qncy3txwero2rpckskqaujd/%E5%9B%BE%E7%89%87.png
  [21]: http://static.zybuluo.com/whunfand/0ptmzlvwc87uftfnxyd0gs7j/%E5%9B%BE%E7%89%87.png
  [22]: http://static.zybuluo.com/whunfand/bc1etp3vt6ulqbje2nv48zo2/%E5%9B%BE%E7%89%87.png
  [23]: http://static.zybuluo.com/whunfand/nn5k5yhna31edvhq8b9k5hsu/%E5%9B%BE%E7%89%87.png
  [24]: http://static.zybuluo.com/whunfand/1sp7enjbk794pdxc5zeswyje/%E5%9B%BE%E7%89%87.png
  [25]: http://static.zybuluo.com/whunfand/y9p0hqukbyjq9u0rb4yn4ug6/QQ%E6%88%AA%E5%9B%BE20170724180118.png