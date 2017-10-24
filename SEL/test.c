#include <stdio.h>
#include <stdlib.h>
#include <sys/wait.h>

int fonction(int i){


while (1){
	printf("%d \n",i);
	i++;
sleep(1);
}
}


int main(){
int i=0;
fonction(i);
}
