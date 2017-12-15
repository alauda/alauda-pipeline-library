@Grapes(
    @Grab(group='org.yaml', module='snakeyaml', version='1.19')
)

package io.alauda

import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import org.yaml.snakeyaml.Yaml

def updateImgVersionInAppYml(ymlMap, imgTagMap){
  imgTagMap.each{ img, tag ->
    if (ymlMap.get(img)!=null){
      def originImg = ymlMap[img]["image"]
      ymlMap[img]["image"] = originImg.split(":")[0] + ":" + tag
    }
  }

  return ymlMap
}