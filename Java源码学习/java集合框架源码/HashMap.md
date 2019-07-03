# java.util.HashMap源码学习

## 1.层次结构

先看看继承关系

![ArrayList的继承关系](/图片/jdk1.8源码系列/HashMap.png)

HashMap实现Cloneable接口,可以被克隆

HashMap实现Serializable接口,可以被序列化

HashMap继承AbstractMap,实现map接口,具有map的所有功能

存储结构

![ArrayList的继承关系](/图片/jdk1.8源码系列/HashMap-structure.png)

java中,HashMap采用了(数组+链表+红黑树)的复杂结构,数组的一个元素,又叫做桶)

添加元素时,会根据hash值算出元素的位置,如果该位置没有元素,则直接把元素放到这里,如果有元素了,则把元素以链表的形式放在链表的尾部

当一个链表元素个数达到一定的数量,就链表转化为红黑树,从而提高查询效率

## 2.分析

### 2.1 属性

```java
//默认初始化容量,默认为16
static final int DEFAULT_INITIAL_CAPACITY = 1 << 4; // aka 16

//最大容量,为2的30次方
static final int MAXIMUM_CAPACITY = 1 << 30;

//默认的装载因子
static final float DEFAULT_LOAD_FACTOR = 0.75f;

//当一个桶的元素个数大于等于8的时候进行树化
static final int TREEIFY_THRESHOLD = 8;

//当一个桶中的元素个数小于等于6的时候把树转化成链表
static final int UNTREEIFY_THRESHOLD = 6;

//当桶的个数达到64的时候进行树化
static final int MIN_TREEIFY_CAPACITY = 64;

//桶数组
transient Node<K,V>[] table;

/**
  * Holds cached entrySet(). Note that AbstractMap fields are used
  * for keySet() and values().
  */
//保存entrySet()的缓存
transient Set<Map.Entry<K,V>> entrySet;

//元素的数量
transient int size;

//修改次数，用于在迭代的时候执行快速失败策略
transient int modCount;

//当桶的使用数量达到多少时进行扩容，threshold = capacity * loadFactor
int threshold;

//装载因子
final float loadFactor;
```

