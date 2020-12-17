package com.flink.test

import org.apache.flink.api.scala._

import scala.collection.mutable

object Join {
  def main(args: Array[String]): Unit = {

    val env = ExecutionEnvironment.getExecutionEnvironment
    val data = new mutable.MutableList[(Int, String, Double)]
    data.+=((1, "yuwen", 90.0))
    data.+=((2, "shuxue", 20.0))
    data.+=((3, "yingyu", 30.0))
    data.+=((4, "wuli", 40.0))
    data.+=((5, "yuwen", 50.0))
    data.+=((6, "wuli", 60.0))
    data.+=((7, "yuwen", 70.0))
    data.+=((8, "yuwen", 20.0))
    val s1 = env.fromCollection(data)
    val data2 = new mutable.MutableList[(Int, String)]
    //学号 ---班级
    data2.+=((1,"class_1"))
    data2.+=((2,"class_1"))
    data2.+=((3,"class_2"))
    data2.+=((4,"class_2"))
    data2.+=((5,"class_3"))
    data2.+=((6,"class_3"))
    data2.+=((7,"class_4"))
    data2.+=((8,"class_1"))
    val s2 = env.fromCollection(data2)
    // todo join
    s1.join(s2).where(0).equalTo(0){
      (s1,s2) => (s2._1, s2._2, s1._2, s1._3)
    }.groupBy(1,2)
      .maxBy(3)
      .print()

    val s3 = env.fromElements("java")
    val s4 = env.fromElements("scala")
    val s5 = env.fromElements("java")
    //todo union
    s3.union(s4).union(s5).print()


  }
}