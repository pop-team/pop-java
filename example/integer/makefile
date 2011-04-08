all:
	popjc Integer.pjava TestInteger.pjava
	popjrun --listlong Integer.class > objectmap.xml

run:
	popjrun objectmap.xml TestInteger

clean:
	rm -f Integer.class TestInteger.class objectmap.xml
