# java.util.ArrayList源码分析

##1.层次结构
首先，我们先来看看arraylist的继承关系，了解下具体实现或者继承了那些类，具体如图：
![ArrayList的继承关系](/图片/jdk1.8源码系列/arraylist.png)

##2 分析
###2.1 ArrayList的属性

	//默认初始化的容量
    private static final int DEFAULT_CAPACITY = 10;
	//空数组对象
	private static final Object[] EMPTY_ELEMENTDATA = {};
	//另一个空数组，跟上一个相比可以知道增加了多少
	private static final Object[] DEFAULTCAPACITY_EMPTY_ELEMENTDATA = {};
	//数组缓存，arrayList实际存储数据的地方
	transient Object[] elementData;
	//数量
	private int size;
### 2.2 constuctor
	
    /**
	 * 带容量的构造器 initialCapacity指的初始化容量的大小
	 */
	public ArrayList(int initialCapacity) {
        if (initialCapacity > 0) {
            this.elementData = new Object[initialCapacity];
        } else if (initialCapacity == 0) {
            this.elementData = EMPTY_ELEMENTDATA;
        } else {
            throw new IllegalArgumentException("Illegal Capacity: "+initialCapacity);
        }
    }

	/**
	 * 我们最常用的构造器，注意此时DEFAULTCAPACITY_EMPTY_ELEMENTDATA的值为空，在jdk8中，初始容量默认是0，而不是10
	 */
	public ArrayList() {
        this.elementData = DEFAULTCAPACITY_EMPTY_ELEMENTDATA;
    }


