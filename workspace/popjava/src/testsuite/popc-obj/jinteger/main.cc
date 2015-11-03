


#include "integer.ph"
#include "integer2.ph"
#include <iostream>

using namespace std;

int main(int argc, char **argv)
{
	try
	{
		// Create 2 Integer objects
		Integer2 i1;
		Jinteger j1;

		// Set values
		i1.Set(1); j1.Set(2);

		printf("i1=%d, j1=%d\n", i1.Get(), j1.Get());

		printf("Add j1 to i1\n");
		
		i1.Add(j1);
		
		printf("j1+i1=%d", i1.Get()); 

	} // Try

	catch (POPException &e)
	{
		printf("Exception occurs in application : %s\n", e.what());
		return -1;
	} // catch

	return 0;
} //main
