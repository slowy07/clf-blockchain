package main

import (
	"bytes"
	"crypto/sha256"
	"strconv"
	"time"
)

type Block struct {
	Timestamp     int64
	Data          []byte
	PrevBlockHash []byte
	Hash          []byte
}

func (b *Block) SetHash() {
	timestamp := []byte(strconv.FormatInt(b.Timestamp, 10))
	headers := bytes.Join([][]byte{b.PrevBlockHash, b.Data, timestamp}, []byte{})
	hash := sha256.Sum256(headers)

	b.Hash = hash[:]
}

func NewBlock(data string, prevBlockchain []byte) *Block {
	block := &Block{time.Now().Unix(), []byte(data), prevBlockchain, []byte{}}
	block.SetHash()

	return block
}

func NewGenesiBlock() *Block {
	return NewBlock("Genesis Block", []byte{})
}
