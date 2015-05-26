#include "parobj.ph"
#include <unistd.h>

PARObject::PARObject()
{
   cout << "PARObject created (by JobMgr) on machine:" << POPSystem::GetHost().c_str() << popcendl;
}


PARObject::~PARObject()
{
   cout << "PARObject on machine:" << POPSystem::GetHost().c_str() <<" is being destroyed\n" << popcendl;
}

void PARObject::sendChar(int length, char* tab){
   cout << "Char Array Length = " << length << " Char Array content = " << tab << popcendl;
   tab[4] =  'C';
   tab[5] =  '+';
   tab[6] =  '+';
   tab[7] =  ':';
}


@pack(PARObject);
