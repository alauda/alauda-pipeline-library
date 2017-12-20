## 使用
配置在 alauda plugin 上完成

#### alaudaBuild
trigger alauda build
- params:
  - name: name of build config
  - commitId: `optional`, commit id that you want to build 
  - branch: `optional`, branch that you want to build
- return:
  - buildId

```
alaudaBuild{
  name = "this is build config name",
  commitId = "xxxxxx", /*optional*/
  branch = "master"
}
```

#### alaudaWaitBuild
wait until build is completed
-  params:
  - buildId
  - timeout: `int`, `optional`, unit is seconds, default value is 1200

```
alaudaWaitBuild{
  buildId = "xxxx",
  timeout = 1200
}

```

#### alaudaNotify
send alauda notification
- params:
  - name: notification configuration name

```
alaudaNotify{
  name = "alauda-notify"
}
```
