#!../venv/bin/python 
# -*- coding: utf-8 -*- 

import paho.mqtt.client as mqtt

import json

import snowboydecoder
import sys
import signal
import time
from googlesamples.assistant.grpc.talkassist import main 

sleepTime = 0.03
interrupted = False

# connected = 0

# # The callback for when the client receives a CONNACK response from the server.
# def on_connect(client, userdata, flags, rc):
#     global connected
#     connected = 1

#     print("Connected with result code "+str(rc))

#     # Subscribing in on_connect() means that if we lose the connection and
#     # reconnect then subscriptions will be renewed.
#     client.subscribe("smartmirror/spotifycreds")

# def on_disconnect(client, userdata, rc):
#     global connected
#     connected = 0


# # The callback for when a PUBLISH message is received from the server.
# def on_message(client, userdata, msg):
#     print(msg.topic+" "+str(msg.payload))
#     
#     # Store the token and refresh token needed to authorize spotify user
#     f = open("spotifyCreds.txt", "w")
#     f.write(str(msg.payload))
#     f.close()


def signal_handler(signal, frame):
    global interrupted
    interrupted = True

def interrupt_callback():
    global interrupted
    return interrupted

def detectedCallback():
    snowboydecoder.play_audio_file()

    # call google assitant
    main()
    print('\nListening... Press Ctrl+C to exit')


detector = snowboydecoder.HotwordDetector("hey_mirror.pmdl", sensitivity=0.5)
print('Listening... Press Ctrl+C to exit')

# client = mqtt.Client()
# client.on_connect = on_connect
# client.on_message = on_message

# try:
#     client.connect("192.168.2.126", 1883, 60)
#     client.loop_start()
# except:
#     print("could not connect")

detector.start(detected_callback=detectedCallback,
            interrupt_check=interrupt_callback,
            sleep_time=sleepTime)

detector.terminate()
