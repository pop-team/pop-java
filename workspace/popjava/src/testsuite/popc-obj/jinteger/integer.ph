#include "integer2.ph"
#ifndef _INTEGER_PH
#define _INTEGER_PH

parclass Integer2
{
	classuid(1000);

public:	
	Integer2();
	~Integer2();

	mutex void Add(Jinteger &other);
	conc int Get();
	seq async void Set(int val);



private:
	int data;

};

#endif
