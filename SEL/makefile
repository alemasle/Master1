CC = gcc
CFLAGS = -g -Wall -Werror
SRC = autreFct.c
OBJ = autreFct.o

autre: $(OBJ)
	$(CC) -o $@ $(OBJ)

%.o: %.c
	$(CC) $(CFLAGS) -c $<

depend:
	makedepend $(SRC)
		
clean :
	rm  *.o 
 
