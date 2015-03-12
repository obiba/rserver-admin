version = 1.2-SNAPSHOT
current_dir = $(shell pwd)

all:
	./gradlew clean build

clean:
	./gradlew clean

launch:
	./gradlew distUnzipped
	export RSERVER_HOME=$(shell pwd)/build/work/rserver-admin-${version} && \
	cd build/work/rserver-admin-${version} && \
	chmod +x ./bin/rserver-admin && \
	./bin/rserver-admin

launch-debug:
	./gradlew distUnzipped
	export RSERVER_HOME=$(shell pwd)/build/work/rserver-admin-${version} && \
	export JAVA_OPTS=-agentlib:jdwp=transport=dt_socket,server=y,address=8000,suspend=n && \
	cd build/work/rserver-admin-${version} && \
	chmod +x ./bin/rserver-admin && \
	./bin/rserver-admin

deb:
	./gradlew distDeb

log:
	tail -f build/work/rserver-admin-${version}/logs/rserver-admin.log

dependencyUpdates:
	./gradlew dependencyUpdates -Drevision=release

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

