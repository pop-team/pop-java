#ifndef PAROBJECT_PH_
#define PAROBJECT_PH_

parclass PARObject
{
	classuid(1501);

public:
	PARObject() @{ od.power(60, 40); od.search(10, 3, 2); };
	~PARObject();

	sync seq void sendChar(int length, [in, out, size=length] char* tab, int length2, [in, out, size=length] char* tab2);

};

#endif /*PAROBJECT_PH_*/
