#include <stdio.h>
#include <stdlib.h>
#include <sys/ptrace.h>
#include <sys/types.h>
#include <unistd.h>
#include <sys/wait.h>
#include <errno.h>


int main(int argc, char* argv[]){
	
	int num_pid;
	long attache;
	 
	FILE *mem;
	char str[512];
	FILE *proc;
	char npid[16];

	if (argv[1]== NULL){
		printf("Il faut mettre un parametre \n");
	}
	printf("programme %s \n",argv[1]);
	snprintf(str,sizeof("pgrep > proc.txt ")+sizeof(argv[1])-1, "pgrep %s > proc.txt", argv[1]);

	
	
	//recupere le pid avec popen sinon si juste system c'est le code de retour de system que l'on recupere
	
	
	if(!(mem= popen(str,"r"))){
		perror("popen erreur \n");
	}



	proc= fopen("proc.txt","r");

	if (proc ==NULL){
		perror("fopen erreur \n");
	}
	

	if(fread(npid,sizeof(char),15, proc)==0){
		perror("fread erreur\n");	
	}
	
	num_pid= atoi(npid);
	if(num_pid <0){
		perror("Erreur pgrep pour le processus demander \n");
		return -1;
	}

	printf("%d \n",num_pid);

	//attrape le processus
	attache=ptrace(PTRACE_ATTACH, num_pid,0,0);
	if(attache < 0){
		perror("Erreur processus non attaché\n");
		return -1;
	}
	
	//on attend que ca finisse avec waitpid
	wait(&num_pid);
	

	//on balance le trap
	//penser à proc/pid/mem pour acceder à la mémoire du processus avec open, read et lseek

	

	//convertit le début de la chaîne en une valeur de type unsigned long int en fonction de l'argument base

	//on ferme
   	if(fclose(mem)<0){
		perror("erreur sur la fermeture de men");
	}
	if(fclose(proc)<0){
		perror("erreur sur la fermeture de proc");
	}

		

	return 0;
	
}







