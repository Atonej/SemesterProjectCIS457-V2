import java.net.*;
import java.awt.HeadlessException;
import java.io.*;
import java.util.Scanner;
import java.util.ArrayList;
import java.util.stream.Stream;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;


public class Server{
	
	
	//int Size = 10000;
	//byte tempBuffer[] = new byte[Size];
	//static Mixer.Info[] mixerInfo = AudioSystem.getMixerInfo();
	private Socket socket;
	//private Socket asocket;
	private ServerSocket server;
	//private ServerSocket aserver;
	public static ArrayList<ClientHandler> handler = new ArrayList<ClientHandler>();
	
	public Server(){
	    try {
	    	server = new ServerSocket(9090);
	    	//aserver = new ServerSocket(9094);
	    	server.setReuseAddress(true);
			System.out.println("Running the Central Server on  9090");
	       
	        
			while(true){
				try{
					socket=server.accept();
					socket.setReuseAddress(true);
					//asocket = aserver.accept();
					handler.add(new ClientHandler(socket));
					//server.close();
				}	catch(IOException i){
					System.out.println(i);
				}
			}

	    }
	    catch (IOException e) {

	        e.printStackTrace();
	    }
		


	}
	

	public static void main(String args[]){
		Server server = new Server();
	}
	
	
}
class ClientHandler{
	
	private ServerSocket aserver;
		private Socket asocket = null;
		private InputStream input;
		private TargetDataLine targetDataLine;
		private OutputStream out;
		private AudioFormat audioFormat;
		private SourceDataLine sourceDataLine;
		int Size = 10000;
		byte tempBuffer[] = new byte[Size];
		static Mixer.Info[] mixerInfo = AudioSystem.getMixerInfo();
		AudioClient audio = new AudioClient();

		//private ServerSocket server;
	
	
	private Socket socket;
	public String uname;
	private Packet packet;
	private ObjectOutputStream oos;
	public ClientHandler(Socket socket){
		this.socket = socket;
		new Thread(()->{
			try {
				InputStream is=socket.getInputStream();
				ObjectInputStream ois= new ObjectInputStream(is);

				OutputStream os = socket.getOutputStream();
				oos = new ObjectOutputStream(os);

				//String line ="";
				boolean quit = false;
				while(!quit){
					Packet p =  (Packet)ois.readObject();
					
					if(p.type==CommandType.MESSAGE){
						if(p.message.equals("q")) {
							System.out.println("Stopping chat");
							quit = true;
						}
						
						else if(p.message.equals("call")) {
							System.out.println("Starting call");
							
//		      				audio.captureAudio(socket);
							//socket.close();
							//oos.reset();
							//os.flush();
							//is.reset();
							//is.close();
							//ois.reset();
							//ois.close();
							//oos.flush();
							//oos.close();
							startListening();
						}
						
						else if(p.message.equals("end")) {
							System.out.println("Stopping call");
							quit = true;
						}
						
						else {
						System.out.println(p.uname+": "+p.message);
						}
						Server.handler.forEach(t->{
							
							try{t.oos.writeObject(p);
							}
							
							catch(IOException e){System.err.println(e);};
						});
					}

				}

			}catch(Exception i){System.out.println(i);};
		}).start();
		//UDP
	}

	public void startListening() {
		//new Thread(() -> {
		//oos.notifyAll();
		try {
			aserver = new ServerSocket(9096);

		 Mixer mixer_ = AudioSystem.getMixer(mixerInfo[1]);   // Select Available Hardware Devices for the speaker, for my Notebook it is number 1
	        audioFormat = getAudioFormat();
	        DataLine.Info dataLineInfo = new DataLine.Info(SourceDataLine.class, audioFormat);
	        sourceDataLine = (SourceDataLine) AudioSystem.getLine(dataLineInfo);
	        sourceDataLine.open(audioFormat);
	        sourceDataLine.start();
	        //server = new ServerSocket(500);
	        //start listening
	        //asocket = socket;
	        asocket = aserver.accept();
	        captureAudio();
	        input = new BufferedInputStream(asocket.getInputStream());
	        out = new BufferedOutputStream(asocket.getOutputStream());
		
		 

		
		
		
	        while (input.read(tempBuffer) != -1) {
	        	
	            sourceDataLine.write(tempBuffer, 0, Size);

	        }
	        
		}
		
	    catch (LineUnavailableException e) {
	    	e.printStackTrace();
	    }
	    
	    catch (HeadlessException e) {
	    	
	    	e.printStackTrace();
	    }
	    
	    catch (UnknownHostException e){
	    	
	    }
		
		catch (IOException e) {

	        e.printStackTrace();
	    }
		
		
	        
	        
		//}).start();
	}
	
	private AudioFormat getAudioFormat() {
	    float sampleRate = 8000.0F;
	    int sampleSizeInBits = 16;
	    int channels = 2;
	    boolean signed = true;
	    boolean bigEndian = false;
	    return new AudioFormat(
	            sampleRate,
	            sampleSizeInBits,
	            channels,
	            signed,
	            bigEndian);
	}
	
	private void captureAudio() {
	    try {

	        audioFormat = getAudioFormat();
	        DataLine.Info dataLineInfo = new DataLine.Info(
	                TargetDataLine.class, audioFormat);
	        Mixer mixer = null;
	        System.out.println("Server Ip Address "+InetAddress.getLocalHost().getHostAddress());
	        System.out.println("Available Hardware Devices:");
	        for (int cnt = 0; cnt < mixerInfo.length; cnt++) {
	            mixer = AudioSystem.getMixer(mixerInfo[3]);      // Select Available Hardware Devices for the micro, for my Notebook it is number 3
	            if (mixer.isLineSupported(dataLineInfo)) {
	                System.out.println(cnt+":"+mixerInfo[cnt].getName());
	                targetDataLine = (TargetDataLine) mixer.getLine(dataLineInfo);
	            }
	        }
	        targetDataLine.open(audioFormat);
	        targetDataLine.start();

	        Thread captureThread = new CaptureThread();
	        captureThread.start();
	    } catch (Exception e) {
	        System.out.println(e);
	        System.exit(0);
	    }
	}

	class CaptureThread extends Thread {

	    byte tempBuffer[] = new byte[Size];

	    @Override
	    public void run() {
	        try {
	            while (true) {
	                int cnt = targetDataLine.read(tempBuffer, 0, tempBuffer.length);
	                out.write(tempBuffer);
	                out.flush();

	            }

	        } catch (Exception e) {
	            System.out.println(e);
	            System.exit(0);
	        }
	    }
	}

}
