#include <stdio.h>
#include "integer.ph"
#include <unistd.h>

Integer2::Integer2()
{
	printf("Create remote object Integer on %s (od.search fixed)\n", POPSystem::GetHost().c_str());
}

Integer2::~Integer2()
{
	printf("Destroying Integer object...\n");
}

void Integer2::Set(int val)
{
	data=val;
}

int Integer2::Get()
{
	return data;
}

void Integer2::Add(Jinteger &other)
{
	data += other.Get();
}

@pack( Integer2);
