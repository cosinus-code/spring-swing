#!/bin/bash

INFO_COLOR="\033[0;34m"
ERROR_COLOR="\033[0;31m"
WARNING_COLOR="\033[1;33m"
NO_COLOR='\033[0m'

function show-info() {
    echo -e "[${INFO_COLOR}INFO${NO_COLOR}] $1"
}

function show-warning() {
    echo -e "[${WARNING_COLOR}INFO${WARNING_COLOR}] $1"
}

function show-error() {
    echo -e "[${ERROR_COLOR}ERROR${NO_COLOR}] $1"
}

function start-time-watcher() {
  if [ -z "$initial_timestamp" ]; then
    initial_timestamp=$(date +%s)
  fi
}

function show-elapsed-time() {
  start_timestamp=${1-$initial_timestamp}
  current_timestamp=$(date +%s)
  seconds=$((current_timestamp-start_timestamp))
  if [ "$seconds" -gt 60 ]; then
    minutes=$((seconds/60))
    seconds=$((seconds%60))
    show-info "Elapsed time: $(printf "%02d" $minutes):$(printf "%02d" $seconds) min"
  else
    show-info "Elapsed time: $seconds sec"
  fi
}