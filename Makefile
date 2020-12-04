skipTests = false
version = 2.0-SNAPSHOT
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
	chmod +x ./bin/rserver && \
	./bin/rserver

debug:
	export RSERVER_HOME=$(shell pwd)/target/rserver-admin-${version}-dist/rserver-admin-${version} && \
	export JAVA_OPTS=-agentlib:jdwp=transport=dt_socket,server=y,address=8003,suspend=n && \
	cd target/rserver-admin-${version}-dist/rserver-admin-${version} && \
	chmod +x ./bin/rserver && \
	./bin/rserver

deb:
	mvn clean install -Pci-build

log:
	tail -f target/rserver-admin-${version}-dist/rserver-admin-${version}/logs/rserver-admin.log

rlog:
	tail -f target/rserver-admin-${version}-dist/rserver-admin-${version}/logs/Rserve.log

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

new-session:
	curl -v -X POST localhost:6312/r/sessions -H 'Content-Type:application/json'

get-session:
	curl -v -X GET localhost:6312/r/session/${id} -H 'Content-Type:application/json'

delete-session:
	curl -v -X DELETE localhost:6312/r/session/${id} -H 'Content-Type:application/json'