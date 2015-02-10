PROJECT_NAME=in
HADOOP_VERSION=1.1.1
HADOOP_HOME=/util/hadoop/hadoop-${HADOOP_VERSION}
HADOOP_JARS=${HADOOP_HOME}/hadoop-core-1.1.1.jar:${HADOOP_HOME}/hadoop-client-1.1.1.jar
SRC=src

all: jar

jar: compile
	jar -cvf ${PROJECT_NAME}.jar *.class
	rm -rf *.class	
compile:
	javac -cp ${HADOOP_JARS} -d . $(SRC)/*

clean:
	rm -f *.class *.jar
