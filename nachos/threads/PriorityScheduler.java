package nachos.threads;

import nachos.machine.*;

import java.util.*;

/**
 * A scheduler that chooses threads based on their priorities.
 * <p/>
 * <p/>
 * A priority scheduler associates a priority with each thread. The next thread
 * to be dequeued is always a thread with priority no less than any other
 * waiting thread's priority. Like a round-robin scheduler, the thread that is
 * dequeued is, among all the threads of the same (highest) priority, the
 * thread that has been waiting longest.
 * <p/>
 * <p/>
 * Essentially, a priority scheduler gives access in a round-robin fassion to
 * all the highest-priority threads, and ignores all other threads. This has
 * the potential to
 * starve a thread if there's always a thread waiting with higher priority.
 * <p/>
 * <p/>
 * A priority scheduler must partially solve the priority inversion problem; in
 * particular, priority must be donated through locks,` and through joins.
 */
public class PriorityScheduler extends Scheduler {
    /**
     * Allocate a new priority scheduler.
     */
    public PriorityScheduler() {
    }

    /**
     * Allocate a new priority thread queue.
     *
     * @param
      <tt>true</tt> if this queue should
     *                         transfer priority from waiting threads
     *                         to the owning thread.
     * @return a new priority thread queue.
     */
    public ThreadQueue newThreadQueue(boolean transferPriority) {
        return new PriorityQueue(transferPriority);
    }

    public int getPriority(KThread thread) {
        Lib.assertTrue(Machine.interrupt().disabled());

        return getThreadState(thread).getPriority();
    }

    public int getEffectivePriority(KThread thread) {
        Lib.assertTrue(Machine.interrupt().disabled());

        return getThreadState(thread).getEffectivePriority();
    }

    public void setPriority(KThread thread, int priority) {
        Lib.assertTrue(Machine.interrupt().disabled());

        Lib.assertTrue(priority >= priorityMinimum &&
                priority <= priorityMaximum);

        getThreadState(thread).setPriority(priority);
    }

    public boolean increasePriority() {
        boolean intStatus = Machine.interrupt().disable();

        KThread thread = KThread.currentThread();

        int priority = getPriority(thread);
        if (priority == priorityMaximum)
            return false;

        setPriority(thread, priority + 1);

        Machine.interrupt().restore(intStatus);
        return true;
    }

    public boolean decreasePriority() {
        boolean intStatus = Machine.interrupt().disable();

        KThread thread = KThread.currentThread();

        int priority = getPriority(thread);
        if (priority == priorityMinimum)
            return false;

        setPriority(thread, priority - 1);

        Machine.interrupt().restore(intStatus);
        return true;
    }

    /**
     * The default priority for a new thread. Do not change this value.
     */
    public static final int priorityDefault = 1;
    /**
     * The minimum priority that a thread can have. Do not change this value.
     */
    public static final int priorityMinimum = 0;
    /**
     * The maximum priority that a thread can have. Do not change this value.
     */
    public static final int priorityMaximum = 7;

    /**
     * Return the scheduling state of the specified thread.
     *
     * @param thread the thread whose scheduling state to return.
     * @return the scheduling state of the specified thread.
     */
    protected ThreadState getThreadState(KThread thread) {
        if (thread.schedulingState == null)
            thread.schedulingState = new ThreadState(thread);

        return (ThreadState) thread.schedulingState;
    }

    /**
     * A <tt>ThreadQueue</tt> that sorts threads by priority.
     */
    protected class PriorityQueue extends ThreadQueue {

      PriorityQueue(boolean transferPriority) {
	       this.transferPriority = transferPriority;
	    }

        public void waitForAccess(KThread thread) {
            Lib.assertTrue(Machine.interrupt().disabled());
            ThreadState ts = getThreadState(thread);
            int thisPriority = ts.getPriority();
            if (this.resourceHolder != null && this.resourceHolder.getThread().getName() != "main"){
              if (this.resourceHolder.getPriority() < thisPriority){
                this.resourceHolder.previousPriority = this.resourceHolder.getPriority();
                this.resourceHolder.setPriority(thisPriority);
              }
            }

            //Agrega el queue al listado de los recursos que esta esperando
            this.waitQueue.add(ts);
            ts.waitForAccess(this);
        }

        public void acquire(KThread thread) {
            Lib.assertTrue(Machine.interrupt().disabled());
            ThreadState ts = getThreadState(thread);
            if (this.resourceHolder != null) {
              //Si antes le donaron prioridad
                if (this.resourceHolder.previousPriority != 0){
                  int previous = this.resourceHolder.previousPriority;
                  this.resourceHolder.setPriority(previous);
                  this.resourceHolder.previousPriority = 0;
                }
                //Borra al thread que tenia esta cola
                this.resourceHolder.currentResources.remove(this);
            }
            //Thread se vuelve dueno de esta cola
            this.resourceHolder = ts;
            ts.acquire(this);
        }

        public KThread nextThread() {
            Lib.assertTrue(Machine.interrupt().disabled());

            // Escojo el siguiente thread a ejecutar
            ThreadState nextThread = this.pickNextThread();
            if (nextThread == null) return null;
            this.waitQueue.remove(nextThread);
            // Give nextThread the resource
            this.acquire(nextThread.getThread());
            return nextThread.getThread();
        }

        /**
         * Return the next thread that <tt>nextThread()</tt> would return,
         * without modifying the state of this queue.
         *
         * @return the next thread that <tt>nextThread()</tt> would
         *         return.
         */
        protected ThreadState pickNextThread() {
            int nextPriority = 0;
            ThreadState next = waitQueue.peek();
            for (ThreadState currThread : this.waitQueue) {
                int currPriority = currThread.getPriority();
                //System.out.println("Thread " + currThread.getThread().getName());
                //System.out.println("Priority " + currPriority);
                if (currPriority == nextPriority){
                  if(currThread.threadAge <= next.threadAge){
                      next = currThread;
                      nextPriority = currPriority;
                  }
                }else if (next == null || (currPriority > nextPriority)){
                  next = currThread;
                  nextPriority = currPriority;
                }

            }
            return next;
        }

        /**
         * This method returns the effectivePriority of this PriorityQueue.
         * The return value is cached for as long as possible. If the cached value
         * has been invalidated, this method will spawn a series of mutually
         * recursive calls needed to recalculate effectivePriorities across the
         * entire resource graph.
         * @return
         */

        public void print() {
            Lib.assertTrue(Machine.interrupt().disabled());
            for (final ThreadState ts : this.waitQueue) {
                System.out.println(ts.getThread() + " priority: " + ts.getEffectivePriority());
            }
        }

        public boolean transferPriority;
        //Lista de threads que estan esperando accesar este recurso
        protected LinkedList<ThreadState> waitQueue = new LinkedList<ThreadState>();
         //variable tipo thread state que indica que thread es dueno actual de este recurso
        protected ThreadState resourceHolder = null;
        protected int effectivePriority = priorityMinimum;

    }

    /**
     * The scheduling state of a thread. This should include the thread's
     * priority, its effective priority, any objects it owns, and the queue
     * it's waiting for, if any.
     *
     * @see nachos.threads.KThread#schedulingState
     */
    protected class ThreadState {
        /**
         * Allocate a new <tt>ThreadState</tt> object and associate it with the
         * specified thread.
         *
         * @param thread the thread this state belongs to.
         */
        public ThreadState(KThread thread) {
            this.thread = thread;
            this.currentResources = new LinkedList<PriorityQueue>();
            setPriority(priorityDefault);

        }

        /**
         * Return the priority of the associated thread.
         *
         * @return the priority of the associated thread.
         */
        public int getPriority() {
            return priority;
        }

        /**
         * Return the effective priority of the associated thread.
         *
         * @return the effective priority of the associated thread.
         */
        public int getEffectivePriority() {
            return this.getPriority();
        }

        /**
         * Set the priority of the associated thread to the specified value.
         *
         * @param priority the new priority.
         */
        public void setPriority(int priority) {
            if (this.priority == priority)
                return;
            this.priority = priority;

        }

        /**
         * Called when <tt>waitForAccess(thread)</tt> (where <tt>thread</tt> is
         * the associated thread) is invoked on the specified priority queue.
         * The associated thread is therefore waiting for access to the
         * resource guarded by <tt>waitQueue</tt>. This method is only called
         * if the associated thread cannot immediately obtain access.
         *
         * @param waitQueue the queue that the associated thread is
         *                  now waiting on.
         * @see nachos.threads.ThreadQueue#waitForAccess
         */
        public void waitForAccess(PriorityQueue waitQueue) {
            this.threadAge = Machine.timer().getTime();
            this.waitingOn = waitQueue;
            //Como lo estoy esperando, lo quito de la lista de lo que actualmente tengo
            this.currentResources.remove(waitQueue);
        }

        /**
         * Called when the associated thread has acquired access to whatever is
         * guarded by <tt>waitQueue</tt>. This can occur either as a result of
         * <tt>acquire(thread)</tt> being invoked on <tt>waitQueue</tt> (where
         * <tt>thread</tt> is the associated thread), or as a result of
         * <tt>nextThread()</tt> being invoked on <tt>waitQueue</tt>.
         *
         * @see nachos.threads.ThreadQueue#acquire
         * @see nachos.threads.ThreadQueue#nextThread
         */
        public void acquire(PriorityQueue waitQueue) {
            this.currentResources.add(waitQueue);
            //Como el thread ya lo tiene, lo quita de su lista de lo que esta esperando
            this.waitingOn = null;
        }

        /**
         * Called when the associated thread has relinquished access to whatever
         * is guarded by waitQueue.
          * @param waitQueue The waitQueue corresponding to the relinquished resource.
         */


        public KThread getThread() {
            return thread;
        }


        protected KThread thread;
        protected int priority;
        protected long threadAge = Machine.timer().getTime();
        //Variable que almacena la prioridad antes de las donaciones
        protected int previousPriority = 0;
        protected List<PriorityQueue> currentResources;
        protected PriorityQueue waitingOn = null;

    }

/*
    public static void selfTest() {
    		System.out.println("---------PriorityScheduler test---------------------");
    		PriorityScheduler s = new PriorityScheduler();
    		ThreadQueue queue = s.newThreadQueue(true);

    		KThread thread1 = new KThread();
    		KThread thread2 = new KThread();
    		KThread thread3 = new KThread();
    		KThread thread4 = new KThread();
    		KThread thread5 = new KThread();
    		thread1.setName("thread1");
    		thread2.setName("thread2");
    		thread3.setName("thread3");
    		thread4.setName("thread4");
    		thread5.setName("thread5");


    		boolean intStatus = Machine.interrupt().disable();

    		s.getThreadState(thread1).setPriority(7);
    		queue.acquire(thread1);
    		s.getThreadState(thread2).setPriority(1);
    		s.getThreadState(thread3).setPriority(5);
    		s.getThreadState(thread4).setPriority(5);
    		s.getThreadState(thread5).setPriority(2);


    		queue.waitForAccess(thread2);
    		queue.waitForAccess(thread4);
    		queue.waitForAccess(thread3);
    		queue.waitForAccess(thread5);
    		System.out.println("thread1 EP = "+s.getThreadState(thread1).getEffectivePriority());
    		System.out.println("thread2 EP = "+s.getThreadState(thread2).getEffectivePriority());
    		System.out.println("thread3 EP = "+s.getThreadState(thread3).getEffectivePriority());
    		System.out.println("thread4 EP = "+s.getThreadState(thread4).getEffectivePriority());
    		System.out.println("thread5 EP = "+s.getThreadState(thread5).getEffectivePriority());

    		//System.out.println(s.getThreadState(thread4).timeWaitQueue);
    		//System.out.println(s.getThreadState(thread5).timeWaitQueue);
        System.out.println("Elements in queue: ");
        queue.print();
    		System.out.println("Siguiente thread a ejecutar = " + queue.nextThread());
    		Machine.interrupt().restore(intStatus);
    		System.out.println("--------End PriorityScheduler test------------------");
    	}
*/
      public static void selfTest2(){
      		/*
      		 * Creates 3 threads with different priorities and runs them
      		 */
      		System.out.println("Priority TEST #2: START");
      		KThread thread1 = new KThread(new Runnable(){
      			public void run(){

      				//KThread.yield();

      				System.out.println("Im first to run");
      			}
      		}).setName("Thread1");
      		KThread thread2 = new KThread(new Runnable(){
      			public void run(){
      				System.out.println("Im second to run");
      			}
      		}).setName("Thread2");
      		KThread thread3 = new KThread(new Runnable(){

      			public void run(){

      				//KThread.yield();
      				System.out.println("Im third to run");
      			}
      		}).setName("Thread3");
      		Machine.interrupt().disable();
      		ThreadedKernel.scheduler.setPriority(3);
      		ThreadedKernel.scheduler.setPriority(thread1, 7);
      		ThreadedKernel.scheduler.setPriority(thread2, 5);
      		ThreadedKernel.scheduler.setPriority(thread3, 7);


      		Machine.interrupt().enable();
      		thread1.fork();
      		thread2.fork();
      		thread3.fork();

      		KThread.yield();
      		System.out.println("Priority TEST #2: END");
      	}

        public static void selfTestRun(KThread t1, int t1p, KThread t2, int t2p)
        {

            boolean int_state;

            int_state = Machine.interrupt().disable();
            ThreadedKernel.scheduler.setPriority(t1, t1p);
            ThreadedKernel.scheduler.setPriority(t2, t2p);
            Machine.interrupt().restore(int_state);

            t1.setName("a").fork();
            t2.setName("b").fork();
            t1.join();
            t2.join();

        }

public static void selfTestRun(KThread t1, int t1p, KThread t2, int t2p, KThread t3, int t3p)
{

boolean int_state;

int_state = Machine.interrupt().disable();
ThreadedKernel.scheduler.setPriority(t1, t1p);
ThreadedKernel.scheduler.setPriority(t2, t2p);
ThreadedKernel.scheduler.setPriority(t3, t3p);
Machine.interrupt().restore(int_state);

t1.setName("a").fork();
t2.setName("b").fork();
t3.setName("c").fork();
t1.join();
t2.join();
t3.join();

}

/**
* Tests whether this module is working.
*/

public static void selfTest()
{

KThread t1, t2, t3;
final Lock lock;
final Condition2 condition;

/*
* Case 1: Tests priority scheduler without donation
*
* This runs t1 with priority 7, and t2 with priority 4.
*
*/

System.out.println("Case 1:");

t1 = new KThread(new Runnable()
{
public void run()
{
System.out.println(KThread.currentThread().getName() + " started working");
for (int i = 0; i < 10; ++i)
{
System.out.println(KThread.currentThread().getName() + " working " + i);
KThread.yield();
}
System.out.println(KThread.currentThread().getName() + " finished working");
}
});

t2 = new KThread(new Runnable()
{
public void run()
{
System.out.println(KThread.currentThread().getName() + " started working");
for (int i = 0; i < 10; ++i)
{
System.out.println(KThread.currentThread().getName() + " working " + i);
KThread.yield();
}
System.out.println(KThread.currentThread().getName() + " finished working");
}

});

selfTestRun(t1, 7, t2, 4);

/*
* Case 2: Tests priority scheduler without donation, altering
* priorities of threads after they've started running
*
* This runs t1 with priority 7, and t2 with priority 4, but
* half-way through t1's process its priority is lowered to 2.
*
*/

System.out.println("Case 2:");

t1 = new KThread(new Runnable()
{
public void run()
{
System.out.println(KThread.currentThread().getName() + " started working");
for (int i = 0; i < 10; ++i)
{
System.out.println(KThread.currentThread().getName() + " working " + i);
KThread.yield();
if (i == 4)
{
System.out.println(KThread.currentThread().getName() + " reached 1/2 way, changing priority");
boolean int_state = Machine.interrupt().disable();
ThreadedKernel.scheduler.setPriority(2);
Machine.interrupt().restore(int_state);
}
}
System.out.println(KThread.currentThread().getName() + " finished working");
}
});

t2 = new KThread(new Runnable()
{
public void run()
{
System.out.println(KThread.currentThread().getName() + " started working");
for (int i = 0; i < 10; ++i)
{
System.out.println(KThread.currentThread().getName() + " working " + i);
KThread.yield();
}
System.out.println(KThread.currentThread().getName() + " finished working");
}

});

selfTestRun(t1, 7, t2, 4);

/*
* Case 3: Tests priority donation
*
* This runs t1 with priority 7, t2 with priority 6 and t3 with
* priority 4. t1 will wait on a lock, and while t2 would normally
* then steal all available CPU, priority donation will ensure that
* t3 is given control in order to help unlock t1.
*
*/

System.out.println("Case 3:");

lock = new Lock();
condition = new Condition2(lock);

t1 = new KThread(new Runnable()
{
public void run()
{
lock.acquire();
System.out.println(KThread.currentThread().getName() + " active");
lock.release();
}
});

t2 = new KThread(new Runnable()
{
public void run()
{
System.out.println(KThread.currentThread().getName() + " started working");
for (int i = 0; i < 3; ++i)
{
System.out.println(KThread.currentThread().getName() + " working " + i);
KThread.yield();
}
System.out.println(KThread.currentThread().getName() + " finished working");
}

});

t3 = new KThread(new Runnable()
{
public void run()
{
lock.acquire();

boolean int_state = Machine.interrupt().disable();
ThreadedKernel.scheduler.setPriority(2);
Machine.interrupt().restore(int_state);

KThread.yield();

// t1.acquire() will now have to realise that t3 owns the lock it wants to obtain
// so program execution will continue here.

System.out.println(KThread.currentThread().getName() + " active ('a' wants its lock back so we are here)");
lock.release();
KThread.yield();
lock.acquire();
System.out.println(KThread.currentThread().getName() + " active-again (should be after 'a' and 'b' done)");
lock.release();

}
});

selfTestRun(t1, 6, t2, 4, t3, 7);

}

}
