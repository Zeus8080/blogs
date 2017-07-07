#  Java定时器的三种实现方式

## 1.普通thread


```
package com.local.test;

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

