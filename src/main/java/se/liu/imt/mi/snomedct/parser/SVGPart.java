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
		this.part = "";
	}

	public SVGPart(int indent, int height, String part) {
		super();
		this.indent = indent;
		this.height = height;
		this.part = part;
	}

	public String getSVG() {
		return part.toString();
	}

	public SVGPart append(SVGPart svgPart) {
		if (svgPart != null) {
			this.part += "<g transform=\"translate("+ this.indent +"," + this.height + ")\">" + svgPart.part + "</g>";
			this.height += svgPart.height;
			this.indent += svgPart.indent;
		}
		return this;
	}

	public SVGPart append(SVGPart svgPart, int indent, int height) {
		if (svgPart != null) {
			this.part += "<g transform=\"translate("+ (this.indent + indent) +"," + (this.height + height) + ")\">" + svgPart.part + "</g>";
			this.height += svgPart.height;
			this.indent += svgPart.indent;
		}
		return this;
	}

	public SVGPart appendNoG(SVGPart svgPart) {
		if (svgPart != null) {
			this.part += svgPart.part;
			this.height += svgPart.height;
			this.indent += svgPart.indent;
		}
		return this;
	}

	protected int indent;
	protected int height;
	protected String part;
	
	public void adjust(int indent, int height) {
		this.indent += indent;
		this.height += height;
	}

	public int getHeight() {
		// TODO Auto-generated method stub
		return height;
	}
	
	public void setHeight(int height) {
		this.height = height;
	}
	
	public int getIndent() {
		return this.indent;
	}
	
	public void setIndent(int indent) {
		this.indent = indent;
	}

//	public void setPre(String svgPre) {
//		this.part = svgPre + this.part;
//	}
	
	public String toString() {
		return "h: " + this.height + ", i: " + this.indent;
	}
}
