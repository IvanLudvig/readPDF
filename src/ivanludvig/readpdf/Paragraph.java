package ivanludvig.readpdf;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.pdfbox.pdmodel.font.PDFont;

public class Paragraph {
	
	String text;
	ArrayList<String> lines;
	
	public Paragraph(String text) {
		this.text = text;
	}
	
	public void format(PDFont pdfFont, float fontsize, float width) throws IOException {
		text = normal(text, pdfFont);
		lines = new ArrayList<String>();
	    int lastSpace = -1;
	    while (text.length() > 0){
	        int spaceIndex = text.indexOf(' ', lastSpace + 1);
	        if (spaceIndex < 0)
	            spaceIndex = text.length();
	        String subString = text.substring(0, spaceIndex);
	        
	        float size  = fontsize * pdfFont.getStringWidth(subString) / 1000;
	        //System.out.printf("'%s' - %f of %f\n", subString, size, width);
	        if (size > width){
	            if (lastSpace < 0) {
	                lastSpace = spaceIndex;
	            }
	            subString = text.substring(0, lastSpace);
	            lines.add(subString);
	            text = text.substring(lastSpace).trim();
	            lastSpace = -1;
	        }
	        else if (spaceIndex == text.length()){
	            lines.add(text);
	            text = "";
	        }
	        else{
	            lastSpace = spaceIndex;
	        }
	    }
	}
	
	private String normal(String str, PDFont pdfFont) throws IOException {
		String s="";
		for(char ch : str.toCharArray()) {
		    try {
		    	pdfFont.encode(Character.toString(ch));
		        s+=ch;
		    } catch (IllegalArgumentException iae) {
		    	s+="<?>";
		    	System.out.println("ILLEGAL CHAR");
		    }
		}
		return s;
	}

}
