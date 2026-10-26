TEST_DB ?= jdbc:postgresql://localhost:5432/webhooks_test

.PHONY: build test run clean

build:          ## Build the executable jar (skips tests)
	mvn -B -DskipTests package

test:           ## Run the current ingestion tests
	mvn -B test

run: build      ## Run against DATABASE_URL (default jdbc:postgresql://localhost:5432/webhooks)
	java -jar target/webhook-delivery-0.1.0.jar

clean:
	mvn -B clean
