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
		communicationLock = new Lock();
		currentSpeaker = new Condition2(communicationLock);
		currentListener = new Condition2(communicationLock);
		wordReady = false;
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
	public void speak(int word) {
		System.out.println("Speaker initializing");
		//El speaker obtiene el candado
		communicationLock.acquire();
		//Usamos este into para indicar que hay un speaker
		speaker += 1;
		while (listener < 1 || wordReady){
			//Si no exist un speaker o no hay un mensaje el thread se va a dormir
			System.out.println("No word or no listeners, speaker going to sleep");
			this.currentSpeaker.sleep();
		}
		System.out.println("Listener found or word ready");
		//Guardamos el parametro dentro de la variable msg
		this.msg = word;
		//Indicamos que existe un mensaje
		wordReady = true;
		//Despertamos a los listeners
		System.out.println("Word incoming, Waking up listener");
		currentListener.wake();
		//Volvemos a reducir el numero de speaker por 1
		speaker -= 1;
		//Soltamos el lock
		System.out.println("Speaker has finished");
		communicationLock.release();
	}

	/**
	 * Wait for a thread to speak through this communicator, and then return
	 * the <i>word</i> that thread passed to <tt>speak()</tt>.
	 *
	 * @return	the integer transferred.
	 */
	public int listen() {
		int word;
		//Obtenemos el lock
		communicationLock.acquire();
		listener += 1;
		//System.out.println("Listener initializing");
		//System.out.println(!wordReady);
		while (wordReady == false){
			//System.out.println("The word is not ready going to send the listener to sleep and waking speaker");
			//Si el mensaje esta listo despertamos al speaker y mandamos a dormir al listener
			//System.out.println("Speaker waking up, to ready up word");
			currentSpeaker.wake();
			//System.out.println("The speaker is about to speak, listener going to sleep");
			this.currentListener.sleep();
		}
		word = this.msg;
		wordReady = false;
		listener -= 1;
		currentSpeaker.wake();
		communicationLock.release();
		//System.out.println("The word has been heard, listener finishing.");
		return word;
	}

	private Lock communicationLock;
	private Condition2 currentSpeaker;
	private Condition2 currentListener;
	private int speaker = 0;
	private int listener = 0;
	private int msg;
	private boolean wordReady;


	public static void selfTest(){

		final Communicator com = new Communicator();


		Runnable listenWord = new Runnable(){
			public void run() {
				System.out.println("Thread -- Start/Listening");

				System.out.println("Thread -- Heard: " +com.listen());
			}
		};


		Runnable speakWord = new Runnable(){
			public void run() {

				for(int i = 1; i <= 5; i++) {
					System.out.println("SPeaker -- Start/Speaking");
					com.speak(i);
					System.out.println("Speaker -- Said: "+ i);
					System.out.println("speaker -- Finish/Speaking");
				}
			}
		};

		KThread speak = new KThread(speakWord);

		KThread listen = new KThread(listenWord);
		KThread listen2 = new KThread(listenWord);
		KThread listen3 = new KThread(listenWord);
		KThread listen4 = new KThread(listenWord);
		KThread listen5 = new KThread(listenWord);

		speak.fork();
		listen.fork();
		listen2.fork();
		listen3.fork();
		listen4.fork();
		listen5.fork();


		speak.join();
		listen.join();
		listen2.join();
		listen3.join();
		listen4.join();
		listen5.join();

	}

}
