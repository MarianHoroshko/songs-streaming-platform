FROM golang:1.22-alpine

# Download ffmpeg
RUN apk update & apk upgrade 
RUN apk --no-cache add ca-certificates wget bash xz-libs git
WORKDIR /tmp
RUN wget https://johnvansickle.com/ffmpeg/releases/ffmpeg-release-amd64-static.tar.xz
RUN tar -xvJf  ffmpeg-release-amd64-static.tar.xz
RUN cd ff* && mv ff* /usr/local/bin

WORKDIR /

# COPY
COPY go.mod ./
COPY go.sum ./
RUN go mod download

COPY *.go ./
COPY . .

# RUN go build -o bin

# ENTRYPOINT [ "./bin/songs_service" ]
