CC = gcc
CFLAGS = -g -Wall -Werror
SRC = tp1.c
SRC2= test.c
OBJ = tp1.o
OBJ2= test.o


all: tp1 test

tp1: $(OBJ)
	$(CC) -o $@ $(OBJ)

test: $(OBJ2)
	$(CC) -o $@ $(OBJ2)

%.o: %.c
	$(CC) $(CFLAGS) -c $<

depend:
	makedepend $(SRC)
	makedepend $(SRC2)
		
clean :
	rm  *.o 
 
