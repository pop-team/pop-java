all:
	popjc DemoMain.pjava DemoPOP.pjava
	popjrun --listlong DemoPOP.class > objectmap.xml

run:
	popjrun objectmap.xml DemoMain 3

clean:
	rm -f DemoPOP.class DemoMain.class objectmap.xml
