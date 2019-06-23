package com.task.util



import com.alibaba.fastjson.{JSON, JSONObject}
import com.typesafe.scalalogging.LazyLogging
import org.apache.http.client.methods.{HttpGet, HttpPost}
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.HttpClients
import org.apache.http.util.EntityUtils
import scala.collection.JavaConverters._
/**
  * @author limeng
  * @date 2019/6/6 16:50
  * @version 1.0
  */
object HttpClientUtil extends LazyLogging{
  def doGet(url: String, header: String = null): String = {
    val httpClient = HttpClients.createDefault()    // 创建 client 实例
    val get = new HttpGet(url)    // 创建 get 实例

    if (header != null) {   // 设置 header
      val json = JSON.parseObject(header)
      json.keySet().toArray.map(_.toString).foreach(key => get.setHeader(key, json.getString(key)))
    }else{
      get.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:39.0) Gecko/20100101 Firefox/39.0")
    }

    val response = httpClient.execute(get)    // 发送请求
    EntityUtils.toString(response.getEntity)    // 获取返回结果
  }
  def doPost(url: String, params: Map[String, String]): String ={

//    val paramsMap: java.util.Map[String, String] = params.asJava
//    val result: String = JSON.toJSONString(paramsMap)
//    this.doPost(url,result,null)
    null
  }

  def doPost(url: String, params: String = null, header: String = null): String ={
    val httpClient = HttpClients.createDefault()    // 创建 client 实例
    val post = new HttpPost(url)    // 创建 post 实例

    // 设置 header
    if (header != null) {
      val json = JSON.parseObject(header)
      json.keySet().toArray.map(_.toString).foreach(key => post.setHeader(key, json.getString(key)))
    }else{
      post.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:39.0) Gecko/20100101 Firefox/39.0")
    }

    if (params != null) {
      post.setEntity(new StringEntity(params, "UTF-8"))
    }

    val response = httpClient.execute(post)    // 创建 client 实例
    EntityUtils.toString(response.getEntity, "UTF-8")   // 获取返回结果
  }

}
