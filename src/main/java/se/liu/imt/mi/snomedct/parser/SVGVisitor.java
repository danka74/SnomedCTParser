/**
 * 
 */
package se.liu.imt.mi.snomedct.parser;

import se.liu.imt.mi.snomedct.expression.SNOMEDCTExpressionBaseVisitor;
import se.liu.imt.mi.snomedct.expression.SNOMEDCTExpressionLexer;
import se.liu.imt.mi.snomedct.expression.SNOMEDCTExpressionParser.ConceptReferenceContext;
import se.liu.imt.mi.snomedct.expression.SNOMEDCTExpressionParser.ExpressionContext;
import se.liu.imt.mi.snomedct.expression.SNOMEDCTExpressionParser.StatementContext;

/**
 * @author Daniel Karlsson, daniel.karlsson@liu.se
 * 
 *         Visitor class for generating SVG output from SNOMED CT expressions.
 *         Opted to use string representation of SVG in order to reuse SVG from
 *         the IHTSDO Browser.
 *
 */
public class SVGVisitor extends SNOMEDCTExpressionBaseVisitor<SVGPart> {

	@Override
	protected SVGPart aggregateResult(SVGPart aggregate, SVGPart nextResult) {
		if(aggregate != null)
			return aggregate.append(nextResult);
		else return nextResult;
	}

	private String svgDefs = "<defs id=\"SctDiagramsDefs\">\n"
			+ "		<marker id=\"BlackTriangle\" refX=\"0\" refY=\"10\" markerWidth=\"8\"\n"
			+ "			markerHeight=\"6\" orient=\"auto\" viewBox=\"0 0 22 20\" markerUnits=\"strokeWidth\"\n"
			+ "			fill=\"black\" stroke=\"black\" stroke-width=\"2\">\n"
			+ "			<path d=\"M 0 0 L 20 10 L 0 20 z\"></path>\n"
			+ "		</marker>\n"
			+ "		<marker id=\"ClearTriangle\" refX=\"0\" refY=\"10\" markerWidth=\"8\"\n"
			+ "			markerHeight=\"8\" orient=\"auto\" viewBox=\"0 0 22 20\" markerUnits=\"strokeWidth\"\n"
			+ "			fill=\"white\" stroke=\"black\" stroke-width=\"2\">\n"
			+ "			<path d=\"M 0 0 L 20 10 L 0 20 z\"></path>\n"
			+ "		</marker>\n"
			+ "		<marker id=\"LineMarker\" refX=\"0\" refY=\"10\" markerWidth=\"8\"\n"
			+ "			markerHeight=\"8\" orient=\"auto\" viewBox=\"0 0 22 20\" markerUnits=\"strokeWidth\"\n"
			+ "			fill=\"white\" stroke=\"black\" stroke-width=\"2\">\n"
			+ "			<path d=\"M 0 10 L 20 10\"></path>\n" + "		</marker>\n"
			+ "	</defs>";

	private String svgEquivalentTo = "<g>\n"
			+ "		<circle cx=\"100\" cy=\"93\" r=\"20\" fill=\"white\" stroke=\"black\"\n"
			+ "			stroke-width=\"2\"></circle>\n"
			+ "		<line x1=\"93\" y1=\"88\" x2=\"107\" y2=\"88\" stroke=\"black\"\n"
			+ "			stroke-width=\"2\"></line>\n"
			+ "		<line x1=\"93\" y1=\"93\" x2=\"107\" y2=\"93\" stroke=\"black\"\n"
			+ "			stroke-width=\"2\"></line>\n"
			+ "		<line x1=\"93\" y1=\"98\" x2=\"107\" y2=\"98\" stroke=\"black\"\n"
			+ "			stroke-width=\"2\"></line>\n" + "	</g>";
	private String svgSubclassOf = "<g>\n"
			+ "		<circle cx=\"100\" cy=\"89\" r=\"20\" fill=\"white\" stroke=\"black\"\n"
			+ "			stroke-width=\"2\"></circle>\n"
			+ "		<line x1=\"93\" y1=\"81\" x2=\"107\" y2=\"81\" stroke=\"black\"\n"
			+ "			stroke-width=\"2\"></line>\n"
			+ "		<line x1=\"93\" y1=\"92\" x2=\"107\" y2=\"92\" stroke=\"black\"\n"
			+ "			stroke-width=\"2\"></line>\n"
			+ "		<line x1=\"94\" y1=\"81\" x2=\"94\" y2=\"92\" stroke=\"black\"\n"
			+ "			stroke-width=\"2\"></line>\n"
			+ "		<line x1=\"93\" y1=\"96\" x2=\"107\" y2=\"96\" stroke=\"black\"\n"
			+ "			stroke-width=\"2\"></line>\n" + "	</g>";

	@Override
	public SVGPart visitStatement(StatementContext ctx) {
		SVGPart result = new SVGPart(0, 0, svgDefs);

		SVGPart x = visitSubExpression(ctx.subExpression(0));
		result.part.append(x);

		if (ctx.definitionStatus().start.getType() == SNOMEDCTExpressionLexer.EQ_TO)
			result.append(new SVGPart(0, 0, svgEquivalentTo));
		else
			result.append(new SVGPart(0, 0, svgSubclassOf));

		result.append(visitSubExpression(ctx.subExpression(1)));

		return result;
	}

	@Override
	public SVGPart visitConceptReference(ConceptReferenceContext ctx) {
		String svg = "<rect x=\"0\" y=\"0\" width=\"158\" height=\"40\" rx=\"18\" ry=\"18\" id=\"rect56\"\n"
				+ "style=\"fill:#ffffcc;stroke:#333333;stroke-width:1\" />\n"
				+ "<rect x=\"2\" y=\"2\" width=\"154\" height=\"36\" rx=\"18\" ry=\"18\" id=\"rect17\"\n"
				+ "style=\"fill:#ffffcc;stroke:#333333;stroke-width:1\" />\n"
				+ "<text x=\"12\" y=\"17\"\n"
				+ "font-family=\"&quot;Helvetica Neue&quot;,Helvetica,Arial,sans-serif\"\n"
				+ "font-size=\"10\">"
				+ ctx.SCTID().getText()
				+ "</text>\n"
				+ "<text x=\"12\" y=\"31\" id=\"text61\"\n"
				+ "font-family=\"&quot;Helvetica Neue&quot;,Helvetica,Arial,sans-serif\"\n"
				+ "font-size=\"12\">" + ctx.TERM().getText() + "</text>";
		SVGPart result = new SVGPart(0, 50, svg);
		return result;
	}
}
