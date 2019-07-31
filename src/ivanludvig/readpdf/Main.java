package ivanludvig.readpdf;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.encryption.InvalidPasswordException;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.text.PDFTextStripper;

public class Main {
	
	static Main main;
	
	PDPageContentStream contentStream;
	PDDocument newpdf;
	PDRectangle mediabox;
	PDFont pdfFont;
	
	ArrayList<Paragraph> paragraphs;
	
	float fontsize = 20;
	float leading = 1.25f * fontsize;
	float margin = 54;
	float startX;
	float startY;
    float width;
    
    int n = 0;
	

	public static void main(String[] args) throws InvalidPasswordException, IOException {
		main = new Main();
		
		main.paragraphs = new ArrayList<Paragraph>();
		
		PDDocument document = PDDocument.load(new File("res/Cheerleader_effect.pdf"));
	    PDFTextStripper stripper = new PDFTextStripper();
	    stripper.setParagraphStart("<p>");
	    stripper.setLineSeparator(" ");
	    //stripper.setParagraphEnd("/t");
	    stripper.setWordSeparator(" ");

	    for (String p: stripper.getText(document).split(stripper.getParagraphStart())){
	    	if(main.isNumber(p)) {
	    		continue;
	    	}
	    	main.paragraphs.add(new Paragraph(("  "+p).replace("\n", " ").replace("\r", " ")));
	    }
		document.close();
		
		try {
			main.newpdf = new PDDocument();
			PDPage page = new PDPage();
			main.newpdf.addPage(page);
			
			main.contentStream = new PDPageContentStream(main.newpdf, page);
			//main.pdfFont = PDType1Font.HELVETICA_BOLD_OBLIQUE;
			main.pdfFont = PDType0Font.load(main.newpdf, new File("fonts/lucida-sans-unicode.ttf"));
			
			main.mediabox = page.getMediaBox();
			main.startX = main.mediabox.getLowerLeftX() + main.margin;
			main.startY = main.mediabox.getUpperRightY() - main.margin;
			main.width = main.mediabox.getWidth() - 2*main.margin;
			
		    
		    main.contentStream.beginText();
		    main.contentStream.setFont(main.pdfFont, main.fontsize);
		    main.contentStream.newLineAtOffset(main.startX, main.startY);
		    
		    for(Paragraph p : main.paragraphs) {
		    	p.format(main.pdfFont, main.fontsize, main.width);
		    	main.writeParagraph(p);
		    }
		    
		    main.contentStream.endText(); 
		    main.contentStream.close();

		    main.newpdf.save(new File("output", "Cheerleader_effect.pdf"));
			
			
		}finally {
			main.newpdf.close();
		}
		
        
	}
	
	public PDPage addPage() {
		PDPage extrapage = new PDPage();
		main.newpdf.addPage(extrapage);
		return extrapage;
	}
	
	public void writeParagraph(Paragraph p) throws IOException {
	    for (String line: p.lines){
	    	float charSpacing = 0;
	    	if (line.length() > 1){
	    		float size = main.fontsize * main.pdfFont.getStringWidth(line) / 1000;
	    		float free = width - size;
	    		if (free > 0){
	    			charSpacing = free / (line.length() - 1);
	    		}
	    		n++;
	    	}
	    	if((main.leading*n)>=(mediabox.getHeight()-margin)) {
	    		main.contentStream.endText();
	    		main.contentStream.close();
				main.contentStream = new PDPageContentStream(main.newpdf, main.addPage());
			    main.contentStream.beginText();
			    main.contentStream.setFont(main.pdfFont, main.fontsize);
			    main.contentStream.newLineAtOffset(startX, startY);
				n=1;
	    	}
	    	if(p.lines.indexOf(line)==(p.lines.size()-1)) {
	    		//System.out.println(p.lines.indexOf(line)+" "+(p.lines.size()-1));
	    		main.contentStream.setCharacterSpacing(0);
	    	}else {
	    		main.contentStream.setCharacterSpacing(charSpacing);
	    	}
	    	//main.contentStream.setCharacterSpacing(charSpacing);
	    	main.contentStream.showText(line);
	    	main.contentStream.newLineAtOffset(0, -main.leading);
	    }
	}
	
	public boolean isNumber(String str) { 
		try {  
			Double.parseDouble(str);  
		    return true;
		} catch(NumberFormatException e){  
			return false;  
		}  
	}

}
