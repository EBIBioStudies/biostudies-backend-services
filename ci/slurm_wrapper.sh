#!/bin/bash

#
# Executes the given command in the SLURM cluster placing the files in a dynamic location based on the job id last
# three digits.
#
# Parameters:
#  $1: Job logs output path
#  $2: Command to be executed by the cluster job
#

base_dir="$1/$(printf "%03d" $((SLURM_JOB_ID % 1000)))"
mkdir -p "$base_dir"
eval "$2" > "$base_dir/${SLURM_JOB_ID}_OUT" 2> "$base_dir/${SLURM_JOB_ID}_IN"
