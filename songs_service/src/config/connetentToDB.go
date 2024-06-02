package config

import (
	"log"

	"github.com/gocql/gocql"
)

func SetupDBConnection(dbConnectionUrl string, keyspace string) (*gocql.Session, error) {
	// create cluster
	cluster := gocql.NewCluster(dbConnectionUrl)
	cluster.Keyspace = keyspace
	cluster.Consistency = gocql.Quorum
	log.Println("[config:SetupDBConnection] Successfully created db cluster.")

	dbSession, err := gocql.NewSession(*cluster)
	if err != nil {
		log.Panic(err)

		return nil, err
	}
	log.Println("[config:SetupDBConnection] Successfully created new db session.")

	return dbSession, nil
}
