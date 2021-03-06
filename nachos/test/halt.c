/* halt.c
 *	Simple program to test whether running a user program works.
 *
 *	Just do a "syscall" that shuts down the OS.
 *
 * 	NOTE: for some reason, user programs with global data structures
 *	sometimes haven't worked in the Nachos environment.  So be careful
 *	out there!  One option is to allocate data structures as
 * 	automatics within a procedure, but if you do this, you have to
 *	be careful to allocate a big enough stack to hold the automatics!
 */

#include "syscall.h"

int
main()
{
    char *test = "Test.txt";
    creat(test);


    open(test);

    //Write
    /*
    char *escribir = "Hello";
    void *buffer[1];
    int openFile = open(test);
    int writeFile = write(openFile, escribir, strlen(escribir));
    int readFile = read(openFile, escribir, strlen(escribir));
    */

    //Read

    void *buffer[1];
    int read1 = open(test);
    int read2 = read(read1,buffer,11);
    printf("Contenido: %s\n", buffer);

    halt();
    /* not reached */

}
