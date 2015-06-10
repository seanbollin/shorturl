#!/bin/bash
echo Initiating deployment procedure ...
sbt assembly
scp -i ~/seanbo.pem target/scala-2.11/shorturl-assembly-0.1.jar ubuntu@ec2-54-165-63-62.compute-1.amazonaws.com:~
ssh -i ~/seanbo.pem ubuntu@ec2-54-165-63-62.compute-1.amazonaws.com "nohup java -jar shorturl-assembly-0.1.jar 2> errorOutput.log > output.log &"
ssh -i ~/seanbo.pem ubuntu@ec2-54-165-63-62.compute-1.amazonaws.com screen -d -m ~/redis-stable/src/redis-server
