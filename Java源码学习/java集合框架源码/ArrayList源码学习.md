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
### 2.2 Constructor

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

- public boolean add(E e);


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

- public void add(int index, E element)

```java
//在指定位置新增一条数据
public void add(int index, E element) {
    //范围检查
    rangeCheckForAdd(index);
	//确保容量
    ensureCapacityInternal(size + 1);  // Increments modCount!!
    //将index及其之后的元素向后挪一位，那么index位置就空出了
    System.arraycopy(elementData, index, elementData, index + 1,
                     size - index);
    //插入元素到index
    elementData[index] = element;
    //容量+1
    size++;
}

//add 方法， addAll方法使用的范围检查版本
private void rangeCheckForAdd(int index) {
    //当0≤index≤size(当前ArrayList实际容量)时，才会报错
    if (index > size || index < 0)
        throw new IndexOutOfBoundsException(outOfBoundsMsg(index));
}
/**
  * @param      src      原数组
  * @param      srcPos   原数组要复制的起始位置
  * @param      dest     目标数组
  * @param      destPos  目标数组要复制的起始位置
  * @param      length   要复制的长度
  */
public static native void arraycopy(Object src,  int  srcPos,
                                        Object dest, int destPos,
                                        int length);
```

- public boolean addAll(Collection<? extends E> c)

```java
//集合并集操作
public boolean addAll(Collection<? extends E> c) {
    //将集合c转化位数组
    Object[] a = c.toArray();
    int numNew = a.length;
    //检查是否需要扩容
    ensureCapacityInternal(size + numNew);  // Increments modCount
    //将c拷贝到elmentData末尾
    System.arraycopy(a, 0, elementData, size, numNew);
    //容量+numNew
    size += numNew;
    return numNew != 0;
}
```

- public E get(int index)

```java
public E get(int index) {
    //范围检查
    rangeCheck(index);

    return elementData(index);
}

private void rangeCheck(int index) {
    if (index >= size)//当要取的index>size报错
        throw new IndexOutOfBoundsException(outOfBoundsMsg(index));
}


E elementData(int index) {
    return (E) elementData[index];
}
```

- public E remove(int index)

```java
public E remove(int index) {
    //范围检查，判断是否越界
    rangeCheck(index);

    modCount++;
    E oldValue = elementData(index);
	// 如果index不是最后一位，则将index之后的元素往前挪一位
    int numMoved = size - index - 1;
    if (numMoved > 0)
        System.arraycopy(elementData, index+1, elementData, index,
                         numMoved);
    // 将最后一个元素删除，清理工作交给GC
    elementData[--size] = null; // clear to let GC do its work
    return oldValue;
}

private void rangeCheck(int index) {
    if (index >= size)//当要取的index>size报错
        throw new IndexOutOfBoundsException(outOfBoundsMsg(index));
}

E elementData(int index) {
    return (E) elementData[index];
}
```

- public boolean remove(Object o)

```java
public boolean remove(Object o) {
    if (o == null) {
        //遍历数组，找到第一次出现的地方，并删除
        for (int index = 0; index < size; index++)
             // 如果要删除的元素为null，则以null进行比较，使用==
            if (elementData[index] == null) {
                fastRemove(index);
                return true;
            }
    } else {
        //遍历数组，找到第一次出现的地方，并删除
        for (int index = 0; index < size; index++)
            // 如果要删除的元素不为null，则进行比较，使用equals()方法
            if (o.equals(elementData[index])) {
                fastRemove(index);
                return true;
            }
    }
    return false;
}

//快速删除，相比remove(int index)少一个数组越界检查，因为这里index就是从数组取的，所以必定在范围内
//少了越界检查过程，变相提高了效率
private void fastRemove(int index) {
    modCount++;
    // 如果index不是最后一位，则将index之后的元素往前挪一位
    int numMoved = size - index - 1;
    if (numMoved > 0)
        System.arraycopy(elementData, index+1, elementData, index,
                         numMoved);
     // 将最后一个元素删除，清理工作交给GC
    elementData[--size] = null; // clear to let GC do its work
}
```

- public boolean retainAll(Collection<?> c)

```java
//集合求交集
public boolean retainAll(Collection<?> c) {
    Objects.requireNonNull(c);
    // 同样调用批量删除方法，这时complement传入true，表示删除c中不包含的元素
    return batchRemove(c, true);
}

/**
  * 批量删除元素
  * complement为true表示删除c中不包含的元素
  * complement为false表示删除c中包含的元素
  */
private boolean batchRemove(Collection<?> c, boolean complement) {
    final Object[] elementData = this.elementData;
    // 使用读写两个指针同时遍历数组
    // 读指针每次自增1，写指针放入元素的时候才加1
    // 这样不需要额外的空间，只需要在原有的数组上操作就可以了
    int r = 0, w = 0;
    boolean modified = false;
    try {
        for (; r < size; r++)
            //遍历整个数组，如果c中有这个元素，把这个元素放到数组w位置，然后将w后移一位
            if (c.contains(elementData[r]) == complement)
                elementData[w++] = elementData[r];
    } finally {
        // 正常来说r最后是等于size的，除非c.contains()抛出了异常
        if (r != size) {
            // 如果c.contains()抛出了异常，则把未读的元素都拷贝到写指针之后
            System.arraycopy(elementData, r,
                             elementData, w,
                             size - r);
            w += size - r;
        }
        if (w != size) {
            // 将写指针之后的元素置为空
            for (int i = w; i < size; i++)
                elementData[i] = null;
            modCount += size - w;
            // 新大小等于写指针的位置（因为每写一次写指针就加1，所以新大小正好等于写指针的位置）
            size = w;
            // 有修改返回true
            modified = true;
        }
    }
    return modified;
}
```

- public boolean removeAll(Collection<?> c)

```java
//只保留当前集合中不在c中的元素，不保留在c中不在当前集体中的元素。
public boolean removeAll(Collection<?> c) {
    Objects.requireNonNull(c);
    // 同样调用批量删除方法，这时complement传入false，表示删除包含在c中的元素
    return batchRemove(c, false);
}
```

