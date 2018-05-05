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
		    currentSpeaker = new Condition(communicationLock);
		    currentListener = new Condition(communicationLock);
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
    private Condition currentSpeaker;
    private Condition currentListener;
    private int speaker = 0;
    private int listener = 0;
    private int msg;
    private boolean newMsg;

/*
    public static void selfTest() {
          //Communicator test: prueba lo de los Condition2 blocks
          Lib.debug(dbgThread, "Enter Communicator.selfTest");
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

    }  */

  public static void selfTest() {
		Communicator com = new Communicator();

		Runnable listenWord = new Runnable(){
			public void run() {
				System.out.println("Listener -- Start/Listening");
				System.out.println("Listener -- Heard: " +com.listen());
			}
		};

		Runnable speakWord = new Runnable(){
			public void run() {

				for(int i = 0; i < 5; i++) {
					System.out.println("Speaker -- Start/Speaking");
					com.speak(i);
					System.out.println("Speaker -- Said: "+ i);
					System.out.println("speaker -- Finish/Speaking");
				}
			}
		};

		KThread speak1 = new KThread(speakWord);
		KThread listen1 = new KThread(listenWord);
		KThread listen2 = new KThread(listenWord);
		KThread listen3 = new KThread(listenWord);
		KThread listen4 = new KThread(listenWord);
		KThread listen5 = new KThread(listenWord);
		speak1.fork();
		listen1.fork();
		listen2.fork();
		listen3.fork();
		listen4.fork();
		listen5.fork();


		speak1.join();
		listen1.join();
		listen2.join();
		listen3.join();
		listen4.join();
		listen5.join();

	}

}
