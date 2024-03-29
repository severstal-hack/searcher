build:
	.\mvnw clean
	.\mvnw package

up:
	make build
	make down
	docker-compose build
	docker-compose up -d

down:
	docker-compose down

