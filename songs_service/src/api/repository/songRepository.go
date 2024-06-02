package repository

import (
	"log"

	"github.com/gocql/gocql"
	"spotify_clone.com/songs_service/src/api/models"
)

type SongRepository interface {
	Save(song models.Song) (*models.Song, error)
}

type songRepository struct {
	session *gocql.Session
}

func NewSongRepository(dbSession *gocql.Session) SongRepository {
	return &songRepository{session: dbSession}
}

func (s *songRepository) Save(song models.Song) (*models.Song, error) {
	// create insert query
	var query string = "INSERT INTO song (id, title) VALUES(?, ?)"

	// execute insert query
	if err := s.session.Query(query, song.ID, song.Title).Exec(); err != nil {
		log.Panic(err)

		return nil, err
	}

	// return saved model
	return &song, nil
}
