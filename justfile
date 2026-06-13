set shell := ["bash", "-cu"]

java_home := env_var_or_default("JAVA_HOME_GRADLE", "/home/dgjalic/.sdkman/candidates/java/24.0.1-amzn")

default:
    just --list

build:
    JAVA_HOME="{{java_home}}" PATH="{{java_home}}/bin:$PATH" ./gradlew build

runclient:
    JAVA_HOME="{{java_home}}" PATH="{{java_home}}/bin:$PATH" ./gradlew runClient

gensources:
    JAVA_HOME="{{java_home}}" PATH="{{java_home}}/bin:$PATH" ./gradlew genSources

datagen:
    JAVA_HOME="{{java_home}}" PATH="{{java_home}}/bin:$PATH" ./gradlew runDatagen

cleanbuild:
    JAVA_HOME="{{java_home}}" PATH="{{java_home}}/bin:$PATH" ./gradlew clean build
