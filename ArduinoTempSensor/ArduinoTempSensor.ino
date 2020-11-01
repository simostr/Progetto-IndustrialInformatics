int pinLM35=A0;
int cicli=10;
float tot=0;
float temp=0;
int i=0;

void setup() {
  Serial.begin(9600);
}

void loop() {
  if(i>=cicli){
    temp=tot/cicli;
    Serial.println(temp);
    i=0;
    temp=0;
    tot=0;
  }
  int valore=analogRead(pinLM35);   
  tot+=valore*0.48875;  
  i++;
  delay(100);
}
