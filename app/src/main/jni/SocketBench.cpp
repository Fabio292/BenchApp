#include <stdio.h>
#include <stdlib.h>
#include <errno.h>
#include <ctype.h>
#include <string.h>
#include <unistd.h>

#include <sys/types.h>
#include <sys/socket.h>
#include <sys/stat.h>
#include <arpa/inet.h>
#include <netinet/in.h>


#include <iostream>
#include <string>

#include "lib/errlib.h"
#include "lib/sockwrap.h"

#define BUF_SIZE 500

char *prog_name;

int main(int argc, char** argv)
{
	int s,port,len;
	struct sockaddr_in srv;
	char buf[BUF_SIZE];

	port=29000;
	prog_name = argv[0];

	myCreateSA_dns(&srv,"192.168.1.20",port);
	s = Socket (AF_INET,SOCK_STREAM,0);
	Connect(s,(SA*) &srv , sizeof (struct sockaddr));

	print_SA("Connesso al server:",&srv);

	len=sprintf(buf,"ciaone");
	Send(s,buf,len,0);

	Close(s);

    return 0;
}