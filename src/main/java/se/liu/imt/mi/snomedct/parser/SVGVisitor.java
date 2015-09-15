/**
 *
 */
package se.liu.imt.mi.snomedct.parser;

import java.awt.Font;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.util.HashMap;

import org.apache.log4j.Logger;

import se.liu.imt.mi.snomedct.expression.SNOMEDCTExpressionBaseVisitor;
import se.liu.imt.mi.snomedct.expression.SNOMEDCTExpressionLexer;
import se.liu.imt.mi.snomedct.expression.SNOMEDCTExpressionParser.AttributeContext;
import se.liu.imt.mi.snomedct.expression.SNOMEDCTExpressionParser.AttributeGroupContext;
import se.liu.imt.mi.snomedct.expression.SNOMEDCTExpressionParser.AttributeSetContext;
import se.liu.imt.mi.snomedct.expression.SNOMEDCTExpressionParser.ConceptReferenceContext;
import se.liu.imt.mi.snomedct.expression.SNOMEDCTExpressionParser.FocusConceptContext;
import se.liu.imt.mi.snomedct.expression.SNOMEDCTExpressionParser.NonGroupedAttributeSetContext;
import se.liu.imt.mi.snomedct.expression.SNOMEDCTExpressionParser.RefinementContext;
import se.liu.imt.mi.snomedct.expression.SNOMEDCTExpressionParser.StatementContext;
import se.liu.imt.mi.snomedct.expression.SNOMEDCTExpressionParser.SubExpressionContext;

/**
 * @author Daniel Karlsson, daniel.karlsson@liu.se
 *
 *         Visitor class for generating SVG output from SNOMED CT expressions.
 *         Opted to use string representation of SVG in order to reuse SVG from
 *         the IHTSDO Browser.
 *
 */
public class SVGVisitor extends SNOMEDCTExpressionBaseVisitor<SVGPart> {

	static Logger logger = Logger.getLogger(SVGVisitor.class);

	private HashMap<Long, Boolean> concepts = null;

	private Boolean fullyDefinedDefault = false;

	private Boolean definiens = true;

	public SVGVisitor() {
		super();
		concepts = null;
		fullyDefinedDefault = false;
	}

	public SVGVisitor(HashMap<Long, Boolean> concepts,
			Boolean fullyDefinedDefault) {
		super();
		this.concepts = concepts;
		this.fullyDefinedDefault = fullyDefinedDefault;
	}

	@Override
	protected SVGPart aggregateResult(SVGPart aggregate, SVGPart nextResult) {
		if (aggregate != null)
			return aggregate.append(nextResult);
		else
			return nextResult;
	}

	private String svgPre = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n"
			+ "<svg xmlns=\"http://www.w3.org/2000/svg\" id=\"SctDiagram\"><defs id=\"SctDiagramsDefs\">\n"
			+ "<marker id=\"BlackTriangle\" refX=\"0\" refY=\"10\" markerWidth=\"8\"\n"
			+ "markerHeight=\"6\" orient=\"auto\" viewBox=\"0 0 22 20\" markerUnits=\"strokeWidth\"\n"
			+ "fill=\"black\" stroke=\"black\" stroke-width=\"2\">\n"
			+ "<path d=\"M 0 0 L 20 10 L 0 20 z\"></path>\n"
			+ "</marker>\n"
			+ "<marker id=\"ClearTriangle\" refX=\"0\" refY=\"10\" markerWidth=\"8\"\n"
			+ "markerHeight=\"8\" orient=\"auto\" viewBox=\"0 0 22 20\" markerUnits=\"strokeWidth\"\n"
			+ "fill=\"white\" stroke=\"black\" stroke-width=\"2\">\n"
			+ "<path d=\"M 0 0 L 20 10 L 0 20 z\"></path>\n"
			+ "</marker>\n"
			+ "<marker id=\"LineMarker\" refX=\"0\" refY=\"10\" markerWidth=\"8\"\n"
			+ "markerHeight=\"8\" orient=\"auto\" viewBox=\"0 0 22 20\" markerUnits=\"strokeWidth\"\n"
			+ "fill=\"white\" stroke=\"black\" stroke-width=\"2\">\n"
			+ "<path d=\"M 0 10 L 20 10\"></path>\n</marker>\n" + "</defs>";

	private String svgPost = "</svg>";

	private String svgEquivalentTo = "<polyline points=\"0,-10 0,30 20,30\" id=\"poly1\" fill=\"none\"\n"
			+ "stroke=\"black\" stroke-width=\"2\" marker-end=\"url(#BlackTriangle)\" />\n"
			+ "<circle cx=\"52\" cy=\"30\" r=\"20\" fill=\"white\" stroke=\"black\"\n"
			+ "stroke-width=\"2\" />\n"
			+ "<line x1=\"45\" y1=\"25\" x2=\"59\" y2=\"25\" stroke=\"black\"\n"
			+ "stroke-width=\"2\" />\n"
			+ "<line x1=\"45\" y1=\"30\" x2=\"59\" y2=\"30\" stroke=\"black\"\n"
			+ "stroke-width=\"2\" />\n"
			+ "<line x1=\"45\" y1=\"35\" x2=\"59\" y2=\"35\" stroke=\"black\"\n"
			+ "stroke-width=\"2\" />\n"
			+ "<polyline points=\"72,30 84,30\" fill=\"none\" stroke=\"black\" stroke-width=\"2\"/>\n";

	private String svgSubclassOf = "<polyline points=\"0,-10 00,30 20,30\" id=\"poly1\" fill=\"none\"\n"
			+ "stroke=\"black\" stroke-width=\"2\" marker-end=\"url(#BlackTriangle)\" />"
			+ "<circle cx=\"52\" cy=\"30\" r=\"20\" fill=\"white\" stroke=\"black\"\n"
			+ "stroke-width=\"2\" />\n"
			+ "<line x1=\"45\" y1=\"22\" x2=\"58\" y2=\"22\" stroke=\"black\"\n"
			+ "stroke-width=\"2\"></line>\n"
			+ "<line x1=\"45\" y1=\"33\" x2=\"58\" y2=\"33\" stroke=\"black\"\n"
			+ "stroke-width=\"2\"></line>\n"
			+ "<line x1=\"46\" y1=\"22\" x2=\"46\" y2=\"33\" stroke=\"black\"\n"
			+ "stroke-width=\"2\"></line>\n"
			+ "<line x1=\"45\" y1=\"37\" x2=\"58\" y2=\"37\" stroke=\"black\"\n"
			+ "stroke-width=\"2\"></line>\n"
			+ "<polyline points=\"72,30 84,30\" fill=\"none\" stroke=\"black\" stroke-width=\"2\"/>\n";

	@Override
	public SVGPart visitStatement(StatementContext ctx) {
		logger.debug("visitStatement");
		SVGPart result = new SVGPart(0, 0, svgPre);

		definiens = true;

		if (ctx.subExpression(0).getChildCount() == 1
				&& ctx.subExpression(0).getChild(0).getChildCount() == 1) {
			SVGPart x = visitSubExpression(ctx.subExpression(0));
			result.append(x);
		}
		// else throw not implemented

		logger.debug(result.toString());
		
		definiens = false;

		if (ctx.definitionStatus().start.getType() == SNOMEDCTExpressionLexer.EQ_TO)
			result.append(new SVGPart(124, 10, svgEquivalentTo), 40, 0);
		else
			result.append(new SVGPart(124, 10, svgSubclassOf), 40, 0);

		result.append(visitSubExpression(ctx.subExpression(1)));
		
		logger.debug(result.toString());
		
		result.appendNoG(new SVGPart(0, 0, svgPost));

		return result;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see se.liu.imt.mi.snomedct.expression.SNOMEDCTExpressionBaseVisitor#
	 * visitFocusConcept
	 * (se.liu.imt.mi.snomedct.expression.SNOMEDCTExpressionParser
	 * .FocusConceptContext)
	 */
	@Override
	public SVGPart visitFocusConcept(FocusConceptContext ctx) {
		logger.debug("visitFocusConcept");
		
		if (ctx.getChildCount() == 1) {
			if (definiens)
				return visit(ctx.getChild(0));
			// add single arrow
			String svgArrow = "<polyline points=\"0,20 12,20\" fill=\"none\" stroke=\"black\" stroke-width=\"2\" marker-end=\"url(#ClearTriangle)\"/>\n";
			SVGPart result = new SVGPart(0, 0, svgArrow);
			result.append(visit(ctx.getChild(0)), 28, 0);
			logger.debug(result.toString());
			return result;
		} else {
			String svg = "<circle cx=\"10\" cy=\"20\" r=\"10\" fill=\"black\" stroke=\"black\" stroke-width=\"2\" />\n";
			SVGPart result = new SVGPart(20, 0, svg);
			for (int i = 0; i < ctx.getChildCount(); i++) {
				String svgArrow = "";
				if (ctx.getChild(i).getClass() == ConceptReferenceContext.class) {
					if (i == 0)
						svgArrow = "<polyline points=\"0,20 14,20\" fill=\"none\" stroke=\"black\" stroke-width=\"2\" marker-end=\"url(#ClearTriangle)\"/>\n";
					else
						svgArrow = "<polyline points=\"-10,-20 -10,20 14,20\" id=\"poly1\" fill=\"none\"\n"
								+ "stroke=\"black\" stroke-width=\"2\" marker-end=\"url(#ClearTriangle)\" />";
				} else
					continue;

				result.append(new SVGPart(0, 0, svgArrow), 0, 0);
				result.append(visit(ctx.getChild(i)), 28, 0);
			}
			logger.debug(result.toString());
			return result;
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see se.liu.imt.mi.snomedct.expression.SNOMEDCTExpressionBaseVisitor#
	 * visitAttributeSet
	 * (se.liu.imt.mi.snomedct.expression.SNOMEDCTExpressionParser
	 * .AttributeSetContext)
	 */
	@Override
	public SVGPart visitAttributeSet(AttributeSetContext ctx) {
		logger.debug("visitAttributeSet");
		if (ctx.getChildCount() == 1) {
			// add single arrow
			String svgArrow = "<polyline points=\"0,20 14,20\" fill=\"none\" stroke=\"black\" stroke-width=\"2\" marker-end=\"url(#BlackTriangle)\"/>\n";
			SVGPart result = new SVGPart(28, 0, svgArrow);
			result.append(visit(ctx.getChild(0)), 0, 0);
			logger.debug(result.toString());
			return result;
		} else {
			String svg = "<polyline points=\"0,20 12,20\" fill=\"none\" stroke=\"black\" stroke-width=\"2\" marker-end=\"url(#BlackTriangle)\"/>\n"
					+ "<circle cx=\"36\" cy=\"20\" r=\"10\" fill=\"black\" stroke=\"black\" stroke-width=\"2\" />\n";
			SVGPart result = new SVGPart(16, 0, svg);
			for (int i = 0; i < ctx.getChildCount(); i++) {
				String svgArrow = "";
				if (ctx.getChild(i).getClass() == AttributeContext.class) {
					if (i == 0)
						svgArrow = "<polyline points=\"0,20 14,20\" fill=\"none\" stroke=\"black\" stroke-width=\"2\" marker-end=\"url(#BlackTriangle)\"/>\n";
					else
						svgArrow = "<polyline points=\"-10,-20 -10,20 14,20\" id=\"poly1\" fill=\"none\"\n"
								+ "stroke=\"black\" stroke-width=\"2\" marker-end=\"url(#BlackTriangle)\" />";
				} else
					continue;

				result.append(new SVGPart(0, 0, svgArrow), 28, 0);
				result.append(visit(ctx.getChild(i)), 56, 0);
			}
			logger.debug(result.toString());
			return result;
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see se.liu.imt.mi.snomedct.expression.SNOMEDCTExpressionBaseVisitor#
	 * visitNonGroupedAttributeSet
	 * (se.liu.imt.mi.snomedct.expression.SNOMEDCTExpressionParser
	 * .NonGroupedAttributeSetContext)
	 */
	@Override
	public SVGPart visitNonGroupedAttributeSet(NonGroupedAttributeSetContext ctx) {
		logger.debug("visitNonGroupedAttributeSet");
		if (ctx.getChildCount() == 1) {
			return visit(ctx.getChild(0));
		} else {
			SVGPart result = new SVGPart();
			int height = 0;
			for (int i = 0; i < ctx.getChildCount(); i++) {
				String svgArrow = "";
				if (ctx.getChild(i).getClass() == AttributeContext.class) {
					if (i != 0)
						svgArrow = "<polyline points=\"-38,"
								+ (-height - 20)
								+ " -38,20 -14,20\" id=\"poly1\" fill=\"none\"\n"
								+ "stroke=\"red\" stroke-width=\"2\" marker-end=\"url(#BlackTriangle)\" />";
				} else
					continue;

				result.append(new SVGPart(0, 0, svgArrow));
				SVGPart childPart = visit(ctx.getChild(i));
				height += childPart.getHeight();
				logger.debug("child: " + childPart.toString());
				result.append(childPart, 0, 0);
				//result.adjust(-40, 0);
			}
			logger.debug(result.toString());
			return result;
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see se.liu.imt.mi.snomedct.expression.SNOMEDCTExpressionBaseVisitor#
	 * visitAttributeGroup
	 * (se.liu.imt.mi.snomedct.expression.SNOMEDCTExpressionParser
	 * .AttributeGroupContext)
	 */
	@Override
	public SVGPart visitAttributeGroup(AttributeGroupContext ctx) {
		logger.debug("visitAttributeGroup");
		String svg = "<circle cx=\"20\" cy=\"20\" r=\"20\" fill=\"white\" stroke=\"black\" stroke-width=\"2\" />\n";// +
		// "<polyline points=\"40,20 60,20\" fill=\"none\" stroke=\"black\" stroke-width=\"2\"/>\n";
		SVGPart result = new SVGPart(40, 0, svg);
		result.append(visit(ctx.attributeSet()));
		logger.debug(result.toString());
		return result;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see se.liu.imt.mi.snomedct.expression.SNOMEDCTExpressionBaseVisitor#
	 * visitSubExpression
	 * (se.liu.imt.mi.snomedct.expression.SNOMEDCTExpressionParser
	 * .SubExpressionContext)
	 */
	@Override
	public SVGPart visitSubExpression(SubExpressionContext ctx) {
		logger.debug("visitSubExpression");
		if (ctx.getChildCount() == 1) {
			return visit(ctx.getChild(0));
		} else {
			String svg = // "<polyline points=\"0,20 12,20\" fill=\"none\" stroke=\"black\" stroke-width=\"2\"/>\n"
			"<circle cx=\"10\" cy=\"20\" r=\"10\" fill=\"black\" stroke=\"black\" stroke-width=\"2\" />\n";
			SVGPart result = new SVGPart(20, 0, svg);

			for (int i = 0; i < ctx.getChildCount(); i++) {
				String svgArrow = "";
				if (ctx.getChild(i).getClass() == FocusConceptContext.class) {
					// if (i == 0)
					// svgArrow =
					// "<polyline points=\"0,20 12,20\" fill=\"none\" stroke=\"black\" stroke-width=\"2\" marker-end=\"url(#ClearTriangle)\"/>\n";
					// else
					// svgArrow =
					// "<polyline points=\"-8,-20 -8,20 12,20\" id=\"poly1\" fill=\"none\"\n"
					// +
					// "stroke=\"black\" stroke-width=\"2\" marker-end=\"url(#ClearTriangle)\" />";
					SVGPart childPart = visit(ctx.getChild(i));
					result.append(childPart, 0, 0);
				} else if (ctx.getChild(i).getClass() == RefinementContext.class) {
					if (i == 0)
						svgArrow = "<polyline points=\"0,20 14,20\" fill=\"none\" stroke=\"blue\" stroke-width=\"2\" marker-end=\"url(#BlackTriangle)\"/>\n";
					else
						svgArrow = "<polyline points=\"-10,-20 -10,20 14,20\" id=\"poly1\" fill=\"none\"\n"
								+ "stroke=\"blue\" stroke-width=\"2\" marker-end=\"url(#BlackTriangle)\" />";
					result.append(new SVGPart(20, 0, svgArrow), 0, 0);
					SVGPart childPart = visit(ctx.getChild(i));
					result.append(childPart, 8, 0);

				} else
					continue;

			}
			logger.debug(result.toString());
			return result;
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see se.liu.imt.mi.snomedct.expression.SNOMEDCTExpressionBaseVisitor#
	 * visitConceptReference
	 * (se.liu.imt.mi.snomedct.expression.SNOMEDCTExpressionParser
	 * .ConceptReferenceContext)
	 *
	 * This method would need access to a concept table (at least) to determine
	 * if a concept is primitive or fully defined
	 */
	@Override
	public SVGPart visitConceptReference(ConceptReferenceContext ctx) {
		String term = removeCharacter(ctx.TERM().getText(), "|");
		logger.debug("visitConceptReference - " + term);

		int len = Math.max(getTextWidth(term), 60) + 24;

		Boolean isFullyDefined = fullyDefinedDefault;

		if (concepts != null) {
			Long sctid = Long.parseLong(ctx.SCTID().getText());
			isFullyDefined = concepts.get(sctid);
			if (isFullyDefined == null) // if id is missing
				isFullyDefined = fullyDefinedDefault;
		}

		String svg = "";
		if (isFullyDefined) {
			// fully defined
			svg = "<!-- "
					+ term
					+ "-->\n"
					+ "<rect x=\"0\" y=\"0\" width=\""
					+ len
					+ "\" height=\"40\" fill=\"#ccccff\" stroke=\"#333\"\n"
					+ "stroke-width=\"1\" />\n"
					+ "<rect x=\"2\" y=\"2\" width=\""
					+ (len - 4)
					+ "\" height=\"36\" id=\"rect16\" fill=\"#ccccff\"\n"
					+ "stroke=\"#333\" stroke-width=\"1\" />"
					+ "<text x=\"12\" y=\"17\"\n"
					+ "font-family=\"&quot;Helvetica Neue&quot;,Helvetica,Arial,sans-serif\"\n"
					+ "font-size=\"10\">"
					+ ctx.SCTID().getText()
					+ "</text>\n"
					+ "<text x=\"12\" y=\"31\" id=\"text61\"\n"
					+ "font-family=\"&quot;Helvetica Neue&quot;,Helvetica,Arial,sans-serif\"\n"
					+ "font-size=\"12\">" + term + "</text>";
		} else {
			// primitive
			svg = "<!-- "
					+ term
					+ "-->\n"
					+ "<rect x=\"0\" y=\"0\" width=\""
					+ len
					+ "\" height=\"40\" fill=\"#99ccff\" stroke=\"#333\"\n"
					+ "stroke-width=\"1\" />\n"
					+ "<text x=\"12\" y=\"17\"\n"
					+ "font-family=\"&quot;Helvetica Neue&quot;,Helvetica,Arial,sans-serif\"\n"
					+ "font-size=\"10\">"
					+ ctx.SCTID().getText()
					+ "</text>\n"
					+ "<text x=\"12\" y=\"31\" id=\"text61\"\n"
					+ "font-family=\"&quot;Helvetica Neue&quot;,Helvetica,Arial,sans-serif\"\n"
					+ "font-size=\"12\">" + term + "</text>";
		}
		SVGPart result = new SVGPart(0, 50, svg);
		logger.debug(result.toString());
		return result;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see se.liu.imt.mi.snomedct.expression.SNOMEDCTExpressionBaseVisitor#
	 * visitAttribute
	 * (se.liu.imt.mi.snomedct.expression.SNOMEDCTExpressionParser.
	 * AttributeContext)
	 */
	@Override
	public SVGPart visitAttribute(AttributeContext ctx) {
		String term = removeCharacter(ctx.conceptReference().TERM().getText(),
				"|");
		logger.debug("visitAttribute - " + term);

		int len = Math.max(getTextWidth(term), 60) + 24;

		SVGPart val = visit(ctx.attributeValue());

		String svg = "<!-- "
				+ term
				+ "-->\n"
				+ "<rect x=\"0\" y=\"0\" width=\""
				+ len
				+ "\" height=\"40\" rx=\"18\" ry=\"18\" id=\"rect56\"\n"
				+ "style=\"fill:#ffffcc;stroke:#333333;stroke-width:1\" />\n"
				+ "<rect x=\"2\" y=\"2\" width=\""
				+ (len - 4)
				+ "\" height=\"36\" rx=\"18\" ry=\"18\" id=\"rect17\"\n"
				+ "style=\"fill:#ffffcc;stroke:#333333;stroke-width:1\" />\n"
				+ "<text x=\"12\" y=\"17\"\n"
				+ "font-family=\"&quot;Helvetica Neue&quot;,Helvetica,Arial,sans-serif\"\n"
				+ "font-size=\"10\">"
				+ ctx.conceptReference().SCTID().getText()
				+ "</text>\n"
				+ "<text x=\"12\" y=\"31\" id=\"text61\"\n"
				+ "font-family=\"&quot;Helvetica Neue&quot;,Helvetica,Arial,sans-serif\"\n"
				+ "font-size=\"12\">" + term + "</text>\n" + "\n"
				+ "<polyline points=\"" + len + ",20 " + (len + 27)
				+ ",20\" fill=\"none\" stroke=\"black\"\n"
				+ "stroke-width=\"2\" marker-end=\"url(#BlackTriangle)\" />";

		SVGPart result = new SVGPart(0, 0, svg);
		result.append(val, len + 40, 0);
		logger.debug(result.toString());
		return result;
	}

	// TODO: let the grammar handle vertical bars instead! Only semi-possible?
	private String removeCharacter(String input, String c) {
		if (input.startsWith(c))
			input = input.substring(1);
		if (input.endsWith(c))
			input = input.substring(0, input.length() - 1);
		return input.trim();
	}

	private AffineTransform affinetransform = new AffineTransform();
	private FontRenderContext frc = new FontRenderContext(affinetransform,
			true, true);
	private Font font = new Font("SansSerif", Font.PLAIN, 12);

	private int getTextWidth(String input) {

		return (int) (font.getStringBounds(input, frc).getWidth());
	}
}
