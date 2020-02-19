package ivanludvig.readpdf;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

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
import org.fit.pdfdom.PDFDomTree;
import org.w3c.dom.Document;

public class Main {
	
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
	

	public Main(String input, String output, String filename) throws InvalidPasswordException, IOException, ParserConfigurationException, TransformerException {
		paragraphs = new ArrayList<Paragraph>();
		
		PDDocument document = PDDocument.load(new File(input));
	    PDFTextStripper stripper = new PDFTextStripper();
	    stripper.setParagraphStart("<p>");
	    stripper.setLineSeparator(" ");
	    //stripper.setParagraphEnd("/t");
	    stripper.setWordSeparator(" ");

	    for (String p: stripper.getText(document).split(stripper.getParagraphStart())){
	    	if(isNumber(p)) {
	    		continue;
	    	}
	    	paragraphs.add(new Paragraph(("  "+p).replace("\n", " ").replace("\r", " ")));
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
	    	while((totalp<total)&&(currentp<(paragraphs.size()-1))) {
	    		totalp+=paragraphs.get(currentp).text.length();
	    		currentp++;
	    	}
    		for(int j = lastp; j<=currentp; j++) {
    			paragraphs.get(j).setPage(i);
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
		
		findImages(document);
		
		document.close();

		Collections.sort(images, Comparator.comparingInt(TheImage -> TheImage.page));
		//Collections.reverse(images);

		try {
			newpdf = new PDDocument();
			PDPage page = new PDPage();
			newpdf.addPage(page);
			
			contentStream = new PDPageContentStream(newpdf, page);
			//pdfFont = PDType1Font.HELVETICA_BOLD_OBLIQUE;
			pdfFont = PDType0Font.load(newpdf, new File("fonts/serif.ttf"));
			
			mediabox = page.getMediaBox();
			startX = mediabox.getLowerLeftX() + margin;
			startY = mediabox.getUpperRightY() - margin;
			width = mediabox.getWidth() - 2*margin;
			
		    
		    contentStream.beginText();
		    contentStream.setFont(pdfFont, fontsize);
		    contentStream.newLineAtOffset(startX, startY);
		    
		    for(Paragraph p : paragraphs) {
		    	p.format(pdfFont, fontsize, width);
		    	writeParagraph(p);
		    }
		    
		    contentStream.endText(); 
		    contentStream.close();

		    newpdf.save(new File(output, filename.split(".pdf")[0]+"_coverted.pdf"));
		}finally {
			newpdf.close();
		}
		
		
	}
	
	int currentpage = 0;
	int imgheight = 0;
	
	public void writeParagraph(Paragraph p) throws IOException {
		
		if(p.page>=currentpage) {
			currentpage = p.page;
		}
		while(!images.isEmpty()) {
			if((images.get(0).page)<=(currentpage)) {
				//System.out.println(currentpage);
				//byte[] imageBytes = javax.xml.bind.DatatypeConverter.parseBase64Binary(images.get(0));
				//BufferedImage image = ImageIO.read(new ByteArrayInputStream(imageBytes));
			    //BufferedImage awtImage = ImageIO.read( new File( "res/tirnanog.png" ) );
				BufferedImage image = images.get(0).getImage();
			    PDImageXObject  img = LosslessFactory.createFromImage(newpdf, image);
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
			    if(height>(mediabox.getHeight()-((n*leading)+(margin))-imgheight)) {
			    	addPage();
			    }
			    float x = margin;
			    float y = (mediabox.getHeight() - (leading*n+(margin)))-height-imgheight;
				contentStream.endText();
				if(imgheight>0) {
					contentStream.drawImage(img, x, y+(2*margin), width, height);
				}else {
					contentStream.drawImage(img, x, y, width, height);
				}
			    //n+=Math.ceil((height+(2*margin))/leading);
			    images.remove(0);
			    imgheight += height+(2*margin);
			    contentStream.beginText();
			    contentStream.newLineAtOffset(startX, startY-imgheight);
			    //contentStream.newLineAtOffset(0, -leading);
			}else {
				break;
			}

		}
	    for (String line: p.lines){
	    	float charSpacing = 0;
	    	if (line.length() > 1){
	    		float size = fontsize * pdfFont.getStringWidth(line) / 1000;
	    		float free = width - size;
	    		if (free > 0){
	    			charSpacing = free / (line.length() - 1);
	    		}
	    	}
	    	if( ((leading*n)+imgheight) >= (mediabox.getHeight()-(margin)) ) {
	    		addPage();
	    	}
	    	if(p.lines.indexOf(line)==(p.lines.size()-1)) {
	    		//System.out.println(p.lines.indexOf(line)+" "+(p.lines.size()-1));
	    		contentStream.setCharacterSpacing(0);
	    	}else {
	    		contentStream.setCharacterSpacing(charSpacing);
	    	}
	    	//contentStream.setCharacterSpacing(charSpacing);
	    	contentStream.showText(line);
	    	contentStream.newLineAtOffset(0, -leading);
	    	n++;
	    }
	}
	
	public void addPage() throws IOException {
		contentStream.endText();
		contentStream.close();
		contentStream = new PDPageContentStream(newpdf, newPage());
	    contentStream.beginText();
	    contentStream.setFont(pdfFont, fontsize);
	    contentStream.newLineAtOffset(startX, startY);
	    imgheight = 0;
    	n=1;
	}
	
	
	public PDPage newPage() {
		PDPage extrapage = new PDPage();
		newpdf.addPage(extrapage);
		return extrapage;
	}
	
	ArrayList<String> lines = new ArrayList<String>();
	ArrayList<TheImage> images = new ArrayList<TheImage>();
	
	public void findImages(PDDocument document) throws IOException {
        int n = 0;
	    for (PDPage page : document.getPages()) {
	        images.addAll(getImagesFromResources(page.getResources(), n));
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
