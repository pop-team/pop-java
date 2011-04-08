all: app object pjava

app:

	popcc -o main integer.ph integer.cc main.cc

object:
	popcc -object -o integer.obj integer.cc integer.ph main.cc

	@echo "Integer *-* "${POPJAVA_JAVA}" -cp "${POPJAVA_LOCATION}" popjava.broker.Broker -codelocation="${PWD}" -actualobject=Integer" > obj.map

pjava:
	popjc -n -p additional-infos.xml Integer.pjava

clean:
	rm -f *.o *.obj main obj.map *.class

run:
	popcrun obj.map ./main

