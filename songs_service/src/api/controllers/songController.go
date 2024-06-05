package controllers

import (
	"log"
	"mime/multipart"
	"os"

	"github.com/gofiber/fiber/v2"
	"github.com/google/uuid"
	ffmpeg "github.com/u2takey/ffmpeg-go"

	"spotify_clone.com/songs_service/src/api/models"
	"spotify_clone.com/songs_service/src/api/repository"
)

// TODO: to .env file
const (
	SONGS_PATH = "static/"
)

type SongController struct {
	songRepository *repository.SongRepository
}

func NewSongController(r *repository.SongRepository) *SongController {
	return &SongController{songRepository: r}
}

func (s *SongController) AddNewSong(c *fiber.Ctx) error {
	// get data form multiform
	form, err := c.MultipartForm()
	if err != nil {
		log.Fatal(err)

		return c.Status(fiber.StatusBadRequest).JSON(fiber.Map{
			"status":  fiber.StatusBadRequest,
			"message": err.Error(),
		})
	}

	// get song title
	titleValues := form.Value["title"]
	if len(titleValues) == 0 {
		log.Fatal(err)

		return c.Status(fiber.StatusBadRequest).JSON(fiber.Map{
			"status":  fiber.StatusBadRequest,
			"message": "Empty title data.",
		})
	}
	songTitle := form.Value["title"][0]
	log.Println("[controllers:AddNewSong] Got song title successfully.")

	files := form.File["song_file"]
	if len(files) == 0 {
		log.Fatal(err)

		return c.Status(fiber.StatusBadRequest).JSON(fiber.Map{
			"status":  fiber.StatusBadRequest,
			"message": "Got no song files.",
		})
	}
	songFile := form.File["song_file"][0]
	log.Println("[controllers:AddNewSong] Got song file successfully.")

	// get song content as bytes
	fileContent, err := songFile.Open()
	if err != nil {
		log.Fatal(err)

		return c.Status(fiber.StatusBadRequest).JSON(fiber.Map{
			"status":  fiber.ErrBadRequest,
			"message": err.Error(),
		})
	}
	log.Println("[controllers:AddNewSong] Got file content.")

	// covert to streamable file
	songId, err := convertSongToStreamableFiles(fileContent)
	if err != nil {
		log.Fatal(err)

		return c.Status(fiber.StatusBadRequest).JSON(fiber.Map{
			"status":  fiber.ErrBadRequest,
			"message": err.Error(),
		})
	}
	log.Println("[controllers:AddNewSong] Successfully converted user's file.")

	// create song entity and set data
	var songEntity models.Song
	songEntity.ID = songId
	songEntity.Title = songTitle

	// save song data in db
	_, err = s.songRepository.Save(songEntity)
	if err != nil {
		log.Panic(err)

		return c.Status(fiber.StatusBadRequest).JSON(fiber.Map{
			"status":  fiber.ErrBadRequest,
			"message": err.Error(),
		})
	}

	// TODO: add response
	// return song data
	return c.Status(fiber.StatusCreated).JSON(songEntity)
}

func convertSongToStreamableFiles(songsByteData multipart.File) (string, error) {
	uuid_as_string := uuid.New().String()

	if err := createNewDirForSongStreamableFiles(uuid_as_string); err != nil {
		log.Fatal(err)

		return "", err
	}

	var file_input = ffmpeg.Input("pipe:0").WithInput(songsByteData)

	var file_output = file_input.Output(SONGS_PATH+uuid_as_string+"/output%03d.ts",
		ffmpeg.KwArgs{
			"c:a":            "libmp3lame",
			"b:a":            "128k",
			"map":            "0:0",
			"f":              "segment",
			"segment_time":   "10",
			"segment_list":   SONGS_PATH + uuid_as_string + "/outputlist.m3u8",
			"segment_format": "mpegts",
		})

	if err := file_output.Run(); err != nil {
		log.Fatal(err)

		return "", err
	}

	return uuid_as_string, nil
}

func createNewDirForSongStreamableFiles(uuid string) error {
	if _, err := os.Stat(SONGS_PATH + uuid); os.IsNotExist(err) {

		if err := os.MkdirAll(SONGS_PATH+uuid, os.ModePerm); err != nil {
			log.Fatal(err)

			return err
		}
	}

	return nil
}
