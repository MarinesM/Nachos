package nachos.threads;

import nachos.machine.*;

/**
 * A <i>communicator</i> allows threads to synchronously exchange 32-bit
 * messages. Multiple threads can be waiting to <i>speak</i>,
 * and multiple threads can be waiting to <i>listen</i>. But there should never
 * be a time when both a speaker and a listener are waiting, because the two
 * threads can be paired off at this point.
 */
public class Communicator {
    /**
     * Allocate a new communicator.
     */
    public Communicator() {
        //Variables nuevas
        communicationLock = new Lock();
		    currentSpeaker = new Condition2(communicationLock);
		    currentListener = new Condition2(communicationLock);
		    newMsg = false;

    }

    /**
     * Wait for a thread to listen through this communicator, and then transfer
     * <i>word</i> to the listener.
     *
     * <p>
     * Does not return until this thread is paired up with a listening thread.
     * Exactly one listener should receive <i>word</i>.
     *
     * @param	word	the integer to transfer.
     */
    public void speak(int currentMsg) {
      communicationLock.acquire();
      speaker += 1;
      //System.out.println("Speakers: " + speaker);
      //Esperar hasta que por lo menos un listener este disponible
      while (listener < 1 || newMsg == true){
        this.currentSpeaker.sleep();
      }

      //Transmite mensaje
      this.msg = currentMsg;
      //Confirma que existe un mensaje disponible
      newMsg = true;
      currentListener.wake();
      speaker -= 1;

      communicationLock.release();

    }

    /**
     * Wait for a thread to speak through this communicator, and then return
     * the <i>word</i> that thread passed to <tt>speak()</tt>.
     *
     * @return	the integer transferred.
     */


    public int listen() {

      int currentMsg;
    	communicationLock.acquire();
    	listener += 1;
      //System.out.println("Listeners: " + listener);
      //Esperar a que un mensaje este disponible
    	while (newMsg == false){
    		currentSpeaker.wake();
    		this.currentListener.sleep();
    	}

    	currentMsg = this.msg;
      // Ya se transmitio el mensaje
    	newMsg = false;
    	listener -= 1;
      currentSpeaker.wake();
    	communicationLock.release();
      // Devuelve el mensaje que escucho
    	return currentMsg;

    }

    private Lock communicationLock;
    private Condition2 currentSpeaker;
    private Condition2 currentListener;
    private int speaker = 0;
    private int listener = 0;
    private int msg;
    private boolean newMsg;



  public static void selfTest() {
		//Lib.debug(dbgThread, "Enter Communicator.selfTest");
    int numPeople = 1;

		Communicator com = new Communicator();


		Runnable listenWord = new Runnable(){
			public void run() {
        for(int i = 0; i <= numPeople; i++) {
					System.out.println("Thread " + KThread.currentThread() + " -- Start/Listening");
					System.out.println("Thread " + KThread.currentThread() + " -- Heard: " + com.listen());
				}

			}
		};


		Runnable speakWord = new Runnable(){
			public void run() {
				for(int i = 0; i <= numPeople; i++) {
					System.out.println("Thread " + KThread.currentThread() + " -- Start/Speaking");
					com.speak(i);
					System.out.println("Thread " + KThread.currentThread() + " -- Said: "+ i);
					System.out.println("Thread " + KThread.currentThread() + " -- Finish/Speaking");
				}
			}
		};

		KThread speak = new KThread(speakWord);
    speak.setName("Speaker1");
    KThread speak2 = new KThread(speakWord);
    speak2.setName("Speaker2");
    KThread speak3 = new KThread(speakWord);
    speak3.setName("Speaker3");
		KThread listen = new KThread(listenWord);
    listen.setName("Listener 1");
		KThread listen2 = new KThread(listenWord);
    listen2.setName("Listener 2");
    KThread listen3 = new KThread(listenWord);
    listen3.setName("Listener 3");

		speak.fork();
    speak2.fork();
    speak3.fork();
		listen.fork();
		listen2.fork();
    listen3.fork();

		speak.join();
    speak2.join();
    speak3.join();
		listen.join();
		listen2.join();
		listen3.join();



}


/*
    public static void selfTest() {
          //Communicator test: prueba lo de los Condition22 blocks
          Communicator com = new Communicator();
          KThread thread1 = new KThread(new Runnable() {
            public void run() {
            System.out.println("Thread 1 -- Start/Speaking");
            com.speak(0);
            System.out.println("Thread 1 -- Finish/Speaking");
         }
       });

          KThread thread2 = new KThread(new Runnable() {
          public void run() {
            System.out.println("Thread 2 -- Start/Listening");
            com.listen();
            System.out.println("Thread 2 -- Finish/Listening");
          }

       });

       KThread thread3 = new KThread(new Runnable() {
         public void run() {
         System.out.println("Thread 3 -- Start/Speaking");
         com.speak(0);
         System.out.println("Thread 3 -- Finish/Speaking");
        }
      });

      KThread thread4 = new KThread(new Runnable() {
        public void run() {
        System.out.println("Thread 4 -- Start/Speaking");
        com.listen();
        System.out.println("Thread 4 -- Finish/Speaking");
     }
   });

       KThread thread5 = new KThread(new Runnable() {
         public void run() {
         System.out.println("Thread 5 -- Start/Speaking");
         com.speak(0);
         System.out.println("Thread 5 -- Finish/Speaking");
      }
    });

      KThread thread6 = new KThread(new Runnable() {
        public void run() {
        System.out.println("Thread 6 -- Start/Speaking");
        com.listen();
        System.out.println("Thread 6 -- Finish/Speaking");
      }
      });

       thread2.fork();
       thread1.fork();

    }
*/
}
