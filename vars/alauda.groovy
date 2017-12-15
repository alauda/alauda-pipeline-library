#!/usr/bin/env groovy
@Grapes(
    @Grab(group='org.yaml', module='snakeyaml', version='1.19')
)
import groovy.json.JsonSlurper
import io.alauda.AlaudaClient
import io.alauda.Utils
import org.yaml.snakeyaml.Yaml

AlaudaClient client

def setup(endpoint, namespace, token){
  client = new AlaudaClient()
  client.setup(endpoint, namespace, token)
}

def services(region, spaceName){
  return client.services(region, spaceName)
}

def stopService(name, spaceName){
 client.stopService(name, spaceName)
}

def startService(name, spaceName){
 client.startService(name, spaceName)
}

def updateService(serviceFullName, spaceName, imgTag, envVars){
 client.updateService(serviceFullName, spaceName, imgTag, envVars)
}

def deleteService(serviceFullName, spaceName){
  client.deleteService(serviceFullName, spaceName)
}

def deployAppByTempl(region, spaceName, appName, appTemplUUID, tags){
  def content = client.getAppDeployYamlByTempl(appTemplUUID, tags)
  client.createApp("${appTemplUUID}.yml", content, spaceName, appName, region)
}

def _getAppYmlContent(ymlFile, tags){

  try{
    content = readFile(ymlFile)
  }catch(Exception ex){
    echo "Read file ${ymlFile} error"
    throw ex
  }

  def ymlMap = [:]
  def yaml = new Yaml()

  try{
    ymlMap = yaml.load(content)
  }catch(Exception ex){
    echo "${ymlFile} content is  not valid yaml, please check yaml format"
    throw ex
  }

  new Utils().updateImgVersionInAppYml(ymlMap, tags)
  
  def content = yaml.dump(ymlMap)
  return content
}

def deployApp(region, spaceName, appName, tags, ymlFile="alauda.app.yml"){
  def content = _getAppYmlContent(ymlFile, tags)
  def updatedYmlFileName = "updated-by-alauda---${ymlFile}"
  client.createApp(updatedYmlFileName, content, spaceName, appName, region)
}

def waitDeployApp(appName, spaceName, timeoutVal=600){
  timeout(time:timeoutVal, unit:"SECONDS"){
    waitUntil(){
      def app = client.getApp(appName, spaceName)
      if(client.isAppCreateError(app)){
        return error("${appName} deploy error ! please check it on ${client.endpoint}")
      }
      echo "${appName} is ${app['current_status']}"
      return client.isAppRunning(app)
    }
  }
}

def waitUpdateService(serviceFullName, spaceName, timeoutVal=600){
  timeout(time:timeoutVal, unit:"SECONDS"){
    waitUntil(){
      def service = client.getService(serviceFullName, spaceName)
      if(client.isServiceStartError(service)){
        return error("${serviceFullName} created error ! please check it on ${client.endpoint}")
      }
      echo "${serviceFullName} is ${service['current_status']}"
      return client.isServiceRunning(service)
    }
  }
}

def waitDeployApp2(appName, spaceName, timeout=300){
  client.waitDeployApp(appName, spaceName, timeout)
}

def waitUpdateService2(serviceFullName, spaceName, timeout=300){
  def max = timeout/2

  def counter = 0
  while(counter<=max){
    counter++

    def service = client.getService(serviceFullName, spaceName)
    if(client.isServiceUpdating(service)){
      sleep(2)
      echo "sleep 2 ok, will continue"
      continue
    }

    if(client.isServiceRunning()){
      echo "${serviceFullName} is running..."
      break
    }

    // TODO: a better way to tell the job is failure?
    throw Exception("${serviceFullName} status:${service['current_status']} is not expected")
  }

  if(counter>max){
    throw Exception("Update service ${serviceFullName} timeout")
  }

  echo "Update service ${serviceFullName} successed"
}

def deleteApp(appName, spaceName){
  client.deleteApp(appName, spaceName)
}