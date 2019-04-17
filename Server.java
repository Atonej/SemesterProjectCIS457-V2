import java.awt.BorderLayout;
import java.awt.HeadlessException;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;
import javax.swing.ImageIcon;


public class Server {
private ServerSocket MyService;
private Socket clientSocket = null;
private InputStream input;
private TargetDataLine targetDataLine;
private OutputStream out;
private AudioFormat audioFormat;
private SourceDataLine sourceDataLine;
int Size = 10000;
byte tempBuffer[] = new byte[Size];
static Mixer.Info[] mixerInfo = AudioSystem.getMixerInfo();

Server() {

    
    try {
        Mixer mixer_ = AudioSystem.getMixer(mixerInfo[1]);   // Select Available Hardware Devices for the speaker, for my Notebook it is number 1
        audioFormat = getAudioFormat();
        DataLine.Info dataLineInfo = new DataLine.Info(SourceDataLine.class, audioFormat);
        sourceDataLine = (SourceDataLine) AudioSystem.getLine(dataLineInfo);
        sourceDataLine.open(audioFormat);
        sourceDataLine.start();
        MyService = new ServerSocket(500);
        clientSocket = MyService.accept();
        captureAudio();
        input = new BufferedInputStream(clientSocket.getInputStream());
        out = new BufferedOutputStream(clientSocket.getOutputStream());
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

public static void main(String s[]) throws LineUnavailableException, UnknownHostException {
    Server s2 = new Server();
    
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
}}