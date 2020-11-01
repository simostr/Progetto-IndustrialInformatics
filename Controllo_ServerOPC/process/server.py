import csv
from pathlib import Path
base_path=Path(__file__).parent

import time
from opcua import ua, uamethod, Server
import logging as log
import Controllo_ServerOPC.config.gpio as c_gpio
from Controllo_ServerOPC.config.gpio import gpio
from datetime import datetime, timezone, timedelta

log.basicConfig(level=log.ERROR)

class ServerOPC():
    def __init__(self, sensori):
        self.keys=c_gpio.led_dict.keys()
        self.sensori=sensori
        self.__led=c_gpio.led_dict        
        self.init_server()
    
    def init_server(self):
        print("inizio settaggio server")       
        self.server=Server()
        #self.server.set_endpoint("opc.tcp://192.168.1.250:4840")
        self.server.set_endpoint("opc.tcp://0.0.0.0:4840")
        self.server.set_server_name("DomoticProject")
        #carico il certificato e la chiave privata
        global base_path        
        file_path_cert=(base_path / "../config/certificati/my_cert.der").resolve()
        file_path_key=(base_path / "../config/certificati/my_private_key.pem").resolve()        
        self.server.load_certificate(file_path_cert)
        self.server.load_private_key(file_path_key)
        self.server.set_security_policy([
            ua.SecurityPolicyType.NoSecurity,
            ua.SecurityPolicyType.Basic256Sha256_SignAndEncrypt,
            ua.SecurityPolicyType.Basic256Sha256_Sign
        ])
        
        #setup namespace
        uri="Domotic"
        self.idx=self.server.register_namespace(uri)
        self.objects=self.server.get_objects_node()
        
        #per contenere le info dei sensori
        self.sensors_o=self.objects.add_object(self.idx, "Sensori")
        self.temperatura_s=self.sensors_o.add_variable(self.idx, "Temperatura", 0.00)
        self.counterbagno_s=self.sensors_o.add_variable(self.idx, "Counter Bagno", 0)
        self.countersalotto_s=self.sensors_o.add_variable(self.idx, "Counter Salotto", 0)
        
        #per contenere le info dei led
        self.luci_o=self.objects.add_object(self.idx, "Luci")        
        self.luci_o_dict={}
        for x in self.keys:
            self.luci_o_dict[x]=self.luci_o.add_variable(self.idx, "Luce "+x, False)
            self.luci_o_dict[x].set_writable()
        
        #per contenere i metodi
        self.method_o=self.objects.add_object(self.idx, "Metodi")
        
        inarg=ua.Argument()
        inarg.Name="stanza"
        inarg.DataType=ua.NodeId(ua.ObjectIds.String)
        inarg.ValueRank=-1
        inarg.ArrayDimensions=[]
        inarg.Description=ua.LocalizedText("stanza su cui applicare il metodo ['salotto', 'camera', 'bagno', 'ingresso']")
        
        self.mymethod1=self.method_o.add_method(self.idx, "Accendi luce", self.accendi_luce, [inarg])        
        self.mymethod2=self.method_o.add_method(self.idx, "Spegni luce", self.spegni_luce, [inarg])
        
        print("server settato")
                
    def run(self):
        print("start server")
        self.server.start()
        try:
            while True:                
                val=ua.Variant(self.sensori["temperatura"], ua.VariantType.Float)
                val=ua.DataValue(val)                             
                val.SourceTimestamp=datetime.utcnow()                 
                val.ServerTimestamp=datetime.utcnow()                                                                        
                self.temperatura_s.set_data_value(val)                
                
                val2=ua.Variant(self.sensori["counter_bagno"], ua.VariantType.UInt16)
                val2=ua.DataValue(val2)                             
                val2.SourceTimestamp=datetime.utcnow()                 
                val2.ServerTimestamp=datetime.utcnow() 
                self.counterbagno_s.set_data_value(val2)
                
                val3=ua.Variant(self.sensori["counter_salotto"], ua.VariantType.UInt16)
                val3=ua.DataValue(val3)                             
                val3.SourceTimestamp=datetime.utcnow()                 
                val3.ServerTimestamp=datetime.utcnow() 
                self.countersalotto_s.set_data_value(val3)
                
                for x in self.keys:
                    pin=self.__led[x]
                    status=gpio.input(pin)
                    var=ua.Variant(status, ua.VariantType.Boolean)
                    s=ua.DataValue(var)
                    s.SourceTimestamp=datetime.utcnow()
                    s.ServerTimestamp=datetime.utcnow()
                    self.luci_o_dict[x].set_value(s)
                    
                
                time.sleep(1)
                                     
        finally:
            self.server.stop()

    
    @uamethod
    def accendi_luce(self, parent, stanza):
        pin=self.__led[stanza]        
        if stanza=="salotto":
            s=gpio.input(2)
            gpio.output(2, 1)
            gpio.output(pin, 0)
        else:
            gpio.output(pin, 1) 
                  
    @uamethod
    def spegni_luce(self, parent, stanza):
        pin=self.__led[stanza]
        if stanza=="salotto":
            s=gpio.input(2)
            gpio.output(2, 0)
            gpio.output(pin, 1)
        else:
            gpio.output(pin, 0)
        
        
            
