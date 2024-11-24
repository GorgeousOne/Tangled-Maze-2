package me.gorgeousone.tangledmaze.badapple;

import org.bukkit.Bukkit;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.IOException;

public class FrameLoader {
	
	private static final String FRAME_URL = "plugins/frames/frame%04d.bmp";
	public static final int WIDTH = 160;
	public static final int HEIGHT = 120;
	public static final int FRAME_COUNT = 6570;
	
	public static byte[] loadFrame(int index) {
		try {
			//			BufferedImage.GRAY
			File file = new File(String.format(FRAME_URL, index));
			Bukkit.broadcastMessage("Load " + file.getAbsolutePath());
			BufferedImage frame = ImageIO.read(file);
			return ((DataBufferByte) frame.getRaster().getDataBuffer()).getData();
		}catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}