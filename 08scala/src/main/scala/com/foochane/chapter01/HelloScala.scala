package com.foochane.chapter01

// Scala是完全面向对象的语言，没有静态的语法操作
// .scala ==> .class
// Scala采用object类代替静态操作
object HelloScala {
    // main方法
    def main(args: Array[String]): Unit = {

        println("Hello Scala") // 打印字符串

        HelloScala.print()
    }

    def print():Unit = {
        // 函数体
    }
}
