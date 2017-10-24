#include <stdio.h>
#include <stdlib.h>
#include <sys/ptrace.h>
#include <sys/types.h>
#include <unistd.h>
#include <sys/wait.h>
#include <errno.h>


int main(int argc, char* argv[]){
	
	
	char cmd[512];
	FILE *pGrep;
	FILE *proc;
	char npid[512];
	int num_pid;
	long attache;
	char cmd2[512];
	FILE *nm;
	FILE *adrMemoir;
	long unsigned adrMem;
	char str1[512];
	char str2[512];
	FILE *chemin;
	FILE *file;
	char stop ={0xCC};


	if (argv[1]== NULL || argv[2] == NULL){
		printf("Il faut mettre deux parametre, programme et fonction \n");
		return -1;
	}

	/////////////////////Parti de recuperer le pid
	printf("programme %s \n",argv[1]);
	snprintf(cmd,sizeof("pgrep  > proc.txt")+sizeof(argv[1]), "pgrep %s > proc.txt", argv[1]);
	
	
	if(!(pGrep= popen(cmd,"r"))){
		perror("popen erreur \n");
	}

	//recupere le pid avec fopen sinon si juste system c'est le code de retour de system que l'on recupere
	proc= fopen("proc.txt","r");

	if (proc == NULL){
		perror("fopen erreur pour proc.txt\n");
		return -1;
	}
	

	if(fread(npid,sizeof(char),16, proc)==0){
		perror("fread erreur pid \n");	
		return -1;	
	}
	
	num_pid= atoi(npid);
	if(num_pid <0){
		perror("Erreur pgrep pour le processus demander \n");
		return -1;
	}

	printf("le numero du proc est :%d \n",num_pid);

	///////////////////////////////Attache le processus
	attache=ptrace(PTRACE_ATTACH, num_pid,0,0);
	if(attache < 0){
		perror("Erreur processus non attaché\n");
		return -1;
	}
	
	//on attend que ca finisse avec waitpid
	wait(&num_pid);
	

	/////////////////////////////////// PARTI DES ADRESSE MEMOIRE
	

	printf("on prends l'adresse memoire de la fonction: %s du programme %s  \n",argv[2],argv[1]);
	snprintf(cmd2,sizeof("nm  | grep  > adresseMemoire.txt   ")+sizeof(argv[1])+sizeof(argv[2]),"nm %s | grep %s > adresseMemoire.txt", argv[1],argv[2]);
	
	if(!(nm= popen(cmd2,"r"))){
		perror("popen erreur \n");
		return -1;
	}
	adrMemoir= fopen("adresseMemoire.txt","r");

	if (adrMemoir == NULL){
		perror("fopen erreur adresseMemoire.txt\n");
		return -1;
	}

	fscanf(adrMemoir,"%lx %s %s", &adrMem,str1,str2);//adrmem contient l'adresse memoire
	printf("l'adresse est : %lx \n", adrMem);


	///////////////////////////////Suite
	
	snprintf(chemin,sizeof("/proc//mem")+sizeof(num_pid),"/proc/%d/mem",num_pid);
	
	file = fopen(chemin,"w");
	if (file == NULL){
		perror("fopen erreur pour ouverture du file /proc/pid/mem \n");
		return -1;
	}
	
	
	if(fseek(file,adrMem,SEEK_SET)<0){
		perror("fseek erreur\n");
		return -1;	
	}

	if(fwrite(stop,1,1,file)){
		perror("fwrite erreur\n");
		return -1;
	}

	printf("succes ?");
	//on ferme
	if(fclose(pGrep)<0){
		perror("erreur sur la fermeture de pGrep");
		return -1;
	}
	if(fclose(proc)<0){
		perror("erreur sur la fermeture de proc");
		return -1;
	}
	
	if(fclose(nm)<0){
		perror("erreur sur la fermeture de nm");
		return -1;
	}
	if(fclose(adrMemoir)<0){
		perror("erreur sur la fermeture de adrmemoir");
		return -1;
	}
	
	if(fclose(chemin)<0){
		perror("erreur sur la fermeture de chemin");
		return -1;
	}
	if(fclose(file)<0){
		perror("erreur sur la fermeture de file");
		return -1;
	}
	

	return 0;
}
