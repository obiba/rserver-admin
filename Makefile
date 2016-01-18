skipTests = false
version = 1.2-SNAPSHOT
current_dir = $(shell pwd)
mvn_exec = mvn -Dmaven.test.skip=${skipTests}

all: clean install

clean:
	${mvn_exec} clean

install:
	${mvn_exec} install

launch:
	export RSERVER_HOME=$(shell pwd)/target/rserver-admin-${version}-dist/rserver-admin-${version} && \
	cd target/rserver-admin-${version}-dist/rserver-admin-${version} && \
	chmod +x ./bin/rserver-admin && \
	./bin/rserver-admin

launch-debug:
	export RSERVER_HOME=$(shell pwd)/build/work/rserver-admin-${version} && \
	export JAVA_OPTS=-agentlib:jdwp=transport=dt_socket,server=y,address=8000,suspend=n && \
	cd build/work/rserver-admin-${version} && \
	chmod +x ./bin/rserver-admin && \
	./bin/rserver-admin

deb:
	mvn clean install -Pci-build

log:
	tail -f build/work/rserver-admin-${version}/logs/rserver-admin.log

test: status stop start

status:
	curl localhost:6312/rserver
	@echo
	@echo

start:
	curl -X PUT localhost:6312/rserver
	@echo
	@echo

stop:
	curl -X DELETE localhost:6312/rserver
	@echo
	@echo

