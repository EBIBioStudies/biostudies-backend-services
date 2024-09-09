#!/bin/bash

#
# Executes the given command in the SLURM cluster placing the files in a dynamic location based on the job id last
# three digits. For example, for a job with id 12345, the logs would be placed at /the/logs/path/345/12345_OUT.
#
# Parameters:
#  $1: Job logs output path
#  $2: Command to be executed by the cluster job
#

base_dir="$1/$(printf "%03d" $((SLURM_JOB_ID % 1000)))"
mkdir -p "$base_dir"
eval "$2" > "$base_dir/${SLURM_JOB_ID}_OUT" 2> "$base_dir/${SLURM_JOB_ID}_IN"
