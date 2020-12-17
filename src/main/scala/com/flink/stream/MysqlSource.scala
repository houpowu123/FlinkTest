package com.flink.stream
import java.sql.{Connection, DriverManager, PreparedStatement}

import org.apache.flink.configuration.Configuration
import org.apache.flink.streaming.api.functions.source.{RichSourceFunction, SourceFunction}
import org.apache.flink.streaming.api.scala._
object MysqlSource {
  def main(args: Array[String]): Unit = {
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    val source: DataStream[Student] = env.addSource(new SQL_source)
    source.print()
    env.execute()
  }
}

class SQL_source extends RichSourceFunction[Student] {
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
    val sql = "select stuid , stuname , stuaddr , stusex from Student"
    //执行
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

  override def run(ctx: SourceFunction.SourceContext[Student]): Unit = {
    val queryRequest = ps.executeQuery()
    while (queryRequest.next()) {
      val stuid = queryRequest.getInt("stuid")
      val stuname = queryRequest.getString("stuname")
      val stuaddr = queryRequest.getString("stuaddr")
      val stusex = queryRequest.getString("stusex")
      val stu = Student(stuid, stuname, stuaddr, stusex)
      ctx.collect(stu)
    }
  }
    override def cancel(): Unit ={}
}
