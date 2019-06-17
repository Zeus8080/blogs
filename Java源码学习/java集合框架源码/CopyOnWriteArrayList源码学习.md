# java.util.CopyOnWriteArrayList

## 1.层次结构

![LinkedList的继承关系](/图片/jdk1.8源码系列/CopyOnWriteLArrayList.png)

## 2.分析

### 2.1 数据结构

```java
//数组
private transient volatile Object[] array;
```

### 2.2 CopyOnWriteArrayList属性

```java
/** The lock protecting all mutators */
//用于修改时加锁
final transient ReentrantLock lock = new ReentrantLock();

/** The array, accessed only via getArray/setArray. */
//真正存储元素的地方，只能通过getArray/setArray访问，volatile表示一个线程修改对另一个线程可见
private transient volatile Object[] array;
```

### 2.3 Constructor

```java
/**
 * 无参数的构造方法
 */
public CopyOnWriteArrayList() {
    // 所有对array的操作都是通过setArray()和getArray()进行
    setArray(new Object[0]);
}

final void setArray(Object[] a) {
    array = a;
}

//带参数的构造方法
public CopyOnWriteArrayList(Collection<? extends E> c) {
    Object[] elements;
    if (c.getClass() == CopyOnWriteArrayList.class)
        // 如果c也是CopyOnWriteArrayList类型
        // 那么直接把它的数组拿过来使用
        elements = ((CopyOnWriteArrayList<?>)c).getArray();
    else {
        elements = c.toArray();
        //这里c.toArray()不一定返回Objec[]类型，详细将ArrayList分析
        // c.toArray might (incorrectly) not return Object[] (see 6260652)
        if (elements.getClass() != Object[].class)
            elements = Arrays.copyOf(elements, elements.length, Object[].class);
    }
    setArray(elements);
}

//把toCopyIn数组拷贝到当前数组
public CopyOnWriteArrayList(E[] toCopyIn) {
    setArray(Arrays.copyOf(toCopyIn, toCopyIn.length, Object[].class));
}

```

### 2.4 Method

- public boolean add(E e)

  ```java
  public boolean add(E e) {
      final ReentrantLock lock = this.lock;
      //加锁
      lock.lock();
      try {
          //获取当前数组
          Object[] elements = getArray();
          int len = elements.length;
          //将旧数组元素拷贝到新数组，新数组大小=旧数组大小+1
          Object[] newElements = Arrays.copyOf(elements, len + 1);
          //将元素放到数组尾部
          newElements[len] = e;
          setArray(newElements);
          return true;
      } finally {
          //释放锁
          lock.unlock();
      }
  }
  ```