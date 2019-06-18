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

  添加一个元素在尾部
  
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

- public void add(int index, E element)

  添加一个元素在指定索引处

  ```java
  public void add(int index, E element) {
      final ReentrantLock lock = this.lock;
      //加锁
      lock.lock();
      try {
          //获取旧数组
          Object[] elements = getArray();
          int len = elements.length;
          //检查是否越界
          if (index > len || index < 0)
              throw new IndexOutOfBoundsException("Index: "+index+
                                                  ", Size: "+len);
          Object[] newElements;
          int numMoved = len - index;
          //等于len的时候意味着在数组末尾加一个
          if (numMoved == 0)
              //拷贝一个n+1的数组，前面n个元素与旧数组一样
              newElements = Arrays.copyOf(elements, len + 1);
          else {
              //如果插入位置不是最后一位
              //新建一个n+1数组
              newElements = new Object[len + 1];
              //拷贝旧数组前index元素到新数组
              System.arraycopy(elements, 0, newElements, 0, index);
              //这样新数组第index位置刚好空出
              System.arraycopy(elements, index, newElements, index + 1,
                               numMoved);
          }
          //将元素放到index处
          newElements[index] = element;
          setArray(newElements);
      } finally {
          //解锁
          lock.unlock();
      }
  }
  ```

- public boolean addIfAbsent(E e)

  添加一个元素，如果这个元素不存在集合

  ```java
  public boolean addIfAbsent(E e) {
      Object[] snapshot = getArray();
      return indexOf(e, snapshot, 0, snapshot.length) >= 0 ? false :
          addIfAbsent(e, snapshot);
  }
  
  private boolean addIfAbsent(E e, Object[] snapshot) {
      final ReentrantLock lock = this.lock;
      lock.lock();
      try {
          Object[] current = getArray();
          int len = current.length;
          if (snapshot != current) {
              // Optimize for lost race to another addXXX operation
              int common = Math.min(snapshot.length, len);
              for (int i = 0; i < common; i++)
                  if (current[i] != snapshot[i] && eq(e, current[i]))
                      return false;
              if (indexOf(e, current, common, len) >= 0)
                  return false;
          }
          Object[] newElements = Arrays.copyOf(current, len + 1);
          newElements[len] = e;
          setArray(newElements);
          return true;
      } finally {
          lock.unlock();
      }
  }
  ```

