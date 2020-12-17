package com.flink.stream

import java.sql.{Connection, DriverManager, PreparedStatement}

import org.apache.flink.configuration.Configuration
import org.apache.flink.streaming.api.functions.sink.{RichSinkFunction, SinkFunction}
import org.apache.flink.streaming.api.functions.source.SourceFunction.SourceContext
import org.apache.flink.streaming.api.scala._

object MysqlSink {
  def main(args: Array[String]): Unit = {
    //1.创建流执行环境
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    //2.准备数据
    val dataStream: DataStream[Student] = env.fromElements(
      Student(8, "xiaoming", "beijing biejing", "female")
      // Student(6, "daming", "tainjing tianjin", "male "),
      // Student(7, "daqiang ", "shanghai shanghai", "female")
    )
    //3.将数据写入到自定义的sink 中（这里是mysql）
    dataStream.addSink(new StudentSinkToMysql)
    //4.触发流执行
    env.execute()
  }
}


case class Student(stuid:Int, stuname:String, stuaddr:String, stusex:String)

class StudentSinkToMysql extends RichSinkFunction[Student]{
  private var connection: Connection = null
  private var ps: PreparedStatement = null
  override def open(parameters: Configuration): Unit = {
    val driver = "com.mysql.jdbc.Driver"
    val url = "jdbc:mysql://bigdata003:3306/test"
    val username = "root"
    val password = "123456"
    //1:加载驱动
    Class.forName(driver)
    //2：创建连接
    connection = DriverManager.getConnection(url, username, password)
    val sql = "insert into Student(stuid , stuname , stuaddr , stusex) values(?,?,?,?);"
    //3:获得执行语句
    ps = connection.prepareStatement(sql)
  }

  override def close(): Unit = {
    if (connection != null) {
      connection.close()
    }
    if (ps != null) {
      ps.close()
    }
  }
  //每条数据都会调用这个方法
  override def invoke(value: Student, context: SinkFunction.Context[_]): Unit = {
    val name = value.stuname
    val sex = value.stusex
    ps.setString(1, name)
    ps.setString(2, sex)
    ps.executeUpdate()
  }

}
