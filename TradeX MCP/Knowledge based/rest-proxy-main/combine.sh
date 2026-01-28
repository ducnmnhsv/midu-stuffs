#!/bin/bash
cp src/env.js build/src/
cd build/src
node combineapi.js
cd ../..


echo "cp build/src/combined.json src/"
