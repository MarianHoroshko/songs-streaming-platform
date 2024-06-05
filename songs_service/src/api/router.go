package api

import (
	"github.com/gocql/gocql"
	"github.com/gofiber/fiber/v2"

	"spotify_clone.com/songs_service/src/api/controllers"
	"spotify_clone.com/songs_service/src/api/repository"
)

type Router struct {
	songController *controllers.SongController
}

func NewRouter(dbSession *gocql.Session) *Router {
	// create repository and controller
	// inject repository to controller
	songRepository := repository.NewSongRepository(dbSession)
	songController := controllers.NewSongController(songRepository)

	return &Router{songController: songController}
}

func (r *Router) RegisterRoutes(app *fiber.App, base_path string) {
	router := app.Group(base_path)

	// upload new song
	router.Post("/song/add", r.songController.AddNewSong)
}
