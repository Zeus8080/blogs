# Java内存区域与内存溢出异常 #
## 1.Java虚拟机运行时数据区 ##
![](http://i.imgur.com/eT0vofH.png)
### 1.1程序计数器(Program Counter Register) ###
程序计数器(Program Counter Register)是一块较小的内存空间，它可以被看作当前线程所执行的字节码的行号指示器。字节码解释器就是通过改变该计数器的值来选取下一条需要执行的字节码指令，分支、循环、跳转、异常处理、线程恢复等基础功能都需依赖计数器来完成。

由于Java虚拟机的多线程是通过线程轮流切换并分配处理器执行时间来实现的，在任何一个确定的时刻，一个处理器(对多核处理器来说是一个内核)都只会执行一个线程的内容。所以，为了确保线程切换后能正常执行，每个线程都需要设置一个独立的程序计数器，各个线程的计数器独立存储，互不影响，我们称这类内存为“线程私有的”内存。

注意：如果一个线程执行的是Java方法，当前程序计数器记录的是正在执行的虚拟字节码指令的地址值；如果是<a href="http://blog.csdn.net/wike163/article/details/6635321">Native方法</a>，
计数器值则为空(Undefined)。此内存区域(程序计数器)是唯一一个在Java虚拟机规范中没有规定任何OutOfMemoryError情况的地方。<br/>

### 1.2Java虚拟机栈(Java Virtual Machine Stacks) ###
虚拟机栈是描述Java方法执行的内存模型：每个方法在执行的同时会创建一个栈帧(Stack Frame)用于存储局部变量、操作数栈、动态链接、方法出口等信息。每个方法从调用到执行完成的过程对应着一个栈帧在虚拟机栈中入栈到出栈的过程。与程序计数器一样是线程私有的。<br/>
这个区域规范了两种异常的状况：StackOverflowError和OutOfMemoryError

### 1.3本地方法栈(Native Method Stack) ###
本地方法栈与虚拟机栈所发挥的作用是相似的，区别是虚拟机栈为虚拟机执行Java方法服务，本地方法栈为虚拟机使用到的Native服务，也会抛出StackOverFlowError和OutOfMemoryError异常。

### 1.4Java堆(Java Heap)###
