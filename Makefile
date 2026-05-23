.PHONY: build test clean rebuild

build:
	./gradlew build

test:
	./gradlew test

clean:
	./gradlew clean

rebuild: clean build
