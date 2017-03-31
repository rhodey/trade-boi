#!/bin/bash

protoc --proto_path=shared/src/main/proto --java_out=shared/src/main/java shared/src/main/proto/trading.proto