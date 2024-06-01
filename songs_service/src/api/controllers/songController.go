package controllers

import (
	"fmt"
	"log"
	"os"

	"github.com/gofiber/fiber/v2"
	"github.com/google/uuid"
	ffmpeg "github.com/u2takey/ffmpeg-go"
)

const (
	SONGS_PATH = "static/"
)

func AddNewSong(c *fiber.Ctx) error {
	// get song data

	// get song file from artist

	// covert to streamable file
	if err := convertSongToStreamableFiles(); err != nil {
		return err
	}

	// save song data in db

	// return song data
	return c.Status(201).SendString("data")
}

func convertSongToStreamableFiles() error {
	uuid_as_string := uuid.New().String()
	fmt.Println("uuid: " + uuid_as_string)

	createNewDirForSongStreamableFiles(uuid_as_string)

	var file_input = ffmpeg.Input("test_files/test.mp3")

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

	var err = file_output.Run()

	if err != nil {
		return err
	}

	return nil
}

func createNewDirForSongStreamableFiles(uuid string) {
	fmt.Println("path: " + SONGS_PATH + uuid)

	if _, err := os.Stat(SONGS_PATH + uuid); os.IsNotExist(err) {
		// path is a directory

		fmt.Println("Path not exists.")

		if err := os.MkdirAll(SONGS_PATH+uuid, os.ModePerm); err != nil {
			log.Fatal(err)

			return
		}
	}
}
