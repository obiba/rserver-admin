all:
	./gradlew clean build

clean:
	./gradlew clean

launch:
	./gradlew build
	export RSERVER_HOME=build && java -jar build/libs/rserver-admin-0.1.0.jar

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