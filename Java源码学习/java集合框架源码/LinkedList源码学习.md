# java.util.LinkedList源码分析

## 1.层次结构

![LinkedList的继承关系](/图片/jdk1.8源码系列/linkedList.png)

# 2.分析

### 2.1 数据结构

LinkedList是一个双向链表

```java
//LinkedList内部定义的数据结构
private static class Node<E> {
    //存储元素
    E item; 
    //指向下一个节点的引用
    Node<E> next;
    //指向上个节点的引用
    Node<E> prev;

    Node(Node<E> prev, E element, Node<E> next) {
        this.item = element;
        this.next = next;
        this.prev = prev;
    }
}
```



### 2.2 LinkedList的属性

### 2.3 Constructor

### 2.4 Method