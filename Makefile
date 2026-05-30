.PHONY: build test connected-test clean rebuild

build:
	gradlew build

test:
	gradlew test

connected-test:
	gradlew connectedAndroidTest

clean:
	gradlew clean

rebuild: clean build
