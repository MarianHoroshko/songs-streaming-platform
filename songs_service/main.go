package main

import (
	"log"
	"net/http"

	"github.com/gofiber/fiber/v2"
	"github.com/gofiber/fiber/v2/middleware/cors"
	"github.com/gofiber/fiber/v2/middleware/filesystem"
	"github.com/joho/godotenv"

	"spotify_clone.com/songs_service/src/api"
	"spotify_clone.com/songs_service/src/config"
	"spotify_clone.com/songs_service/src/utils"
)

// TODO: to .env file
const (
	BASE_PATH  = "/api/v1"
	SONGS_PATH = "static/"
)

func main() {
	// load .env file
	if err := godotenv.Load(); err != nil {
		log.Panic(err)

		panic(err)
	}
	log.Println("[main:main] Successfully loaded environment variables file.")

	// create fiber app
	app := fiber.New()
	log.Println("[main:main] Fiber app successfully created.")

	// connect to db
	dbURL := utils.GetEnvVariable("DB_URL", "localhost")
	dbKeybinding := utils.GetEnvVariable("DB_KEYBINDING", "song_platform")
	session, err := config.SetupDBConnection(dbURL, dbKeybinding)
	if err != nil {
		log.Panic(err)

		panic(err)
	}
	log.Println("[main:main] Connected to db successfully.")

	// close db connection when program stops
	defer session.Close()

	// connect to consul
	config.SetupConsulConnection()

	// route for Consul health check
	app.Get("/check", func(c *fiber.Ctx) error {
		return c.SendStatus(fiber.StatusOK)
	})

	// middlewares
	// allow cors
	// only on dev
	if mode := utils.GetEnvVariable("PROJECT_MODE", "dev"); mode == "dev" {
		app.Use(cors.New())
		log.Println("[main:main] Allowed cors.")
	}

	// serve static songs files
	app.Use(BASE_PATH+"/song", filesystem.New(filesystem.Config{
		Root: http.Dir(SONGS_PATH),
	}))
	log.Println("[main:main] Static songs dir served.")

	// routes
	// register routes
	router := api.NewRouter(session)
	router.RegisterRoutes(app, BASE_PATH)
	log.Println("[main:main] Router created and registered routes successfully.")

	// start server
	// TODO: get post from variables
	if err := app.Listen(":3000"); err != nil {
		log.Fatal(err)

		panic(err)
	}
}
