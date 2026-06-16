set shell := ["bash", "-cu"]

default:
  just --list

build:
  ./gradlew build

runclient:
  ./gradlew runClient

gensources:
  ./gradlew genSources

datagen:
  ./gradlew runDatagen

cleanbuild:
  ./gradlew clean build
