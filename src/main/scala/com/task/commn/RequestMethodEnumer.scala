package com.task.commn

/**
  * @Author: limeng
  * @Date: 2019/6/8 15:35
  */
object RequestMethodEnumer extends Enumeration{
  type  RequestMethod =Value
  val GET = Value("GET")
  val POST =Value("POST")

  def isPost(value:String):Boolean = value.equalsIgnoreCase(POST.toString)
  def isGet(value:String):Boolean = value.equalsIgnoreCase(GET.toString)

  def isMethod(value:String):Boolean={
    if(isPost(value)) true
    else false
  }

  def getMethod(value:String):String={
    if(isPost(value)) POST.toString
    else GET.toString
  }
}
