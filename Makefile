all:
	./gradlew clean build

clean:
	./gradlew clean

launch:
	export RSERVER_HOME=build && java -jar build/libs/rserver-admin-0.1.0.jar

test: status stop start

status:
	curl localhost:9000/rserver/status
	@echo
	@echo

start:
	curl -X PUT localhost:9000/rserver
	@echo
	@echo
	curl localhost:9000/rserver/status
	@echo
	@echo

stop:
	curl -X DELETE localhost:9000/rserver
	@echo
	@echo
	curl localhost:9000/rserver/status
	@echo
	@echo