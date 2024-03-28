build:
	.\mvnw clean
	.\mvnw package

up:
	make build
	docker-compose down
	docker-compose build
	docker-compose up -d

