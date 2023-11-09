#!/bin/bash

echo "Creating buckets..."
awslocal s3api create-bucket --bucket sample-bucket
