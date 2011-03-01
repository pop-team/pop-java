all:
	popjc *.pjava
	popjrun --listlong MyObj1.class:MyObj2.class:MyObj3.class:MyObj4.class > objectmap.xml

run:
	popjrun objectmap.xml MultiObj

clean:
	rm *.class *.xml
