#小明智能导购机器人

## 目录结构

```
.
├── android
│   └── app
│       ├── libs
│       └── src\main
├── corpus
├── src
│   └── main
│       ├── java
│       │   ├── bitoflife\chatterbean
│       │   └── com\chatrobot
│       ├── resources
├── web
├── ChatRobot.sql
├── pom.xml
└── README.md
```

### android\app

* \libs，安卓端所用的开源包
* \src\main，安卓端程序源代码

### corpus

存放语料库文件

### src\main

* \java\bitoflife\chatterbean，AIML实现源代码
* \java\com\chatrobot，后台主要功能源代码
* \resources，存放资源文件和配置文件

### web

存放web端程序代码

### ChatRobot.sql

所用的数据库脚本文件

### pom.xml

Maven配置文件