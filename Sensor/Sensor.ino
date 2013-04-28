const char * ROOT_URI = "http://wijly.herokuapp.com";


void Log(char * message) {
  Serial.print("0");
  Serial.print(message);
  Serial.print("\n");
}

void Log(String message) {
  Serial.print("0");
  Serial.print(message);
  Serial.print("\n");
}

int lastCheckTime = 0;
String deviceLastState = "";
String deviceNextState = "";
String getDeviceState(String deviceId, String defaultState) {
  int currentTime = millis();
  
  if (currentTime - lastCheckTime > 1000) {
    Serial.print("1");
    Serial.print(ROOT_URI);
    Serial.print("/devices/");
    Serial.print(deviceId);
    Serial.print("\n");
    lastCheckTime = currentTime;
  }
  
  if (deviceLastState.length() < 1) {
    deviceLastState = defaultState;
  }

  char lastRead = '\0';
  while (Serial.available() > 0 && lastRead != '~') {
    Log("Reading serial");
    lastRead = Serial.read();
    if (lastRead == '~') {
      deviceLastState = deviceNextState;
      deviceNextState = "";
      Log(deviceLastState);
    } else {
      deviceNextState += lastRead;
      Log(deviceNextState);
    }
  }

  return deviceLastState;
}

void setDeviceState(String deviceId, String state) {
  Serial.print("2");
  Serial.print(ROOT_URI);
  Serial.print("/devices/");
  Serial.print(deviceId);
  Serial.print("?status=");
  Serial.print(state);
  Serial.print("\n");
}


const char * BUTTON_DEVICE_ID = "button";
const char * LED_DEVICE_ID = "led";
const int buttonPin = 2;
const int ledPin = 13;

void setup() {
  pinMode(buttonPin, INPUT);
  pinMode(ledPin, OUTPUT);
  Serial.begin(9600);
}


int lastButtonState = LOW;

void loop() {
  /*int buttonState = digitalRead(buttonPin);
  if (buttonState != lastButtonState) {
    lastButtonState = buttonState;
    if (buttonState == HIGH) {
      setDeviceState(BUTTON_DEVICE_ID, "on");
    } else {
      setDeviceState(BUTTON_DEVICE_ID, "off");
    }
  }*/
  
  String deviceState = getDeviceState(BUTTON_DEVICE_ID, "off");
  if (deviceState == "on") {
    digitalWrite(ledPin, HIGH);
  } else {
    digitalWrite(ledPin, LOW);
  }
}

