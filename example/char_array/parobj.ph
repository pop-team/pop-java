#ifndef PAROBJECT_PH_
#define PAROBJECT_PH_

parclass PARObject
{
	classuid(1501);

public:
	PARObject() @{ od.power(60, 40); od.search(10, 3, 2); };
	~PARObject();

	sync seq void sendChar(int length, [size=length]char* tab);

};

#endif /*PAROBJECT_PH_*/
