package nachos.threads;
import java.util.PriorityQueue;
import nachos.machine.*;

/**
 * Uses the hardware timer to provide preemption, and to allow threads to sleep
 * until a certain time.
 */
public class Alarm {
    /**
     * Allocate a new Alarm. Set the machine's timer interrupt handler to this
     * alarm's callback.
     *
     * <p><b>Note</b>: Nachos will not function correctly with more than one
     * alarm.
     */
    public Alarm() {
       //Instancia timer interrupt con un runnable como unico argumento
	     Machine.timer().setInterruptHandler(new Runnable() {
		   public void run() {
         timerInterrupt();
       }
	    });
    }

    /**
     * The timer interrupt handler. This is called by the machine's timer
     * periodically (approximately every 500 clock ticks). Causes the current
     * thread to yield, forcing a context switch if there is another thread
     * that should be run.
     */
    public void timerInterrupt() {
      boolean intStatus = Machine.interrupt().disable();
      while (!waitQueue.isEmpty())
         //comparamos con el primer elemento de la lista y si ya se tuvo que haber despertado lo pongo en ready
			   if (waitQueue.peek().wakeTime <= Machine.timer().getTime())
             //Si es menor lo quitamos de la cola
				     waitQueue.poll().thread.ready();
			   else
             break;
	    KThread.currentThread().yield();
      Machine.interrupt().restore(intStatus);

    }

    /**
     * Put the current thread to sleep for at least <i>x</i> ticks,
     * waking it up in the timer interrupt handler. The thread must be
     * woken up (placed in the scheduler ready set) during the first timer
     * interrupt where
     *
     * <p><blockquote>
     * (current time) >= (WaitUntil called time)+(x)
     * </blockquote>
     *
     * @param	x	the minimum number of clock ticks to wait.
     *
     * @see	nachos.machine.Timer#getTime()
     */
    public void waitUntil(long x) {
        long currTime = Machine.timer().getTime();
	      long wakeTime = currTime + x;
        //Agregar current thread con su wakeTime en el waitQueue
        boolean intStatus = Machine.interrupt().disable();
        waitQueue.add(new ThreadStatusHolder(KThread.currentThread(), wakeTime));
        KThread.currentThread().sleep();
        Machine.interrupt().restore(intStatus);
    }

	   private PriorityQueue<ThreadStatusHolder> waitQueue = new PriorityQueue<ThreadStatusHolder>();

	   private class ThreadStatusHolder implements Comparable<ThreadStatusHolder> {
       //PrivateStatusHolder guarda el thread que llamo waitUntil y su wake time
		     private KThread thread;
		     private long wakeTime;

		     ThreadStatusHolder(KThread thread, long wakeTime) {
			        this.thread = thread;
			        this.wakeTime = wakeTime;
		}

		@Override
		public int compareTo(ThreadStatusHolder that) {
			return Long.signum(wakeTime - that.wakeTime);
		}
}

}
