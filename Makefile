version = 1.0-SNAPSHOT
current_dir = $(shell pwd)

all:
	./gradlew clean build

clean:
	./gradlew clean

launch:
	./gradlew distUnzipped
	export RSERVER_HOME=${current_dir}/build/work/rserver-admin-${version} && \
	cd build/work/rserver-admin-${version} && \
	chmod +x ./bin/rserver-admin && \
	./bin/rserver-admin

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

