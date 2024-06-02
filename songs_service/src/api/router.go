package api

import (
	"github.com/gofiber/fiber/v2"

	"spotify_clone.com/songs_service/src/api/controllers"
)

func RegisterRoutes(app *fiber.App, base_path string) {
	router := app.Group(base_path)

	// upload new song
	router.Post("/song/add", controllers.AddNewSong)
}
