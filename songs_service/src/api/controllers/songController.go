package controllers

import (
	"log"
	"mime/multipart"
	"os"

	"github.com/gofiber/fiber/v2"
	"github.com/google/uuid"
	ffmpeg "github.com/u2takey/ffmpeg-go"
	"spotify_clone.com/songs_service/src/api/models"
)

// TODO: to .env file
const (
	SONGS_PATH = "static/"
)

func AddNewSong(c *fiber.Ctx) error {
	// TODO: add error res for ifs

	// get song data
	var reqBody = new(models.Song)
	if err := c.BodyParser(reqBody); err != nil {
		log.Fatal(err)

		return err
	}
	log.Println("[controllers:AddNewSong] Request body parsed successfully.")

	// get song file
	songFile, err := c.FormFile("song_file")
	if err != nil {
		log.Fatal(err)

		return err
	}
	log.Println("[controllers:AddNewSong] Got file form form data.")

	// get song content as bytes
	fileContent, err := songFile.Open()
	if err != nil {
		log.Fatal(err)

		return err
	}
	log.Println("[controllers:AddNewSong] Got file content.")

	// covert to streamable file
	songId, err := convertSongToStreamableFiles(fileContent)
	if err != nil {
		log.Fatal(err)

		return err
	}
	log.Println("[controllers:AddNewSong] Successfully converted user's file.")

	// save song data in db

	// TODO: add response
	// return song data
	return c.Status(201).JSON(fiber.Map{
		"song_id": songId,
	})
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
