import Controllo_ServerOPC.config.gpio as c_gpio
from Controllo_ServerOPC.config.gpio import gpio
import time

class Control():
    def __init__(self, sensori):
        self.keys=c_gpio.led_dict.keys()
        self.sensori=sensori
        self.__led=c_gpio.led_dict
        self.__button=c_gpio.button_dict
        self.__counter=c_gpio.counter_dict
        self.__counter_bagno=0
        self.__counter_salotto=0
        self.__increase_salotto=False
        self.__decrease_salotto=False
        self.__increase_bagno=False
        self.__decrease_bagno=False         
        self.__init_Button()
        self.__init_Counters()        
       
    def run(self):
        print("start controllo")        
        while True:            
            if(c_gpio.arduinoSerialData.inWaiting() !=0):
                myData=c_gpio.arduinoSerialData.readline()
                myData=myData.decode("utf-8")
				if len(myData.strip())==5:
					val=float(myData)
					self.sensori["temperatura"]=val
            time.sleep(1)        
                  
    def __luci_set(self, pin):
        for x in self.keys:
            if self.__button[x]==pin:
                key=x
                break
        pin=self.__led[key]
        status=gpio.input(pin)
        if key=="salotto":            
            stat=gpio.input(2)
            gpio.output(2, not stat) 
        gpio.output(pin, not status) 
       
    def __in_start_salotto(self, pin):        
        if(self.__decrease_salotto):
            if self.__counter_salotto>0:
                print("si decremento salotto")
                self.__counter_salotto=self.__counter_salotto-1
                self.sensori["counter_salotto"]=self.__counter_salotto
                if self.__counter_salotto==0:
                    gpio.output(self.__led["salotto"], 1)
                    gpio.output(2,0)
            else:
                print("sono gia' a zero, ho contato male")
            self.__decrease_salotto=False
        else:
            print("incremento salotto?")
            self.__increase_salotto=True                
    
    def __out_start_salotto(self, pin):
        if(self.__increase_salotto):
            print("si incremento salotto")
            self.__counter_salotto=self.__counter_salotto+1
            self.sensori["counter_salotto"]=self.__counter_salotto
            if self.__counter_salotto==1:
                    gpio.output(self.__led["salotto"], 0)
                    gpio.output(2,1)
            self.__increase_salotto=False
        else:
            print("decremento salotto?")
            self.__decrease_salotto=True

    def __in_start_bagno(self, pin):        
        if(self.__decrease_bagno):
            if self.__counter_bagno>0:
                print("si decremento bagno")
                self.__counter_bagno=self.__counter_bagno-1
                self.sensori["counter_bagno"]=self.__counter_bagno
                if self.__counter_bagno==0:
                    gpio.output(self.__led["bagno"], 0)                
            else:
                print("sono gia' a zero, ho contato male")
            self.__decrease_bagno=False
        else:
            print("incremento bagno?")
            self.__increase_bagno=True                
    
    def __out_start_bagno(self, pin):
        if(self.__increase_bagno):
            print("si incremento bagno")
            self.__counter_bagno=self.__counter_bagno+1
            self.sensori["counter_bagno"]=self.__counter_bagno
            if self.__counter_bagno==1:
                    gpio.output(self.__led["bagno"], 1)
            self.__increase_bagno=False
        else:
            print("decremento bagno?")
            self.__decrease_bagno=True  
    
    def __init_Button(self):
        for x in self.keys:
            print('setto pulsante ', x)
            pin=self.__button[x]
            print('setto pulsante ', pin)
            gpio.add_event_detect(pin, gpio.RISING, 
                                callback=self.__luci_set, bouncetime=400)  
    
    def __init_Counters(self):        
        self.__increase_salotto=False
        self.__decrease_salotto=False                  
        pin_in=self.__counter["salotto_in"]
        pin_out=self.__counter["salotto_out"]
        print("setto porta salotto. Pin in: ", pin_in, "; pin out: ", pin_out)
        gpio.add_event_detect(pin_in, gpio.RISING, callback=self.__in_start_salotto, bouncetime=1000)
        gpio.add_event_detect(pin_out, gpio.RISING, callback=self.__out_start_salotto, bouncetime=1000)

        self.__increase_bagno=False
        self.__decrease_bagno=False                  
        pin_in=self.__counter["bagno_in"]
        pin_out=self.__counter["bagno_out"]
        print("setto porta bagno. Pin in: ", pin_in, "; pin out: ", pin_out)
        gpio.add_event_detect(pin_in, gpio.RISING, callback=self.__in_start_bagno, bouncetime=1000)
        gpio.add_event_detect(pin_out, gpio.RISING, callback=self.__out_start_bagno, bouncetime=1000)
            
            
            
                
