@Grapes(
    @Grab(group='org.yaml', module='snakeyaml', version='1.19')
)

package io.alauda

import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import org.yaml.snakeyaml.Yaml
import com.cloudbees.groovy.cps.NonCPS

def setup(endpoint, namespace, token, verbose=false){
  this.endpoint = endpoint
  this.namespace = namespace
  this.token = token
  this.verbose = verbose
}

def info(){
  return "${endpoint} ${token} ${namespace}".toString()
}

def buildUrl(path){
  return "${endpoint}/v1/${path}"
}

def sendRequest(method, path, data=null, codes="100:399"){
  def headers = [[name:'Authorization', value:"Token ${token}"]]
  def reqBody = ""
  if(data!=null){
    reqBody = new JsonOutput().toJson(data)
  }
  def response = httpRequest(
    httpMode:method, url: buildUrl(path), 
    requestBody:reqBody, 
    customHeaders: headers,
    validResponseCodes: codes,
    contentType: "APPLICATION_JSON",
    quiet: !this.verbose
  )
  if(response.content == null || response.content == ""){
    return [:]
  }
  def jsonSlurper = new JsonSlurper()
  def json = jsonSlurper.parseText(response.content)
  return json
}

def httpPost(path, data){
  return sendRequest("POST", path, data)
}

def httpGet(path){
  return sendRequest("GET", path)
}

def httpPut(path, data=null){
  return sendRequest("PUT", path, data)
}

def httpDelete(path){
  return sendRequest("DELETE", path, null, "100:399,404,410")
}

// ------ application

// httpGet application deploy yaml
def getAppDeployYamlByTempl(uuid, imgTagMap){
  path = "application-templates/${uuid}"
  def response = httpRequest(httpMode:"GET", url: buildUrl(path), customHeaders:[[name:'Authorization', value:"Token ${token}"]])
  def jsonSlurper = new JsonSlurper()
  def appTempl = jsonSlurper.parseText(response.content)
  new Utils().updateImgVersionInAppYml(appTempl, imgTagMap)
  def yaml = new Yaml()
  def content = yaml.dump(appTempl)
  return content
}

def getApp(appName, spaceName){
  path = "applications/${namespace}/${appName}/?space_name=${spaceName}&meta=true"
  return httpGet(path)
}


def startApp(appName, spaceName){
  path = "applications/${namespace}/${appName}/start?space_name=${spaceName}"
  return httpPut(path)
}

def stopApp(appName, spaceName){
  path = "applications/${namespace}/${appName}/stop?space_name=${spaceName}"
  return httpPut(path)
}

def deleteApp(appName, spaceName){
  path = "applications/${namespace}/${appName}?space_name=${spaceName}"
  return httpDelete(path)
}

def isAppRunning(app){
  return app['current_status'].equals("Running")
}

def isAppCreating(app){
  return app['current_status'].equals("Creating")
}

def isAppCreateError(app){
  return app['current_status'].equals('CreateError')
}

// ----- service

// list services
def services(region, spaceName){
  path = "applications/${this.namespace}/?region=${region}?spaceName=${spaceName}"
  return httpGet(path)
}

def parseServiceFullName(serviceFullName){
  def arr = serviceFullName.split('/')
  def service = ""
  def app = ""
  if (arr.size() == 2) {
    app = arr[0]
    service = arr[1]
  }else{
    service = arr[0]
  }

  return [app, service]
}

def stopService(serviceFullName, spaceName){
  def (app, service) = parseServiceFullName(serviceFullName)
  def path = "services/${namespace}/${service}/stop?application=${app}&space_name=${spaceName}"
  echo "stoping service ${namespace}/${serviceFullName}"
  return httpPut(path)
}

def deleteService(serviceFullName, spaceName){
  def (app, service) = parseServiceFullName(serviceFullName)
  def path = "services/${namespace}/${service}?application=${app}&space_name=${spaceName}"
  echo "deleting service ${namespace}/${serviceFullName}"
  return httpDelete(path)
}

def startService(serviceFullName, spaceName){
  def (app, service) = parseServiceFullName(serviceFullName)
  def path = "services/${namespace}/${service}/start?application=${app}&space_name=${spaceName}"
  echo "staring service ${namespace}/${serviceFullName}"
  return httpPut(path)
}

def updateService(serviceFullName, spaceName, imgTag, envVars){
  def (app, service) = parseServiceFullName(serviceFullName)
  path = "services/${namespace}/${service}?application=${app}&space_name=${spaceName}"

  def json = [
    image_tag: imgTag,
    instance_envvars: envVars,
  ]

  return this.httpPut(path, json)
}

def getService(serviceFullName, spaceName){
  def (app, service) = parseServiceFullName(serviceFullName)
  def path = "services/${namespace}/${service}?application=${app}&space_name=${spaceName}"

  return this.httpGet(path)
}

def createApp(yamlFileName, content, spaceName, appName, region){
 node(){
   writeFile file: "${yamlFileName}", text: content
   echo "begin to deploy..."
   withEnv(["NAMESPACE=${this.namespace}", "API_ENDPOINT=${this.endpoint}", "API_TOKEN=${this.token}", "SPACE_NAME=${spaceName}", "APP_NAME=${appName}","REGION=${region}", "SERVICES_FILE=${yamlFileName}"]){
   sh '''
curl -s -S --request POST \
 --url ${API_ENDPOINT}/v1/applications/${NAMESPACE}/ \
 --header "authorization: Token ${API_TOKEN}" \
 --header 'content-type: multipart/form-data; boundary=----WebKitFormBoundary7MA4YWxkTrZu0gW' \
 --form region=${REGION} \
 --form services=@${SERVICES_FILE} \
 --form space_name=${SPACE_NAME} \
 --form app_name=${APP_NAME} | grep error && echo "deploy failed" && exit 1 || echo "deploy succeed" && exit 0
'''
   }
 }
}

def isServiceRunning(service){
  return service["current_status"].equals("Running")
}

def isServiceUpdating(service){
  return service["current_status"].equals("Updating")
}
def isServiceStartError(service){
  return service["current_status"].equals("StartError")
}

def waitDeployApp(appName, spaceName, timeout=300){
  echo "will wait"
  def max = timeout/2

  def counter = 0

  echo "counter=${counter} , max=${max}"
  while(counter<=max){
    echo "in while...."
    counter++
    def app = getApp(appName, spaceName)
    echo "httpGet ok.."
    echo "---> ${app}"
    if(isAppCreating(app)){
      echo "will sleep 2...."
      sleep 2
      echo "sleep 2 ok, will continue"
      continue
    }

    if(isAppRunning(app)){
      echo "${appName} is running..."
      break
    }
    echo "status is not expected."
    // TODO: a better way to tell the job is failure?
    throw Exception("${appName} status:${app['current_status']} is not expected")
  }

  echo "test over.."
  sleep 2
}