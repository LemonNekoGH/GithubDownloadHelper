## Github-Proxy-Java
这是@xiaoxinda大佬的GitHub-Proxy代下载服务的Java实现版本，TA的仓库地址：https://github.com/xiaoxinda/github-proxy

### 开始
将本仓库clone到自己的电脑上：
```shell script
git clone https://github.com/LemonNekoGH/Github-Proxy-Java path/to/your/dir
```
然后导入IntelliJ工程，这个工程中包含了前后端。

#### 前端
由于前端使用了Nodejs，所以IDE会提示你进行npm install，你可以通过下面的命令进入到前端工程文件夹：
```shell script
cd path/to/your/project
cd src/main/resources/frontend
```
然后你就可以进行npm install了，前提是你安装了vue-cli，如果没有安装的话可以通过下面的命令进行安装：
```shell script
npm install -g @vue/cli
```
现在你已经安装了vue-cli，继续操作：
```shell script
npm install
```
没有报错就说明依赖安装完成，现在你可以输入下面的命令开始服务：
```shell script
npm run serve
```
然后打开浏览器，输入localhost:8080，就能看到前端页面了。

#### 后端
后端使用Spring boot，使用gradle作为构建系统，编写语言是kotlin

打开工程时，IDE可能会提示你import gradle project，你只要点击import就行。

gradle会帮你创建一个测试用的Configure，gradle同步完成后，只要点击运行图标就行，Configure名称应该是GithubProxyApplication.