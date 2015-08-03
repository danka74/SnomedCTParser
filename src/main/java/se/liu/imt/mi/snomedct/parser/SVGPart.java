/**
 * 
 */
package se.liu.imt.mi.snomedct.parser;

/**
 * @author daniel
 *
 */
public class SVGPart {

	public SVGPart() {
		super();
		this.indent = 0;
		this.height = 0;
		this.part = new StringBuffer();
	}

	public SVGPart(int indent, int height, String part) {
		super();
		this.indent = indent;
		this.height = height;
		this.part = new StringBuffer(part);
	}

	public String getSVG() {
		return part.toString();
	}

	public SVGPart append(SVGPart svgPart) {
		if (svgPart != null) {
			this.part.append(svgPart.part);
			this.height += svgPart.height;
		}
		return this;
	}
	
	public SVGPart groupWrap(String svg, int x, int y) {
		this.part.insert(0, "<g transform=\"translate("+ x +"," + y + ")\">");
		this.part.append("</g>");
		return this;
	}

	protected int indent;
	protected int height;
	protected StringBuffer part;
}
