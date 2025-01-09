# UDPLog
This project is a simple UDP server host that can run multiple instances of a UDP server on different ports. When data is sent to it, it writes the data to a log file.

You can configure it using a simple `settings.ini` file.

### Intent
The original purpose of this project was to provide a way for microcontrollers such as the Arduino line or the `Raspberry Pi Pico W` etc. to be able to send data to a computer for ongoing data logging. Doing it over the network is MUCH EASIER than trying to do it via bluetooth over serial. 

The program will work for anything that can send data via UDP to an IP address or network host, it is not limited to microcontroller projects.

## Installation
Look in the [releases](https://github.com/EasyG0ing1/UDPLog/releases) section and download the zip file for your operating system, then unzip it. It has been compiled on MacOS, Windows and Ubuntu Linux. They are native binaries and do not require the Java virtual machine to run. When you download the executable, you run it and after first launch, it will create a file in the folder it was launched from called `settings.ini`.

## Usage
Open the settings file and change the options to suit your needs. The format of the settings file is:

```
[Server1]
port=4444
bufferSize=512
logFile=/Users/user/logs/logFile1.txt
                
[Server2]
port=4445
bufferSize=512
logFile=/Users/user/logs/logFile2.txt
```

Here is the breakdown

- Section Heading
  - The section headings must be enclosed in brackets and the first word must be `Server`. Each section will create a new UDP server instance, so you can have as many as you like. You're only limited by the amount of RAM you have in your computer and each instance doesn't take much ram at all (maybe a hundred bytes or less).

- ***port***
  - This is the UDP port number that you need to send your data to (1 - 65535) see [this link](https://en.wikipedia.org/wiki/List_of_TCP_and_UDP_port_numbers) for port numbers you should not use.
- ***bufferSize***
  - This is the size of the UDP packet you desire to send. In Arduino environments, make this number larger than the buffer size you use to build your UDP packet. If that size is unknown then simply set it to be larger than the largest size your project might send. The data frame in a UDP packet can only be 65,535 bytes (64k), but there is overhead in those packets so your size should be smaller than that. Common practice is to not exceed 1,500 for your packet size because of fragmentation potential etc. But on a local network, you can safely go much larger than that since you most likely won't be traversing multiple routers processing heavy amounts of traffic. You don't need to be precise here ... going with a size that is larger than you anticipate is a good idea (so lets say you wont ever send a packet larger than 500 bytes, set this value to 1,000 and you'll be fine)
- ***logFile***
  - This is the FULL PATH to the log file that you want that specific server instance to write the data to. (A Windows path would need to be in standard Windows path format - `C:\Users\user\logs\logFile.txt` for example)

### Program Arguments
```aiignore
UDPLog test
UDPLog version
```

Passing in the word `test` will give you feedback as the program runs which lets you perform data send tests and the program will let you know if your data was received etc.

## Example Arduino Code
This is how a typical Arduino sketch might look like that would use this program:
```C++
#include <WiFi.h>
#include <WiFiUdp.h>

WiFiUDP udpSend;

const auto ssid = "ssid";
const auto password = "ssidPassword";
const IPAddress logIPAddress(192,168,1,15); // IP address to send data to
constexpr uint32_t logPort = 60379; // UDP port to send data on

void sendUDP(const String &msg, const int udpPort = logPort) {
    if (WiFi.status() == WL_CONNECTED) {
        int len = msg.length();
        char buffer[len + 1];
        snprintf(buffer, sizeof(buffer), "%s", msg.c_str());
        udpSend.beginPacket(logIPAddress, udpPort);
        udpSend.write(buffer);
        udpSend.endPacket();
    }
}

void setup() {
    WiFi.mode(WIFI_STA);
    WiFi.begin(ssid, password);
}

void loop() {
    if(logData) {
        sendUDP(myLogData, logPort);
    }
}
```

## Service / Daemon
You can use the program as a Windows service or a linux daemon, but you'll have to Google how to do that if you're interested in it.

### Assistance
If you have any questions, comments or problems, simply create an Issue in this repo and I'll reply as soon as I see it.

# Release Notes
- 1.0.0 - Initial release
