# include <stdio.h>
# include "integer.ph"
# include <unistd.h>

Integer::Integer(){
    printf (" Create remote object Integer on %s\n",POPSystem :: GetHost ().c_str());
}

Integer::~Integer(){
    printf("Destroying Integer object ...\n");
}

void Integer::Add(Integer &other) {
    data += other.Get();
}

int Integer::Get(){
    return data;
}

void Integer::Set(int val) {
    data = val;
}


@pack ( Integer );


