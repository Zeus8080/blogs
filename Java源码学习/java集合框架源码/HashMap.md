# java.util.HashMap源码学习

## 1.层次结构

先看看继承关系

![ArrayList的继承关系](/图片/jdk1.8源码系列/HashMap.png)

HashMap实现Cloneable接口,可以被克隆

HashMap实现Serializable接口,可以被序列化

HashMap继承AbstractMap,实现map接口,具有map的所有功能



## 2.分析



