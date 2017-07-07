#  Java定时器的三种实现方式

## 1.普通thread


```


public class TimeJobTest {
	final long time = 1000;
	
	public static void main(String[] args) {
		final long time = 1000;
		Runnable timethread = new Runnable() {
			public void run() {
				while(true){
					        System.out.println("hello");
					try {
						Thread.sleep(time);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		};
		Thread t = new Thread(timethread);
		t.start();
	}
}

```

## 2.Timer和TimerTask类


```
import java.util.Timer;  
import java.util.TimerTask;  
  
/** 
 *  
 * 于第一种方式相比，优势 1>当启动和去取消任务时可以控制 2>第一次执行任务时可以指定你想要的delay时间 
 *  
 * 在实现时，Timer类可以调度任务，TimerTask则是通过在run()方法里实现具体任务。 Timer实例可以调度多任务，它是线程安全的。 
 * 当Timer的构造器被调用时，它创建了一个线程，这个线程可以用来调度任务。 下面是代码： 
 *  
 * @author GT 
 *  
 */  
public class Task2 {  
    public static void main(String[] args) {  
        TimerTask task = new TimerTask() {  
            @Override  
            public void run() {  
                // task to run goes here  
                System.out.println("Hello !!!");  
            }  
        };  
        Timer timer = new Timer();  
        long delay = 0;  
        long intevalPeriod = 1 * 1000;  
        // schedules the task to be run in an interval  
        timer.scheduleAtFixedRate(task, delay, intevalPeriod);  
    } // end of main  
} 
```



