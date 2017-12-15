# alauda-pipeline-library

This git repository contains a library of reusable [Jenkins Pipeline](https://jenkins.io/doc/book/pipeline/) steps and functions that can be used in your `Jenkinsfile` to help improve your Continuous Delivery pipeline.

## How to use this library
- Jenkins configure  
GO to Jenkins Server `manage/configure/Global Pipeline Libraries` add a Library
```
Name: alauda-pipeline
Default version: master
Allow default version to be overridden: [√]
Include @Library changes in job recent changes: [√]
Modern SCM: [√]
Git/Project Repository: https://github.com/alauda/alauda-pipeline-library.git
```
- Using in jenkins file
```
@Library("alauda-pipeline") _
pipeline{
    agent any
    environment{
        ENDPOINT = "http://api.alauda.cn your api endpoint"
        NAMESPACE = "your namespace"
        TOKEN = "your api token"

        APP_NAME = "app that you want create"
        SPACE_NAME = "space name that you will use"
        REGION = "region name that you will use"
    }
    
    stages{
        stage('Deploy') {
            steps{
                script{
                    // config alauda
                    alauda.setup("${ENDPOINT}", "${NAMESPACE}", "${TOKEN}")
                    // deploy ${APP_NAME} on ${REGION}
                    alauda.deployApp("${REGION}", "${SPACE_NAME}", "${APP_NAME}")
                    // wait ${APP_NAME} until it deploy successfull or failure
                    // if it fails ,will abort current build
                    alauda.waitDeployApp("${APP_NAME}","${SPACE_NAME}")
                }
            }
        }
    }
}
```

### Functions from the Jenkins global library
#### alauda.setup
config alauda information
- params:
  - endpoint: alauda api endpoint
  - namespace: alauda namespace
  - token: alauda api token
  - verbose: `bool`, `optional` show more build message, default is false

```
script{
  alauda.setup(
    "http://api.alauda.cn",
    "alauda",
    "the token"
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
  def services = alauda.services("k8s","global")
  echo "${services}"
}
```

#### alauda.stopService
stop service on alauda
- params:
  - name: service name
  - spaceName: space name
```
script{
  alauda.stopService(name, spaceName)
}
```

#### alauda.startService
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


#### alauda.deleteService
delete service on alauda
- params:
  - name: service name
  - spaceName: space name
```
script{
  alauda.deleteService(name, spaceName)
}
```

#### alauda.updateService
update service on alauda
- params:
  - serviceFullName: aplicationName/serviceName , eg: global-component/jakiro
  - spaceName: space name
  - imgTag: image tag, will update service to this tag
  - envVars: `map`,`optional`, environment variables, eg: [ name1 :"value1", name2:"value2"]
```
script{
  alauda.updateService(
    "app-create-by-jenkins/seq1", "global", "v2", 
    [name1:"jenkins1", name2:"jenkins2"]
  )
}
```

### alauda.waitUpdateService
wait until deploy service successfull or failure.
- params: 
  - serviceFullName: applicationName/serviceName eg: app-created-by-jenkins/service1
  - spaceName: space name
  - timeoutVal: `int`,`optional`, default is 600. unit will be seconds.
```
script{
   alauda.waitUpdateService("app-created-by-jenkins","global")
}
```

#### alauda.deployApp
create a new application on alauda according your alauda.app.yml.  
you should add `alauda.app.yml` to your repository.  
- params:
  - region: region that you want to deploy
  - spaceName: space name that you want to use
  - appName: application name that you want to deploy
  - tags: `map`,`optional`, image versions that you want to update defined in alauda.app.yml. the key should be service name ,the value should be image tag.
  - ymlFile: `optional`, path of your application yaml, releative to you repository. default is `alauda.app.yml`

```
script{
  alauda.deployApp("int", "global", "app-created-by-jenkins", [service1:"${params['IMG_TAG']}"])
}

```

### alauda.waitDeployApp
wait until deploy application successfull or failure.
- params: 
  - appName: application name
  - spaceName: space name
  - timeoutVal: `int`,`optional`, default is 600. unit will be seconds.
```
script{
   alauda.waitDeployApp("app-created-by-jenkins","global")
}
```

### alauda.deleteApp
delete application on alauda
- params:
  - appName: application name
  - spaceName: space name
```
script{
  alauda.deleteApp(name, spaceName)
}





