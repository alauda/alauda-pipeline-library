# alauda-pipeline-library

This git repository contains a library of reusable [Jenkins Pipeline](https://jenkins.io/doc/book/pipeline/) steps and functions that can be used in your `Jenkinsfile` to help improve your Continuous Delivery pipeline.

## How to use this library

进入 到 Jenkins `系统管理`

### Functions from the Jenkins global library
#### alauda.setup
config alauda information
- params:
  - endpoint: alauda api endpoint
  - namespace: alauda namespace
  - token: alauda api token

```
script{
  alauda.setup(
    "http://api.alauda.cn",
    "alauda",
    "xxxxxxxxxxxx", "http://innerapi.alauda.cn"
  )
}
```
#### alauda.services
list all services
- params:
  - region: region name
  - spaceName: space name

```
script{
  alauda.services("k8s","global")
}
```

#### stop service
stop service on alauda
- params:
  - name: service name
  - spaceName: space name
```
script{
  alauda.stopService(name, spaceName)
}
```

#### start service
start service on alauda
start service on alauda
- params:
  - name: service name
  - spaceName: space name
```
script{
  alauda.startService(name, spaceName)
}
```


#### delete service
delete service on alauda
- params:
  - name: service name
  - spaceName: space name
```
script{
  alauda.deleteService(name, spaceName)
}
```

#### update service
update service on alauda
- params:
  - serviceFullName: aplicationName/serviceName
  - spaceName: space name
  - imgTag: image tag
  - envVars: [ name1 :"value1", name2:"value2"]
```
script{
  alauda.updateService(
    "app-create-by-jenkins/seq1", "global", "helloworld2", 
    [name1:"jenkins1", name2:"jenkins2"]
  )
}
```

#### deploy app by alauda.app.yml



### delete app

delete application on alauda
- params:
  - appName: application name
  - spaceName: space name
```
script{
  alauda.deleteApp(name, spaceName)
}





