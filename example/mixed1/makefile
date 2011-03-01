all:
	popcc -object -o integer.obj integer.ph integer.cc
	popjc -p additional-infos.xml TestInteger.pjava Integer.pjava
	popjrun --listlong integer.obj > objmap.xml
	
run:
	popjrun objmap.xml TestInteger

clean:
	rm *.o *.obj *.class objmap.xml
