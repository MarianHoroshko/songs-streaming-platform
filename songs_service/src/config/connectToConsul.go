package config

import (
	"fmt"
	"log"

	capi "github.com/hashicorp/consul/api"
)

func SetupConsulConnection() error {
	consulDefaultConfig := capi.DefaultConfig()
	consulClient, err := capi.NewClient(consulDefaultConfig)
	if err != nil {
		log.Panic(err)

		return err
	}

	// TODO: get data from .env file
	serviceId := "go-server4321"
	serviceName := "SongService"
	port := 3000
	address := "127.0.0.1"
	dockerAddress := "host.docker.internal"

	registeration := &capi.AgentServiceRegistration{
		ID:      serviceId,
		Name:    serviceName,
		Port:    port,
		Address: address,
		Check: &capi.AgentServiceCheck{
			HTTP:     fmt.Sprintf("http://%v:%v/check", dockerAddress, port),
			Interval: "10s",
			Timeout:  "30s",
		},
	}

	err = consulClient.Agent().ServiceRegister(registeration)
	if err != nil {
		log.Panic(err)

		log.Printf("[config:SetupConsulConnection] Failed to register service: %s:%v ", address, port)

		return err
	}

	log.Printf("[config:SetupConsulConnection] Successfully register Consul service: %s:%v", address, port)

	return nil
}
