#include <stdio.h>
#include "integer2.ph"
#include <unistd.h>
Jinteger::Jinteger()
{
	printf("Create remote object Integer on %s (od.search fixed)\n",
		   (const char *)POPSystem::GetHost());
}

Jinteger::~Jinteger()
{
	printf("Destroying Integer object...\n");
}

void Jinteger::Set(int val)
{
	data=val;
}

int Jinteger::Get()
{
	return data;
}

@pack( Jinteger);
