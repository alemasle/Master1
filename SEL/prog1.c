#include <stdio.h>
#include <stdlib.h>
#include <sys/ptrace.h>

int fct(int x, int y){
	return x*y;
}

void main() {
 	int c;
	int a,b;
	a = 4;
	b= 57416558;
	do {
	    c = getchar();
	    putchar(c);
	    putchar(c);
	  } while(c!='%');
	printf("\n");
	printf("%d",fct(a,b));
	printf("\n");
}
