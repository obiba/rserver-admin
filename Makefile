version=1.0-SNAPSHOT

all:
	./gradlew clean build

clean:
	./gradlew clean

launch:
	./gradlew distUnzipped
	cd build/work/rserver-admin-${version} && chmod +x ./bin/rserver-admin && ./bin/rserver-admin

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