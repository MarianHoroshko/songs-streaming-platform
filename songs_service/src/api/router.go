package api

import (
	"github.com/gofiber/fiber/v2"
)

func RegisterRoutes(app *fiber.App, base_path string) {
	var router = app.Group(base_path)

	router.Get("/", func(c *fiber.Ctx) error {
		return c.SendString("test")
	})
}
