/*import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;*/
import java.nio.charset.Charset;
import java.io.*;
import gnu.io.CommPortIdentifier; 
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent; 
import gnu.io.SerialPortEventListener; 
import java.util.Enumeration;
import java.net.*;


public class SerialAdapter implements SerialPortEventListener {
  SerialPort serialPort;
  /** The port we're normally going to use. */
  private static final String PORT_NAMES[] = { 
    "/dev/tty.usbserial-A9007UX1", // Mac OS X
    "/dev/tty.usbmodemfd121", // Mac OSX
    "/dev/ttyUSB0", // Linux
    "COM3", // Windows
    };

  /**
  * A BufferedReader which will be fed by a InputStreamReader 
  * converting the bytes into characters 
  * making the displayed results codepage independent
  */
  private BufferedReader input;
  /** The output stream to the port */
  private OutputStream output;
  /** Milliseconds to block while waiting for port open */
  private static final int TIME_OUT = 2000;
  /** Default bits per second for COM port. */
  private static final int DATA_RATE = 9600;

  public void initialize() {
    CommPortIdentifier portId = null;
    Enumeration portEnum = CommPortIdentifier.getPortIdentifiers();

    //First, Find an instance of serial port as set in PORT_NAMES.
    while (portEnum.hasMoreElements()) {
      CommPortIdentifier currPortId = (CommPortIdentifier) portEnum.nextElement();
      for (String portName : PORT_NAMES) {
        if (currPortId.getName().equals(portName)) {
          System.out.println("Found port: " + portName);
          portId = currPortId;
          break;
        }
      }
    }
    if (portId == null) {
      System.out.println("Could not find COM port.");
      return;
    }
                              
    try {
      // open serial port, and use class name for the appName.
      serialPort = (SerialPort) portId.open(this.getClass().getName(), TIME_OUT);
      System.out.println("Port open");
                              
      // set port parameters
      serialPort.setSerialPortParams(DATA_RATE,
        SerialPort.DATABITS_8,
        SerialPort.STOPBITS_1,
        SerialPort.PARITY_NONE);
                              
      // open the streams
      input = new BufferedReader(new InputStreamReader(serialPort.getInputStream()));
      output = serialPort.getOutputStream();
      System.out.println("Streams open");
                              
      // add event listeners
      serialPort.addEventListener(this);
      serialPort.notifyOnDataAvailable(true);
      System.out.println("Event listeners added");
    } catch (Exception e) {
      System.err.println(e.toString());
    }
  }
                              
  /**
  * This should be called when you stop using the port.
  * This will prevent port locking on platforms like Linux.
  */
  public synchronized void close() {
    if (serialPort != null) {
      serialPort.removeEventListener();
      serialPort.close();
    }
  }
                              
  /**
  * Handle an event on the serial port. Read the data and print it.
  */
  public synchronized void serialEvent(SerialPortEvent oEvent) {
    if (oEvent.getEventType() == SerialPortEvent.DATA_AVAILABLE) {
      try {
        String inputLine=input.readLine();
				if (inputLine.length() > 0) {
					System.out.println("INPUT: " + inputLine);

					char op = inputLine.charAt(0);
					String url = inputLine.substring(1);

					if (op == '0') {
						// No-op used to print
					} else if (!url.startsWith("http")) {
						System.err.println("BAD INPUT!");
					} else {
						if (op == '1') {
							String response = webGet(url) + "~";
							byte[] data = response.getBytes(Charset.forName("UTF-8"));
							output.write(data);
						} else if (op == '2') {
							//String urlParameters = "fName=" + URLEncoder.encode("???", "UTF-8") + "&lName=" + URLEncoder.encode("???", "UTF-8");
							webPost(url, null);
						} else {
							System.err.println("Unexpected operation: " + op);
						}
					}
				}
        System.out.println(inputLine);
      } catch (Exception e) {
        System.err.println(e.toString());
      }
    }
    // Ignore all the other eventTypes, but you should consider the other ones.
  }

  public static String webGet(String urlText) {
		String result = null;

    try {
			System.out.println("GET URL: " + urlText);

      URL url = new URL(urlText);
      URLConnection cxn = url.openConnection();
      BufferedReader in = new BufferedReader(new InputStreamReader(cxn.getInputStream()));

			String inputLine;
      while ((inputLine = in.readLine()) != null) {
				if (result == null) {
					result = inputLine;
				} else {
					result += inputLine;
				}
      }

      in.close();
    } catch (Exception e) {
      System.err.println(e.toString());
    }

		System.out.println("RESULT:\n" + result);
		return result;
  }

  public static String webPost(String targetURL, String urlParameters)
  {
		URL url;
    HttpURLConnection connection = null;  
    try {
			System.out.println("POST URL: " + targetURL + " | " + urlParameters);

      //Create connection
      url = new URL(targetURL);
      connection = (HttpURLConnection)url.openConnection();
      connection.setRequestMethod("POST");
      connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

      String contentLen = urlParameters == null ? "0" : Integer.toString(urlParameters.getBytes().length);
      connection.setRequestProperty("Content-Length", contentLen);
      //connection.setRequestProperty("Content-Language", "en-US");  
      
      connection.setUseCaches(false);
      connection.setDoInput(true);
      connection.setDoOutput(true);

      //Send request
			DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
			if (urlParameters != null) {
				wr.writeBytes(urlParameters);
			}
			wr.flush();
			wr.close();
      
      //Get Response  
      InputStream is = connection.getInputStream();
      BufferedReader rd = new BufferedReader(new InputStreamReader(is));
      String line;
      StringBuffer response = new StringBuffer(); 
      while((line = rd.readLine()) != null) {
        response.append(line);
        response.append('\r');
      }
      rd.close();

			String result = response.toString();
			System.out.println("RESULT:");
			System.out.println(result);

      return response.toString();
      
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    } finally {
      if(connection != null) {
        connection.disconnect(); 
      }
    }
  }

  public static void main(String[] args) throws Exception {
    SerialAdapter main = new SerialAdapter();
    main.initialize();
    Thread t=new Thread() {
      public void run() {
        //the following line will keep this app alive for 1000 seconds,
        //waiting for events to occur and responding to them (printing incoming messages to console).
        try {
					Thread.sleep(1000000);
				} catch (InterruptedException ie) {
				}
      }
    };
    t.start();
    System.out.println("Started");
  }
}
