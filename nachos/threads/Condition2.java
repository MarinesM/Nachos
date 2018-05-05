package nachos.threads;
import java.util.LinkedList;
import nachos.machine.*;

/**
 * An implementation of condition variables that disables interrupt()s for
 * synchronization.
 *
 * <p>
 * You must implement this.
 *
 * @see	nachos.threads.Condition
 */
public class Condition2 {
    /**
     * Allocate a new condition variable.
     *
     * @param	conditionLock	the lock associated with this condition
     *				variable. The current thread must hold this
     *				lock whenever it uses <tt>sleep()</tt>,
     *				<tt>wake()</tt>, or <tt>wakeAll()</tt>.
     */
    public Condition2(Lock conditionLock) {
	     this.conditionLock = conditionLock;
    }

    /**
     * Atomically release the associated lock and go to sleep on this condition
     * variable until another thread wakes it using <tt>wake()</tt>. The
     * current thread must hold the associated lock. The thread will
     * automatically reacquire the lock before <tt>sleep()</tt> returns.b
     */
    public void sleep() {

	     Lib.assertTrue(conditionLock.isHeldByCurrentThread());
       boolean intStatus = Machine.interrupt().disable();
       //System.out.println("Going to sleep: " + KThread.currentThread().getName());
  		 waitQueue.add(KThread.currentThread());
       //Deshabilito candado del condition variable para meter el thread que hizo sleep a esa waitqueue
       conditionLock.release();
       KThread.sleep();
       Machine.interrupt().restore(intStatus);
	     conditionLock.acquire();

    }

    /**
     * Wake up at most one thread sleeping on this condition variable. The
     * current thread must hold the associated lock.
     */
    public void wake() {
    	Lib.assertTrue(conditionLock.isHeldByCurrentThread());

      boolean intStatus = Machine.interrupt().disable();
      		if (!waitQueue.isEmpty()){
            //System.out.println("Waking up: " + KThread.currentThread().getName());
            waitQueue.removeFirst().ready();
          }
            Machine.interrupt().restore(intStatus);
    }

    /**
     * Wake up all threads sleeping on this condition variable. The current
     * thread must hold the associated lock.
     */
    public void wakeAll() {
	       Lib.assertTrue(conditionLock.isHeldByCurrentThread());

         while (!waitQueue.isEmpty()){
         //Mientras el waitQueue tenga elementos, despertarlos
          wake();
        }
    }

    private Lock conditionLock;
    private LinkedList<KThread> waitQueue = new LinkedList<KThread>();
}
