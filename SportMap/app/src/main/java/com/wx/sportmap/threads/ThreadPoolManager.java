package com.wx.sportmap.threads;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by Lin on 2017/7/3.
 */

public class ThreadPoolManager {

	private static  ThreadPoolManager instance=new ThreadPoolManager();

	private LinkedBlockingQueue<Future<?>> taskQuene=new LinkedBlockingQueue<>();

	private ThreadPoolExecutor threadPoolExecutor;
	public static ThreadPoolManager getInstance() {

		return instance;
	}
	private ThreadPoolManager()
	{
		threadPoolExecutor=new ThreadPoolExecutor(6,20,10, TimeUnit.SECONDS,new ArrayBlockingQueue<Runnable>(4), handler);
		threadPoolExecutor.execute(runable);
	}

	public <T> boolean removeTask(FutureTask futureTask)
	{
		boolean result=false;
		/**
		 * 阻塞式队列是否含有线程
		 */
		if(taskQuene.contains(futureTask))
		{
			taskQuene.remove(futureTask);
		}else
		{
			result=threadPoolExecutor.remove(futureTask);
		}
		return  result;
	}


	private Runnable runable =new Runnable() {
		@Override
		public void run() {
			while (true)
			{
				FutureTask futrueTask=null;

				try {
					/**
					 * 阻塞式函数
					 */
//					Log.i(TAG,"等待队列     "+taskQuene.size());
					futrueTask= (FutureTask) taskQuene.take();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				if(futrueTask!=null)
				{
					threadPoolExecutor.execute(futrueTask);
				}
//				Log.i(TAG,"线程池大小      "+threadPoolExecutor.getPoolSize());
			}
		}
	};
	public <T> void execte(FutureTask<T> futureTask) throws InterruptedException {
		taskQuene.put(futureTask);
	}

	private RejectedExecutionHandler handler=new RejectedExecutionHandler() {
		@Override
		public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
			try {
				taskQuene.put(new FutureTask<Object>(r,null) {
				});
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	};

}
