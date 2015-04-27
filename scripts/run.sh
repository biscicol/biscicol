#!/bin/sh
# script to load data

# 1. load individual datasets to biscicol.rdf
isql < ./loadData.sql

# 2. build relations and god table
sudo rm /tmp/relations*
sudo rm /tmp/god*
isql < ./buildRelationsGraph.sql