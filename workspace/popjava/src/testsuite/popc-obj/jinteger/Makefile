all: app object

app:
	popcc -parclass-nobroker -c integer2.ph
	popcc -o main integer2.stub.o integer.ph integer.cc main.cc

object:
	popcc -parclass-nobroker -c integer2.ph
	popcc -object -o integer.obj integer2.stub.o integer.cc integer.ph
	popcc -object -o integer2.obj integer2.cc integer2.ph
	./integer.obj -listlong > obj.map
	./integer2.obj -listlong >> obj.map

clean:
	rm -f *.o *.obj main obj.map

run:
	popcrun obj.map ./main

