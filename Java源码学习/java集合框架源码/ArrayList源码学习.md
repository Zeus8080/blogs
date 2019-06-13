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
        //返回的类型不一定是Object[]类型,这是一个bug,具体分析见下面
        if (elementData.getClass() != Object[].class)
            elementData = Arrays.copyOf(elementData, size, Object[].class);
    } else {
        // replace with empty array.
        this.elementData = EMPTY_ELEMENTDATA;
    }
}
```

##### bug分析

**具体先看下Arrays.asList(x).toArray()的源码** 

```java
//由于启用的是范型,所以构造的结果也是原来类型的结果
//例如:Arrays.asList("1","2");T就是String类型
//所以如果new ArrayList( Arrays.asList("1","2") )方式构造,c.toArray就可能不是Object[]
public static <T> List<T> asList(T... a) {
    return new ArrayList<>(a);
}

//这里的ArrayList 是Arrays里面的内部类
private final E[] a;

ArrayList(E[] array) {
    a = Objects.requireNonNull(array);
}
//具体分析,可以参考: https://www.cnblogs.com/zhizhizhiyuan/p/3662371.html
```

### 2.3 Method

- boolean add(E e);

  ```java
  //add方法
  public boolean add(E e) {
      //判断最大位置后面一位有没有空间，有的话+1，没有的话扩容
      ensureCapacityInternal(size + 1);  // Increments modCount!!
      //将新元素，放到最大元素后一位
      elementData[size++] = e;
      return true;
  }
  
  private void ensureCapacityInternal(int minCapacity) {
      ensureExplicitCapacity(calculateCapacity(elementData, minCapacity));
  }
  //这个方法只是为了判断是不是初始化后第一次调用
  private static int calculateCapacity(Object[] elementData, int minCapacity) {
     	//传入实际存储的数组，如果elementData是空的话（也就是我们调用ArrayList（）构造方法后第一次add）
      if (elementData == DEFAULTCAPACITY_EMPTY_ELEMENTDATA) {
          //返回默认初始容量（这里如果第一次的话，minCapacity= size+1=1）
          //也就是说这里才初始化，分配默认容量大小10
          return Math.max(DEFAULT_CAPACITY, minCapacity);
      }
      //不是空的话，返回当前最小的容量minCapacity = size+1
      return minCapacity;
  }
  //判断容量是否超出初始化的容量
  private void ensureExplicitCapacity(int minCapacity) {
      modCount++;
  
      //如果需要的最小容量比实际存储的最大容量还要大的话，证明容量不够，需要扩容
      if (minCapacity - elementData.length > 0)
          grow(minCapacity);
  }
  
  //扩容操作
  private void grow(int minCapacity) {
      // 旧容量
      int oldCapacity = elementData.length;
      //新容量 = 旧容量+旧容量/2，也就是扩容增加1/2
      int newCapacity = oldCapacity + (oldCapacity >> 1);
      //扩容后还比需求的最小容量小，新容量就是需求的最小容量
      if (newCapacity - minCapacity < 0)
          newCapacity = minCapacity;
      //扩容后容量比最大MAX_ARRAY_SIZE还大，那么新容量就是MAX_ARRAY_SIZE
      //MAX_ARRAY_SIZE =Integer.MAX_VALUE - 8;
      if (newCapacity - MAX_ARRAY_SIZE > 0)
          newCapacity = hugeCapacity(minCapacity);
      // minCapacity is usually close to size, so this is a win:
      elementData = Arrays.copyOf(elementData, newCapacity);
  }
  ```

  