package com.flink.stream


import com.alibaba.fastjson.{JSON, JSONObject}
import org.apache.commons.lang3.StringUtils
import redis.clients.jedis.HostAndPort
import okhttp3.{OkHttpClient, Request, Response}
object HttpUtils {
  val SERVICE_SUFFIX_ADDRESS = "/cache/client/%s/cluster/%s.json?clientVersion=0.4.4-SNAPSHOT&clientType=%s"
  private val client = new OkHttpClient()
  /**
   * 获取redis的ip 端口号
   *
   * @param cacheCloudAddress
   * @param cacheType       缓存类型
   * @param appName
   * @param cacheClientType 缓存客户端类型
   * @return
   */
  def getClusterInfo(cacheCloudAddress: String, cacheType: String, appName: String, cacheClientType: String): Option[String] = {
    // 1. 确定 URL, 通过确定 URL, 从而确定参数
    val url = String.format(cacheCloudAddress + SERVICE_SUFFIX_ADDRESS, cacheType, appName, cacheClientType)
    try {
      // 1. 构建Request请求对象
      val request: Request = new Request.Builder() // 使用建造者设计模式，构建对象
        .url(url) // 设置请求URL地址
        .get() // 设置请求方式
        .build()
      // 2. 通过httpClient发送请求
      val response: Response = client.newCall(request).execute()
      // 3. 当请求成功以后，获取响应的内容
      if (response.isSuccessful) {
        val respStr: String = response.body().string()
        Some(respStr)
      } else {
        None
      }
    } catch {
      case e: Exception => e.printStackTrace(); None
    }

  }
  def parseJson(json:String): java.util.HashSet[HostAndPort] = {
      val bean: JSONObject = JSON.parseObject(json)
      val shardInfo = bean.getString("shardInfo")
      val jedisClusterNode =  new java.util.HashSet[HostAndPort]()
      if(StringUtils.isEmpty(shardInfo)){
        println("地址信息获取失败")
        jedisClusterNode
      }else{
        val strings: Array[String] = shardInfo.split(" ")
        for(hostAndPort1 <- strings){
          var hostAndPort = hostAndPort1.split(":")
          jedisClusterNode.add(new HostAndPort(hostAndPort(0), Integer.parseInt(hostAndPort(1))))
        }
        jedisClusterNode
      }
  }
  def getClusterNode(cacheCloudAddress: String, cacheType: String, appName: String, cacheClientType: String): java.util.HashSet[HostAndPort] ={
      val clusterOnfo = getClusterInfo(cacheCloudAddress, cacheType, appName, cacheClientType)
      clusterOnfo match {
        case Some(json) => parseJson(json)
        case None => new java.util.HashSet[HostAndPort]()
      }
  }

  def main(args: Array[String]): Unit = {
    val json = getClusterNode("http://cachecloud01.beta1.fn","redis","bimqtokafka", "Jedis")
    println(json)

  }
}
