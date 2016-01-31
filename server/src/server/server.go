package main

import (
	"bufio"
	"bytes"
	"encoding/json"
	"log"
	"net"
	"strconv"
	"sync"
)

func main() {
	ln, err := net.Listen("tcp", ":8080")
	if err != nil {
		log.Fatal(err)
	}
	for {
		conn, err := ln.Accept()
		if err != nil {
			log.Println(err)
		} else {
			go handler(conn)
		}
	}
}

var uuidToTransferMutex sync.Mutex
var uuidToTransfer = make(map[string]*Transfer)

var hashToUuidMutex sync.Mutex
var hashToId = make(map[string]string)

type Transfer struct {
	Uuid           string
	FileName       string
	FileSize       int64
	Hash           string
	Data           chan []byte
	SendDataSignal chan struct{}
}

func handler(conn net.Conn) {
	defer conn.Close()
	r := bufio.NewReader(conn)
	msg, err := r.ReadBytes('\n')
	if err != nil {
		panic(err)
	}
	var prot ProtocolData
	json.Unmarshal(msg, &prot)
	if prot.Role == "up" {
		var t *Transfer
		uuidToTransferMutex.Lock()
		id, ok := hashToId[prot.Hash]
		uuidToTransferMutex.Unlock()
		if !ok {
			id = createId(prot.Hash)
			t = &Transfer{Uuid: id,
				FileName:       prot.FileName,
				FileSize:       prot.FileSize,
				Data:           make(chan []byte, 16),
				SendDataSignal: make(chan struct{}),
				Hash:           prot.Hash,
			}
			uuidToTransferMutex.Lock()
			hashToUuidMutex.Lock()
			uuidToTransfer[id] = t
			hashToId[prot.Hash] = id
			hashToUuidMutex.Unlock()
			uuidToTransferMutex.Unlock()
		} else {
			uuidToTransferMutex.Lock()
			hashToUuidMutex.Lock()
			t = uuidToTransfer[id]
			hashToUuidMutex.Unlock()
			uuidToTransferMutex.Unlock()
		}
		conn.Write([]byte("{\"id\":\"" + t.Uuid + "\"}\n"))
		<-t.SendDataSignal
		conn.Write([]byte("{\"msg\":\"send data\"}\n"))
		var b [32][]byte
		for i := range b {
			b[i] = make([]byte, 1024)
		}
		bi := 0
		for {
			n, err := conn.Read(b[bi])
			if n == 0 || err != nil {
				break
			}
			t.Data <- b[bi][:n]
			if bi++; bi == len(b) {
				bi = 0
			}
		}
		close(t.Data)
		uuidToTransferMutex.Lock()
		hashToUuidMutex.Lock()
		delete(uuidToTransfer, id)
		delete(hashToId, t.Hash)
		hashToUuidMutex.Unlock()
		uuidToTransferMutex.Unlock()
	} else if prot.Role == "down" {
		uuidToTransferMutex.Lock()
		v, ok := uuidToTransfer[prot.Id]
		uuidToTransferMutex.Unlock()
		if !ok {
			conn.Write([]byte("{\"error\":\"Not found\"}\n"))
			return
		}
		conn.Write([]byte("{\"name\":\"" + v.FileName + "\", \"size\":" + strconv.FormatInt(v.FileSize, 10) + "}\n"))
		v.SendDataSignal <- struct{}{}
		for {
			b, ok := <-v.Data
			if !ok {
				break
			}
			conn.Write(b)
		}
	}
}

type ProtocolData struct {
	Role     string `json:"role"`
	Hash     string `json:"hash"`
	FileName string `json:"name"`
	FileSize int64  `json:"size"`
	Id       string `json:"id"`
}

func createId(hash string) string {
	var uuid bytes.Buffer
	for i := 0; i < len(hash); i += 2 {
		uuid.WriteRune(rune(hash[i]))
	}
	return uuid.String()
}
