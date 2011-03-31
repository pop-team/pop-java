
#ifndef _INTEGER2_PH
#define _INTEGER2_PH

parclass Jinteger
{
	classuid(1001);

public:	
	Jinteger() @{ od.search(10, 0,0); };
	~Jinteger();

	conc int Get();
	seq async void Set(int val);



private:
	int data;

};

#endif
