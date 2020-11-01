#import gpiozero as gpio
import RPi.GPIO as gpio
import time
import serial

arduinoSerialData=serial.Serial('/dev/ttyACM0', 9600)

gpio.setmode(gpio.BCM)
gpio.setwarnings(False)

#led setup
gpio.setup(4, gpio.OUT)
gpio.setup(3, gpio.OUT)
gpio.setup(2, gpio.OUT)
gpio.setup(17, gpio.OUT)
gpio.setup(27, gpio.OUT)
led_dict={}
led_dict["salotto"]=3
led_dict["camera"]=4
led_dict["bagno"]=17
led_dict["ingresso"]=27


gpio.setup(21, gpio.IN, pull_up_down=gpio.PUD_DOWN)
gpio.setup(20, gpio.IN, pull_up_down=gpio.PUD_DOWN)
gpio.setup(16, gpio.IN, pull_up_down=gpio.PUD_DOWN)
gpio.setup(12, gpio.IN, pull_up_down=gpio.PUD_DOWN)
button_dict={}
button_dict["salotto"]=21
button_dict["camera"]=20
button_dict["bagno"]=16
button_dict["ingresso"]=12

gpio.setup(26, gpio.IN, pull_up_down=gpio.PUD_DOWN)
gpio.setup(19, gpio.IN, pull_up_down=gpio.PUD_DOWN)
gpio.setup(13, gpio.IN, pull_up_down=gpio.PUD_DOWN)
gpio.setup(6, gpio.IN, pull_up_down=gpio.PUD_DOWN)
counter_dict={}
counter_dict["salotto_in"]=26
counter_dict["salotto_out"]=19
counter_dict["bagno_in"]=13
counter_dict["bagno_out"]=6
#gpio.cleanup()
