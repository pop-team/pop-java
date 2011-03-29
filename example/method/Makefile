all:
	popjc MethodObj.pjava MainClass.pjava
	popjrun --listlong MethodObj.class > objectmap.xml

run:
	popjrun objectmap.xml MainClass

clean:
	rm -f MainClass.class MethodObj.class objectmap.xml
