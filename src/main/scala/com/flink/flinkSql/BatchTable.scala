package com.flink.flinkSql

import org.apache.flink.api.scala._
import org.apache.flink.core.fs.FileSystem.WriteMode
import org.apache.flink.table.api.Table
import org.apache.flink.table.api.scala._


object BatchTable {
  def main(args: Array[String]): Unit = {
    val fbEnv = ExecutionEnvironment.getExecutionEnvironment
    val fbTableEnv = BatchTableEnvironment.create(fbEnv)
    val student = fbEnv.readTextFile("D:\\test\\sql_students.txt")
    val score = fbEnv.readTextFile("D:\\test\\sql_scores.txt")
    //id name class
    val stu: DataSet[(Int, String, String)] = student.map(line => {
      val x = line.split(",")
      (x(0).toInt, x(1), x(2))
    })
    //id chinese math english
    val sco: DataSet[(Int, Int, Int, Int)] = score.map(line => {
      val x = line.split(",")
      (x(0).toInt, x(1).toInt, x(2).toInt, x(3).toInt)
    })
    val value: DataSet[(Int, String, String, Int, Int, Int)] = stu.join(sco).where(0).equalTo(0) {
      (stu, sco) => (stu._1, stu._2, stu._3, sco._2, sco._3, sco._4)
    }
    //注册为视图 ,
    //      "id, name, classname, chinese, math, english"
    //'myLong, 'myString
    fbTableEnv.createTemporaryView("t_student_score", value
    ,'id, 'name, 'classname, 'chinese, 'math, 'english)
    //求各班级每个学科的平均分、三科总分平均分
    val resultTable: Table = fbTableEnv.sqlQuery(
      """
        |select
        |classname,
        |avg(chinese) as chinese,
        |avg(math) as math,
        |avg(english) as english,
        |avg(chinese+math+english) as total
        |from t_student_score
        |group by classname
        |order by chinese
        |""".stripMargin)
    val fbTable: DataSet[Info] = fbTableEnv.toDataSet[Info](resultTable)
    fbTable.writeAsText("D:\\test\\result.txt", WriteMode.OVERWRITE).setParallelism(1)
    fbEnv.execute("Flinksql")
  }
}
case class Info (classname:String,
                chinese:Int,
                 math:Int,
                 english:Int,
                 total:Int
                )
