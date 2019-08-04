package ivanludvig.readpdf;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.encryption.InvalidPasswordException;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.graphics.PDXObject;
import org.apache.pdfbox.pdmodel.graphics.form.PDFormXObject;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.fit.pdfdom.PDFDomTree;
import org.w3c.dom.Document;

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
    
    String xml;
    int n = 0;
	

	public static void main(String[] args) throws InvalidPasswordException, IOException, ParserConfigurationException, TransformerException {
		main = new Main();

		main.paragraphs = new ArrayList<Paragraph>();
		
		PDDocument document = PDDocument.load(new File("res/sample1.pdf"));
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
	    
	    int lastp = 0;
	    int currentp = 0;
	    int total = 0;
	    int totalp = 0;
	    for(int i = 1; i<=document.getPages().getCount(); i++) {
	    	stripper.setStartPage(i);
	    	stripper.setEndPage(i);
	    	String pageText = stripper.getText(document).replace("\n", " ").replace("\r", " ").replace("<p>", "  ");
	    	total+=pageText.length();
	    	while((totalp<total)&&(currentp<(main.paragraphs.size()-1))) {
	    		totalp+=main.paragraphs.get(currentp).text.length();
	    		currentp++;
	    	}
    		for(int j = lastp; j<=currentp; j++) {
    			main.paragraphs.get(j).setPage(i);
    		}
	    	lastp=currentp+1;
	    }
	    
		PDFDomTree parser = new PDFDomTree();
		// parse the file and get the DOM Document
		Document dom = parser.createDOM(document);
		
		/*
		OutputFormat format = new OutputFormat(dom);
		format.setIndenting(true);
		XMLSerializer serializer = new XMLSerializer(System.out, format);
		serializer.serialize(dom);  */
		
		//System.out.println("XML IN String format is: \n" + writer.toString());
		
		main.findImages(document);
		
		document.close();

		Collections.sort(main.images, Comparator.comparingInt(TheImage -> TheImage.page));
		//Collections.reverse(main.images);

		try {
			main.newpdf = new PDDocument();
			PDPage page = new PDPage();
			main.newpdf.addPage(page);
			
			main.contentStream = new PDPageContentStream(main.newpdf, page);
			//main.pdfFont = PDType1Font.HELVETICA_BOLD_OBLIQUE;
			main.pdfFont = PDType0Font.load(main.newpdf, new File("fonts/serif.ttf"));
			
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

		    main.newpdf.save(new File("output", "sample1.pdf"));
			
			
		}finally {
			main.newpdf.close();
		}
		
		
	}
	
	int currentpage = 0;
	int imgheight = 0;
	
	public void writeParagraph(Paragraph p) throws IOException {
		
		if(p.page>=main.currentpage) {
			main.currentpage = p.page;
		}
		while(!main.images.isEmpty()) {
			if((main.images.get(0).page)<=(main.currentpage)) {
				//System.out.println(main.currentpage);
				//byte[] imageBytes = javax.xml.bind.DatatypeConverter.parseBase64Binary(images.get(0));
				//BufferedImage image = ImageIO.read(new ByteArrayInputStream(imageBytes));
			    //BufferedImage awtImage = ImageIO.read( new File( "res/tirnanog.png" ) );
				BufferedImage image = main.images.get(0).getImage();
			    PDImageXObject  img = LosslessFactory.createFromImage(main.newpdf, image);
			    float width = img.getWidth();
			    float height = img.getHeight();
			    if(width>(mediabox.getWidth()-(2*margin))) {
			    	height = height * ( (mediabox.getWidth()-(2*margin)) / width);
			    	width = mediabox.getWidth()-(2*margin);
			    }
			    if(height>(mediabox.getHeight()-(2*margin))) {
			    	width = width * ( (mediabox.getHeight()-(2*margin)) / height);
			    	height = (mediabox.getHeight()-(2*margin));
			    }
			    if(height>(mediabox.getHeight()-((n*main.leading)+(main.margin))-imgheight)) {
			    	addPage();
			    }
			    float x = margin;
			    float y = (mediabox.getHeight() - (main.leading*n+(main.margin)))-height-imgheight;
				main.contentStream.endText();
				if(imgheight>0) {
					contentStream.drawImage(img, x, y+(2*main.margin), width, height);
				}else {
					contentStream.drawImage(img, x, y, width, height);
				}
			    //n+=Math.ceil((height+(2*margin))/main.leading);
			    main.images.remove(0);
			    main.imgheight += height+(2*main.margin);
			    main.contentStream.beginText();
			    main.contentStream.newLineAtOffset(startX, startY-imgheight);
			    //main.contentStream.newLineAtOffset(0, -main.leading);
			}else {
				break;
			}

		}
	    for (String line: p.lines){
	    	float charSpacing = 0;
	    	if (line.length() > 1){
	    		float size = main.fontsize * main.pdfFont.getStringWidth(line) / 1000;
	    		float free = width - size;
	    		if (free > 0){
	    			charSpacing = free / (line.length() - 1);
	    		}
	    	}
	    	if( ((main.leading*n)+imgheight) >= (mediabox.getHeight()-(margin)) ) {
	    		main.addPage();
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
	    	n++;
	    }
	}
	
	public void addPage() throws IOException {
		main.contentStream.endText();
		main.contentStream.close();
		main.contentStream = new PDPageContentStream(main.newpdf, main.newPage());
	    main.contentStream.beginText();
	    main.contentStream.setFont(main.pdfFont, main.fontsize);
	    main.contentStream.newLineAtOffset(startX, startY);
	    main.imgheight = 0;
    	n=1;
	}
	
	
	public PDPage newPage() {
		PDPage extrapage = new PDPage();
		main.newpdf.addPage(extrapage);
		return extrapage;
	}
	
	ArrayList<String> lines = new ArrayList<String>();
	ArrayList<TheImage> images = new ArrayList<TheImage>();
	
	public void findImages(PDDocument document) throws IOException {
        int n = 0;
	    for (PDPage page : document.getPages()) {
	        main.images.addAll(getImagesFromResources(page.getResources(), n));
	        n++;
	    }

	}

	private List<TheImage> getImagesFromResources(PDResources resources, int page) throws IOException {
	    List<TheImage> images = new ArrayList<>();
	
	    for (COSName xObjectName : resources.getXObjectNames()) {
	        PDXObject xObject = resources.getXObject(xObjectName);
	
	        if (xObject instanceof PDFormXObject) {
	            images.addAll(getImagesFromResources(((PDFormXObject) xObject).getResources(), page));
	        } else if (xObject instanceof PDImageXObject) {
	            images.add( new TheImage (((PDImageXObject) xObject).getImage(), page));
	        }
	    }
	
	    return images;
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
