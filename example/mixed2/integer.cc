# include <stdio.h>
# include "integer.ph"
# include <unistd.h>

Integer::Integer(){
	printf (" Create remote object Integer on %s\n",
	( const char *) POPSystem :: GetHost ());
}

Integer::~Integer(){
	printf("Destroying Integer object ...\n");
}
void Integer::Set(int val) {
	data = val;
}
int Integer::Get(){
	return data;
}
void Integer::Add(Integer &other) {
	data += other.Get();
}
@pack ( Integer );

