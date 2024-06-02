package main

import (
	"log"
	"net/http"

	"github.com/gofiber/fiber/v2"
	"github.com/gofiber/fiber/v2/middleware/cors"
	"github.com/gofiber/fiber/v2/middleware/filesystem"

	api "spotify_clone.com/songs_service/src/api"
)

// TODO: to .env file
const (
	BASE_PATH  = "/api/v1"
	SONGS_PATH = "static/"
)

func main() {
	// connect to consul

	// connect to db

	// create fiber app
	app := fiber.New()

	// middlewares
	// allow cors
	app.Use(cors.New())

	// serve static songs files
	app.Use(BASE_PATH+"/song", filesystem.New(filesystem.Config{
		Root: http.Dir(SONGS_PATH),
	}))

	// routes
	// register routes
	api.RegisterRoutes(app, BASE_PATH)

	// start server
	if err := app.Listen(":3000"); err != nil {
		log.Fatal(err)
	}
}
