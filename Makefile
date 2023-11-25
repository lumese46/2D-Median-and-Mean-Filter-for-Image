# Parral programing make
# Oratile Rapoo

JAVAC=/usr/bin/javac
.SUFFIXES: .java .class

.java.class:
	$(JAVAC) $<

classes: MeanFilterSerial.class MedianFilterSerial.class MeanFilterParallel.class MedianFilterParalle.class 
default: $(CLASSES)


clean:
	rm*.class