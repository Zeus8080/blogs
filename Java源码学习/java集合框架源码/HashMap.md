# java.util.HashMap源码学习

## 1.层次结构

先看看继承关系

![ArrayList的继承关系](/图片/jdk1.8源码系列/HashMap.png)

HashMap实现Cloneable接口,可以被克隆

HashMap实现Serializable接口,可以被序列化

HashMap继承AbstractMap,实现map接口,具有map的所有功能

存储结构

![ArrayList的继承关系](/图片/jdk1.8源码系列/HashMap-structure.png)

java中,HashMap采用了(数组+链表+红黑树)的复杂结构,数组的一个元素,又叫做桶

添加元素时,会根据hash值算出元素的位置,如果该位置没有元素,则直接把元素放到这里,如果有元素了,则把元素以链表的形式放在链表的尾部

当一个链表元素个数达到一定的数量,就链表转化为红黑树,从而提高查询效率

## 2.分析



