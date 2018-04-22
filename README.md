# alauda-pipeline-library

This git repository contains a library of reusable [Jenkins Pipeline](https://jenkins.io/doc/book/pipeline/) steps and functions that can be used in your `Jenkinsfile` to help improve your Continuous Delivery pipeline.

## How to use this library
- Jenkins configure  
GO to Jenkins Server `manage/configure/Global Pipeline Libraries` add a Library
```
Name: alaudaee-pipeline
Default version: master
Allow default version to be overridden: [√]
Include @Library changes in job recent changes: [√]
Modern SCM: [√]
Git/Project Repository: https://github.com/alauda/alauda-pipeline-library.git
```
- Using in jenkins file
```
@Library("alaudaee-pipeline") _
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
                    alaudaEE.setup("${ENDPOINT}", "${NAMESPACE}", "${TOKEN}")
                    // deploy ${APP_NAME} on ${REGION}
                    alaudaEE.deployApp("${REGION}", "${SPACE_NAME}", "${APP_NAME}")
                    // wait ${APP_NAME} until it deploy successfull or failure
                    // if it fails ,will abort current build
                    alaudaEE.waitDeployApp("${APP_NAME}","${SPACE_NAME}")
                }
            }
        }
    }
}
```

### Functions from the Jenkins global library
#### alaudaEE.setup
config alauda information
- params:
  - endpoint: alauda api endpoint
  - namespace: alauda namespace
  - token: alauda api token
  - verbose: `bool`, `optional` show more build message, default is false

```
script{
  alaudaEE.setup(
    "http://api.alauda.cn",
    "alauda",
    "the token"
  )
}
```
#### alaudaEE.services
list all services
- params:
  - region: region name
  - spaceName: space name
```
script{
  def services = alaudaEE.services("k8s","global")
  echo "${services}"
}
```

#### alaudaEE.stopService
stop service on alauda
- params:
  - name: service name
  - spaceName: space name
```
script{
  alaudaEE.stopService(name, spaceName)
}
```

#### alaudaEE.startService
start service on alauda
start service on alauda
- params:
  - name: service name
  - spaceName: space name
```
script{
  alaudaEE.startService(name, spaceName)
}
```


#### alaudaEE.deleteService
delete service on alauda
- params:
  - name: service name
  - spaceName: space name
```
script{
  alaudaEE.deleteService(name, spaceName)
}
```

#### alaudaEE.updateService
update service on alauda
- params:
  - serviceFullName: aplicationName/serviceName , eg: global-component/jakiro
  - spaceName: space name
  - imgTag: image tag, will update service to this tag
  - envVars: `map`,`optional`, environment variables, eg: [ name1 :"value1", name2:"value2"]
```
script{
  alaudaEE.updateService(
    "app-create-by-jenkins/seq1", "global", "v2", 
    [name1:"jenkins1", name2:"jenkins2"]
  )
}
```

### alaudaEE.waitUpdateService
wait until deploy service successfull or failure.
- params: 
  - serviceFullName: applicationName/serviceName eg: app-created-by-jenkins/service1
  - spaceName: space name
  - timeoutVal: `int`,`optional`, default is 600. unit will be seconds.
```
script{
   alaudaEE.waitUpdateService("app-created-by-jenkins","global")
}
```

#### alaudaEE.deployApp
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
  alaudaEE.deployApp("int", "global", "app-created-by-jenkins", [service1:"${params['IMG_TAG']}"])
}

```

### alaudaEE.waitDeployApp
wait until deploy application successfull or failure.
- params: 
  - appName: application name
  - spaceName: space name
  - timeoutVal: `int`,`optional`, default is 600. unit will be seconds.
```
script{
   alaudaEE.waitDeployApp("app-created-by-jenkins","global")
}
```

### alaudaEE.deleteApp
delete application on alauda
- params:
  - appName: application name
  - spaceName: space name
```
script{
  alaudaEE.deleteApp(name, spaceName)
}





