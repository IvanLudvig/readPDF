package ivanludvig.readpdf;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

public class TheImage {
	
	BufferedImage image;
	public int page;
	
	public TheImage(String base64, int page) throws IOException {
		byte[] imageBytes = javax.xml.bind.DatatypeConverter.parseBase64Binary(base64);
		image = ImageIO.read(new ByteArrayInputStream(imageBytes));
		this.page = page;
		System.out.println(page);
	}
	
	public TheImage(BufferedImage image, int page) throws IOException {
		this.image = image;
		this.page = page;
		System.out.println(page);
	}
	
	public BufferedImage getImage() {
		return image;
	}

}
