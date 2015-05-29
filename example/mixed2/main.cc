#include "integer.ph"
#include <iostream>

using namespace std;

int main(int argc, char **argv)
{
	try
	{
		// Create 2 Integer objects
		Integer o1;
		Integer o2;
		int i = 1;
		int i2 = 2;
		// Set values
		o1.Set(i); o2.Set(i2);
		printf("o1=%d; o2=%d\n", o1.Get(), o2.Get());
		printf("Add o2 to o1\n");
		o1.Add(o2);
		printf("o1=o1+o2; o1=%d\n", o1.Get());

	} catch (POPException &e)
	{
		printf("Exception occurs in application : %s\n", e.what());
		return -1;
	}

	return 0;
}
