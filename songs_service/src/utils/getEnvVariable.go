package utils

import "os"

func GetEnvVariable(key string, fallback string) string {
	var value string = os.Getenv(key)
	if len(value) == 0 {
		return fallback
	}
	return value
}
