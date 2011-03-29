all:
	popjc -j od.jar ParObj.pjava MainClass.pjava
	popjrun --listlong od.jar > objectmap.xml

run:
	popjrun -c od.jar objectmap.xml MainClass

clean:
	rm -f od.jar objectmap.xml
