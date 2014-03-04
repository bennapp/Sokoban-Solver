JFLAGS = -g
JC = javac
.SUFFIXES: .java .class
.java.class:
	$(JC) $(JFLAGS) $*.java

CLASSES = \
	Map.java \
	MapATargetsLeftComparator.java \
	MapCostComparator.java \
	MapANetBoxCostComparator.java \
	Solution.java \
	Coord.java \
	SokobanAgent.java

default: classes

classes: $(CLASSES:.java=.class)

clean:
	$(RM) *.class