all:
	popjc -p additional-infos.xml CharTestMain.pjava PARObject.pjava 
	popcc -object -o parobj.obj parobj.cc parobj.ph
	popjrun -l parobj.obj > objectmap.xml 

clean:
	rm -f *.o *.obj main obj.map *.class objectmap.xml

run:
	popjrun objectmap.xml CharTestMain
