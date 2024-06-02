package models

type Song struct {
	ID    string `json:"id" form:"id"`
	Title string `json:"title" form:"title"`
}
