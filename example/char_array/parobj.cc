#include "parobj.ph"
#include <unistd.h>

PARObject::PARObject()
{
   cout << "PARObject created (by JobMgr) on machine:" << (const char*)POPSystem::GetHost() << popcendl;
}


PARObject::~PARObject()
{
   cout << "PARObject on machine:" << (const char*)POPSystem::GetHost() <<" is being destroyed\n" << popcendl;
}

void PARObject::sendChar(int length, char* tab){
   cout << "Char Array Length = " << length << " Char Array content = " << tab << popcendl;
   tab[4] =  'C';
   tab[5] =  '+';
   tab[6] =  '+';
   tab[7] =  ':';
}


@pack(PARObject);
