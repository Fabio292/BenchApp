#include <stdio.h>
#include <stdlib.h>
#include <errno.h>
#include <ctype.h>
#include <string.h>
#include <unistd.h>
#include <signal.h>
#include <unistd.h>

#include <sys/types.h>
#include <sys/socket.h>
#include <sys/stat.h>
#include <sys/time.h>
#include <arpa/inet.h>
#include <netinet/in.h>

#include <iostream>
#include <string>

#include "lib/errlib.h"
#include "lib/sockwrap.h"

#define TERM_STRING "XXXENDXXX\n"
#define MAX_PAYLOAD_SIZE 4*1024

char *prog_name;
int my_socket;
int packet_sent, current_rate, start_rate, end_rate, packet_per_rate, payload_size;
char buf[MAX_PAYLOAD_SIZE+2];
struct itimerval it_val;  /* for setting itimer */

void socket_task(void);
void term_handler(void);
void gen_random(char *s, const int len);
void set_rate(int rate, itimerval* timer_s);

int main(int argc, char** argv)
{
	int port,len;
	char* server_ip;
	struct sockaddr_in srv;


    //Check arguments
	if(argc != 7){
	    printf("USAGE: IP PORT START_RATE END_RATE PACKET_PER_RATE PAYLOAD_SIZE\n");
	    exit(1);
	}

	srand(time(NULL));
	//buf = (char*)malloc(payload_size+2);

    //Parse arguments
	prog_name = argv[0];
    server_ip=argv[1];
    port=atoi(argv[2]);
    if(port < 1024){
        printf("PORT must be greater than 1024\n");
        exit(2);
    }
    start_rate=atoi(argv[3]);
    end_rate=atoi(argv[4]);
    packet_per_rate=atoi(argv[5]);
    payload_size=atoi(argv[6]);
    if(payload_size >= MAX_PAYLOAD_SIZE){
        printf("PAYLOAD_SIZE must be smaller than 4095\n");
        exit(3);
    }

    current_rate=start_rate;

    //Socket creation
	myCreateSA_dns(&srv,(char*)"192.168.1.20",port);
	my_socket = Socket(AF_INET,SOCK_STREAM,0);
	Connect(my_socket,(SA*) &srv , sizeof (struct sockaddr));

	print_SA("Connected to server:",&srv);


    // SIGNAL HANDLER REGISTRATION
    if (signal(SIGALRM, (void (*)(int))socket_task) == SIG_ERR) {
        perror("Unable to catch SIGALRM");
        exit(1);
    }
    if (signal(SIGINT, (void (*)(int))term_handler) == SIG_ERR) {
        perror("Unable to catch SIGINT");
        exit(1);
    }

    printf("Signal handler correctly registered\n");fflush(stdout);

    //First packet is discarded to start the delta time calculation
    packet_sent=-1;
    set_rate(current_rate, &it_val);

    //Wait
    while (1)
        pause();

    printf("Kill socket\n");
    Send(my_socket,(void*)TERM_STRING,strlen(TERM_STRING),0);
    Close(my_socket);

    return 0;
}

/*
 * Generate random string of given length
 */
void gen_random(char *s, const int len) {
    static const char alphanum[] =
        "0123456789"
        "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
        "abcdefghijklmnopqrstuvwxyz";

    for (int i = 0; i < len - 1; ++i) {
        s[i] = alphanum[rand() % (sizeof(alphanum) - 1)];
    }
    s[len-1]= '\n';
    s[len] = 0;
}

/*
 * Callback invoked from the timer
 */
void socket_task(void) {
    //printf("Invio byte\n");
    Send(my_socket,buf,strlen(buf),0);
    //printf("Inviati %d byte\n",strlen(buf));
    packet_sent++;

    //Check if all packets has been sent
    if(packet_sent == packet_per_rate){
        current_rate++;
        if(current_rate > end_rate){
            printf("LAST\n");
            //Send sequence terminator
            Send(my_socket,(void*)TERM_STRING,strlen(TERM_STRING),0);
            exit(0);
        }
        //Move forward to next transmission rate
        packet_sent=0;
        set_rate(current_rate, &it_val);
    }
}

/*
 * Catch CTRL+C and destroy socket
 */
void term_handler(void) {
    printf("CTRL+C PRESSED...Kill socket\n");fflush(stdout);
    Close(my_socket);
}

/*
 * Set the timer interval according to the transmission rate requested
 */
void set_rate(int rate, itimerval* timer_s){
    int interval = ((int) 1000.0/rate);

    gen_random(buf,payload_size);


    timer_s->it_value.tv_sec =     interval/1000;
    timer_s->it_value.tv_usec =    (interval*1000) % 1000000;
    timer_s->it_interval = it_val.it_value;

    if (setitimer(ITIMER_REAL, timer_s, NULL) == -1) {
        perror("error calling setitimer()");
        exit(1);
    }
    printf("CURRENT RATE: %d pps (%d ms)\n",rate,interval);
}