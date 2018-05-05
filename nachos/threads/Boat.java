package nachos.threads;
import nachos.machine.*;
import java.util.*;
import nachos.ag.BoatGrader;

public class Boat
{
    static BoatGrader bg;

    public static void selfTest(int a, int c)
    {
    	BoatGrader b = new BoatGrader();

    	System.out.println("\n ***Testing Boats with " + c + " children and " + a + " adults***");
    	begin(a, c, b);

    }

    public static void begin( int adults, int children, BoatGrader b )
    {
      Lib.assertTrue(children >= 2);
    	// Store the externally generated autograder in a class
    	// variable to be accessible by children.
    	bg = b;

    	// Instantiate global variables here
      people = adults + children;
      maxAdults = adults;
      maxChildren = children;
      //Todos empiezan en Oahu
      boat = new Lock();
      adultsOnOahu = adults;
      childrenOnOahu = children;
      boatLocation = "Oahu";
      //Nadie en Molokai
      adultsOnMolokai = 0;
      childrenOnMolokai = 0;
      adultOnOahu = new Condition2(boat);
      childOnOahu = new Condition2(boat);
      childOnMolokai = new Condition2(boat);
      everyoneInMolokai = new Condition2(boat);

    	// Create threads here. See section 3.4 of the Nachos for Java
    	// Walkthrough linked from the projects page.
      Runnable createChild = new Runnable() {
            public void run() {
                ChildItinerary();
            }
      };

      Runnable createAdult = new Runnable() {
          public void run() {
              AdultItinerary();
          }
      };

      for(int i = 1; i <= maxChildren; i++) {
        KThread Child = new KThread(createChild);
        Child.setName("Child " + i).fork();
      };

      for(int i = 1; i <= maxAdults; i++) {
        KThread Adult = new KThread(createAdult);
        Adult.setName("Adult " + i).fork();
      };

      boat.acquire();

      while ((childrenOnMolokai + adultsOnMolokai != people) || (boatLocation != "Molokai"))
      {
        everyoneInMolokai.sleep();
      };

      boat.release();
      System.out.println("Boat finished on " + boatLocation + "!");
      System.out.println("childrenOnOahu = " + childrenOnOahu);
      System.out.println("adultsOnOahu = " + adultsOnOahu);
      System.out.println("childrenOnMolokai = " + childrenOnMolokai);
      System.out.println("adultsOnMolokai = " + adultsOnMolokai);

    }

    static void AdultItinerary()
    {
      boat.acquire();

      while (boatLocation.equals("Molokai") || childrenOnOahu != 1){
           adultOnOahu.sleep();
      }

      adultsOnOahu -= 1;
      System.out.println(KThread.currentThread().getName() + " rowing to Molokai");
      //bg.AdultRowToMolokai();
      boatLocation = "Molokai";
      adultsOnMolokai += 1;
      childOnMolokai.wake();

      boat.release();

    }

    static void ChildItinerary()
    {
      boat.acquire();
      //Mientras no esten todos en Molokai
      while (people > (childrenOnMolokai + adultsOnMolokai)){
        if (boatLocation.equals("Molokai")){
          childrenOnMolokai -= 1;
          System.out.println(KThread.currentThread().getName() + " rowing to Oahu");
          //bg.ChildRowToOahu();
          boatLocation = "Oahu";
          childrenOnOahu += 1;

        }
        //Si el barco esta en Oahu y 2 o mas ninos en Oahu
        else if (boatLocation.equals("Oahu") && childrenOnOahu >= 2) {
          if (Passenger.equals("None")) {
            Passenger = "onBoat";
            //bg.ChildRideToMolokai();
            System.out.println(KThread.currentThread().getName() + " riding to Molokai");
            childOnOahu.wake();
            childOnMolokai.sleep();
          }
          else {
            childrenOnOahu -= 2;
            System.out.println(KThread.currentThread().getName() + " rowing to Molokai");
            //bg.ChildRowToMolokai();
            Passenger = "None";
            boatLocation = "Molokai";
            childrenOnMolokai += 2;
            //Validamos si ya todos estan en molokai
            everyoneInMolokai.wake();
          }
        }
        else {
          adultOnOahu.wake();
          childOnOahu.sleep();
        }
      }

      boat.release();
    }

    static void SampleItinerary()
    {
    	// Please note that this isn't a valid solution (you can't fit
    	// all of them on the boat). Please also note that you may not
    	// have a single thread calculate a solution and then just play
    	// it back at the autograder -- you will be caught.
    	System.out.println("\n ***Everyone piles on the boat and goes to Molokai***");
    	bg.AdultRowToMolokai();
    	bg.ChildRideToMolokai();
    	bg.AdultRideToMolokai();
    	bg.ChildRideToMolokai();
    }

    static int maxAdults;
    static int maxChildren;
    static int people;
    static boolean onMolokai = false;
    static int adultsOnOahu;
    static int childrenOnOahu;
    static int adultsOnMolokai;
    static int childrenOnMolokai;
    static String boatLocation;
    static String Passenger = "None";
    static Lock boat;
    static Condition2 childOnOahu;
    static Condition2 adultOnOahu;
    static Condition2 sleepAdultMolokai;
    static Condition2 childOnMolokai;
    static Condition2 everyoneInMolokai;

}
