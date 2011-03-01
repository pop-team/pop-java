all:
	popjc -j callback.jar test/Toto.pjava test/Titi.pjava test/CallBackMain.pjava
	popjrun --listlong callback.jar > objmap.xml
run:
	popjrun -c callback.jar objmap.xml test.CallBackMain

clean:
	rm -f *.jar objmap.xml test/*.class
