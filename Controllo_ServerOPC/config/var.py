from multiprocessing import Manager

manager=Manager()

sensors_dict=manager.dict()
sensors_dict["temperatura"]=0
sensors_dict["counter_bagno"]=0
sensors_dict["counter_salotto"]=0
