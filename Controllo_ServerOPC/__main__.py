import Controllo_ServerOPC.process.controllo as ctrl
import Controllo_ServerOPC.process.server as ser
import Controllo_ServerOPC.config.var as var
import multiprocessing

controllo=ctrl.Control(var.sensors_dict)
server=ser.ServerOPC(var.sensors_dict)

p1=multiprocessing.Process(target=controllo.run)
p2=multiprocessing.Process(target=server.run)

if __name__=="__main__":
    
    p1.start()
    p2.start()
    
    p1.join()
    p2.join()
           
    
