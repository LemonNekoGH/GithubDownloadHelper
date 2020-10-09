# GDH - GithubDownloadHelper
GitHub下载助手的后端，已更换更轻量级的Ktor框架，你会发现源码中只有一个文件，但是它完成了之前Spring版的所有功能

前后端分离，因此你可以自己写前端，但是API文档尚未完成（完 全 没 写）
### 重点 - 如何部署
这个问题问的很好，但是猫猫只说一句话，它是一个jar包而且里面有main方法

所以...直接运行就可以了嘛~
```
java -jar backend.jar
```
那前端呢？猫猫自己写的前端的话用静态服务器都可以，比如Nginx
```
这里并没有例子，去查一下静态网站怎么部署就行了
```

### 惯例 - LICENSES
<a href="LICENSE"><img src="https://img.shields.io/badge/GDH-Apache2.0-blue"/></a>
<a href="https://www.gnu.org/licenses/old-licenses/gpl-2.0.txt"><img src="https://img.shields.io/badge/NekoGit-GPLv2-blue"/></a>
<a href="https://www.gnu.org/licenses/old-licenses/gpl-2.0.txt"><img src="https://img.shields.io/badge/libgit2-GPLv2-blue"/></a>