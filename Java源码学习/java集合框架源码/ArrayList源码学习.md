# java.util.ArrayList源码分析

## 1.层次结构

首先，我们先来看看arraylist的继承关系，了解下具体实现或者继承了那些类，具体如图：
![ArrayList的继承关系](/图片/jdk1.8源码系列/arraylist.png)

**List**接口,提供基础的增加,删除,遍历等操作

**RandomAccess**接口,是一个标记接口,实现这个接口使用for循环访问数据,比使用iterator访问数据来的快

**Cloneable**接口,可以被克隆

**Serializable**接口,可以被序列化

## 2.分析
### 2.1 ArrayList的属性

```java
//默认初始化的容量
private static final int DEFAULT_CAPACITY = 10;
//空数组对象,如果传入的容量为0的时候使用
private static final Object[] EMPTY_ELEMENTDATA = {};
//空数组,传传入容量时使用，添加第一个元素的时候会重新初始为默认容量大小
private static final Object[] DEFAULTCAPACITY_EMPTY_ELEMENTDATA = {};
//数组缓存，arrayList实际存储数据的地方 transient关键字,让被修饰的成员不被序列化
transient Object[] elementData;
//集合实际大小
private int size;
```
### 2.2 Constuctor

```java
/**
 * 带容量的构造器 initialCapacity指的初始化容量的大小
 */
public ArrayList(int initialCapacity) {
    if (initialCapacity > 0) {
        //如果传入初始化容量＞0,新建一个数组存储元素
        this.elementData = new Object[initialCapacity];
    } else if (initialCapacity == 0) {
        //如果传入初始化容量＝0,使用空数组EMPTY_ELEMENTDATA
        this.elementData = EMPTY_ELEMENTDATA;
    } else {
        //如果传入初始化容量＜0,抛出异常
        throw new IllegalArgumentException("Illegal Capacity: "+initialCapacity);
    }
}

/**
  * 我们最常用的构造器，注意此时DEFAULTCAPACITY_EMPTY_ELEMENTDATA的值为空，在jdk8中，初始容量默认是0，而不是10
 */
public ArrayList() {
    //如果没有传入初始化容量,则使用空数组DEFAULTCAPACITY_EMPTY_ELEMENTDATA
    //使用这个数组在添加第一个元素的时候会扩容到默认大小10
    this.elementData = DEFAULTCAPACITY_EMPTY_ELEMENTDATA;
}

/**
 * 带参数的构造方式
 */
public ArrayList(Collection<? extends E> c) {
    elementData = c.toArray();
    if ((size = elementData.length) != 0) {
        // c.toArray might (incorrectly) not return Object[] (see 6260652)
        if (elementData.getClass() != Object[].class)
            elementData = Arrays.copyOf(elementData, size, Object[].class);
    } else {
        // replace with empty array.
        this.elementData = EMPTY_ELEMENTDATA;
    }
}
```


