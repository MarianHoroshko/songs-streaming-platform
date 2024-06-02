package main

import (
	"log"
	"net/http"

	"github.com/gofiber/fiber/v2"
	"github.com/gofiber/fiber/v2/middleware/cors"
	"github.com/gofiber/fiber/v2/middleware/filesystem"

	"spotify_clone.com/songs_service/src/api"
	"spotify_clone.com/songs_service/src/config"
)

// TODO: to .env file
const (
	BASE_PATH  = "/api/v1"
	SONGS_PATH = "static/"
)

func main() {
	// create fiber app
	app := fiber.New()
	log.Println("[main:main] Fiber app successfully created.")

	// connect to db
	session, err := config.SetupDBConnection("127.0.0.1", "song_platform")
	if err != nil {
		log.Panic(err)

		panic(err)
	}
	log.Println("[main:main] Connected to db successfully.")

	// close db connection when program stops
	defer session.Close()

	// connect to consul

	// middlewares
	// allow cors
	app.Use(cors.New())

	// serve static songs files
	app.Use(BASE_PATH+"/song", filesystem.New(filesystem.Config{
		Root: http.Dir(SONGS_PATH),
	}))

	// routes
	// register routes
	router := api.NewRouter(session)
	router.RegisterRoutes(app, BASE_PATH)

	// start server
	if err := app.Listen(":3000"); err != nil {
		log.Fatal(err)

		panic(err)
	}
}
