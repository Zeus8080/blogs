# java.util.CopyOnWriteArrayList

## 1.层次结构

![LinkedList的继承关系](/图片/jdk1.8源码系列/CopyOnWriteLArrayList.png)

CopyOnWriteArrayList是ArrayList线程安全版本

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
          //释放锁
          lock.unlock();
      }
  }
  ```

- public boolean addIfAbsent(E e)

  添加一个元素，如果这个元素不存在集合

  ```java
  public boolean addIfAbsent(E e) {
      //获取当前数组
      Object[] snapshot = getArray();
      //检查元素是否存在,若不存在返回false,存在调用addIfAbsent(e, snapshot)方法
      return indexOf(e, snapshot, 0, snapshot.length) >= 0 ? false :
          addIfAbsent(e, snapshot);
  }
  //找到元素o在数组elements在index到fence范围内存不存在
  private static int indexOf(Object o, Object[] elements,
                             int index, int fence) {
      if (o == null) {
          for (int i = index; i < fence; i++)
              if (elements[i] == null)
                  return i;
      } else {
          for (int i = index; i < fence; i++)
              if (o.equals(elements[i]))
                  return i;
      }
      return -1;
  }
  
  private boolean addIfAbsent(E e, Object[] snapshot) {
      //加锁
      final ReentrantLock lock = this.lock;
      lock.lock();
      try {
          //重新获取一遍旧数组
          Object[] current = getArray();
          int len = current.length;
          //如果当前获取的current与之前快照版本snapshot不一致,说明数组有修改
          if (snapshot != current) {
              //这里取当前数组和快照版本较小的那个
              int common = Math.min(snapshot.length, len);
              //这里保证当前数组common范围内是没有修改的,否则返回false,即添加失败
              for (int i = 0; i < common; i++)
                  if (current[i] != snapshot[i] && eq(e, current[i]))
                      return false;
              //这里保证当前数组common范围内,不包含要添加元素E
              if (indexOf(e, current, common, len) >= 0)
                  return false;
          }
          //拷贝一分n+1的数组
          Object[] newElements = Arrays.copyOf(current, len + 1);
          //将元素放到最后一位
          newElements[len] = e;
          setArray(newElements);
          return true;
      } finally {
          //释放锁
          lock.unlock();
      }
  }
  //判断o1和o2是否相同
  private static boolean eq(Object o1, Object o2) {
      return (o1 == null) ? o2 == null : o1.equals(o2);
  }
  ```

- public E get(int index)

  获取指定索引的元素，支持随机访问(实现了RandomAcess接口)

  ```java
  public E get(int index) {
      // 获取元素不需要加锁
      // 直接返回index位置的元素
      // 这里是没有做越界检查的, 因为数组本身会做越界检查
      return get(getArray(), index);
  }
  
  private E get(Object[] a, int index) {
  	return (E) a[index];
  }
  ```

- public E remove(int index)

  删除指定索引元素

  ```java
  public E remove(int index) {
      //加锁
      final ReentrantLock lock = this.lock;
      lock.lock();
      try {
          //获取当前数组
          Object[] elements = getArray();
          int len = elements.length;
          E oldValue = get(elements, index);
          //删除index位置的元素后,需要移动元素的个数
          int numMoved = len - index - 1;
          if (numMoved == 0)
              //等于0,说明不需要移动元素,删除元素在数组末尾
              // 那么直接拷贝一份n-1的新数组, 最后一位就自动删除了
              setArray(Arrays.copyOf(elements, len - 1));
          else {
              // 如果移除的不是最后一位
              // 那么新建一个n-1的新数组
              Object[] newElements = new Object[len - 1];
              // 将前index的元素拷贝到新数组中
              System.arraycopy(elements, 0, newElements, 0, index);
              // 将index后面(不包含)的元素往前挪一位
              // 这样正好把index位置覆盖掉了, 相当于删除了
              System.arraycopy(elements, index + 1, newElements, index,
                               numMoved);
              setArray(newElements);
          }
          return oldValue;
      } finally {
          //释放锁
          lock.unlock();
      }
  }
  ```

- public int size()

  数组大小

  ```java
  public int size() {
      // 获取元素个数不需要加锁
      // 直接返回数组的长度
      return getArray().length;
  }
  ```

### 3.总结

*对比ArrayList为什么CopyOnWriteArrayList没有size属性？*

因为每次修改都是拷贝一份正好可以存储目标个数元素的数组，所以不需要size属性了，数组的长度就是集合的大小，而不像ArrayList数组的长度实际是要大于集合的大小的。

例如,add(E e)操作，先拷贝一份n+1个元素的数组，再把新元素放到新数组的最后一位，这时新数组的长度为len+1了，也就是集合的size了