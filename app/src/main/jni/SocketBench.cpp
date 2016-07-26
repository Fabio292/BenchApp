#include <stdio.h>
#include <stdlib.h>
#include <errno.h>
#include <ctype.h>
#include <string.h>
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

#define BUF_SIZE 500
#define PAYLOAD_SIZE 20

#define SERV_IP "192.168.1.20"
#define TERM_STRING "XXXENDXXX\n"

#define START_RATE 5
#define END_RATE 20
#define PACKET_PER_RATE 1

char *prog_name;
int my_socket, packet_sent, current_rate;
char* buf;
struct itimerval it_val;  /* for setting itimer */

void socket_task(void);
void term_handler(void);
void gen_random(char *s, const int len);
void set_rate(int rate, itimerval* timer_s);

int main(int argc, char** argv)
{
	int port,len;
	struct sockaddr_in srv;

	srand(time(NULL));

	buf = (char*)malloc(PAYLOAD_SIZE+1);
	port=29000;
	prog_name = argv[0];
	current_rate=START_RATE;

	myCreateSA_dns(&srv,(char*)SERV_IP,port);
	my_socket = Socket(AF_INET,SOCK_STREAM,0);
	Connect(my_socket,(SA*) &srv , sizeof (struct sockaddr));

	print_SA("Connesso al server:",&srv);
	printf("Buffer: %s\n",buf);

    // SIGNAL HANDLER REGISTRATION
    if (signal(SIGALRM, (void (*)(int))socket_task) == SIG_ERR) {
        perror("Unable to catch SIGALRM");
        exit(1);
    }
    if (signal(SIGINT, (void (*)(int))term_handler) == SIG_ERR) {
        perror("Unable to catch SIGINT");
        exit(1);
    }

    //First packet is discarded to start the delta time calculation
    packet_sent=-1;
    set_rate(current_rate, &it_val);

    while (1)
        pause();

    printf("Kill socket\n");
    Send(my_socket,(void*)"XXX\n",4,0);
    Close(my_socket);

    return 0;
}

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


void socket_task(void) {
    Send(my_socket,buf,PAYLOAD_SIZE,0);
    //printf("Inviati %d byte\n",PAYLOAD_SIZE);
    packet_sent++;

    if(packet_sent == PACKET_PER_RATE){
        current_rate++;
        if(current_rate > END_RATE){
            printf("TERMINATO\n");
            Send(my_socket,(void*)TERM_STRING,strlen(TERM_STRING),0);
            exit(0);
        }

        packet_sent=0;
        set_rate(current_rate, &it_val);
    }
}

void term_handler(void) {
    printf("CTRL+C PRESSED...Kill socket\n");
    Close(my_socket);
}

void set_rate(int rate, itimerval* timer_s){
    int interval = ((int) 1000.0/rate);

    gen_random(buf,PAYLOAD_SIZE);

    timer_s->it_value.tv_sec =     interval/1000;
    timer_s->it_value.tv_usec =    (interval*1000) % 1000000;
    timer_s->it_interval = it_val.it_value;

    if (setitimer(ITIMER_REAL, timer_s, NULL) == -1) {
        perror("error calling setitimer()");
        exit(1);
    }

    printf("CURRENT RATE: %d pps (%d ms) -- %s",rate,interval,buf);
}