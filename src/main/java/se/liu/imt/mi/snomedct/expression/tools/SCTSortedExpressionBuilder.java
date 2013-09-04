/**
 * 
 */
package se.liu.imt.mi.snomedct.expression.tools;

import java.util.Iterator;
import java.util.TreeSet;

import org.antlr.runtime.tree.Tree;

/**
 * Class for sorting an expression lexicographically. The rationale for sorting
 * expressions is to avoid storing of expressions which are structurally equal,
 * i.e. expression which are equal without regard to order among genera and
 * among differentiae.
 * 
 * @author Daniel Karlsson, daniel.karlsson@liu.se
 * @author Mikael Nyström, mikael.nystrom@liu.se *
 */
public class SCTSortedExpressionBuilder {
	/**
	 * Method for building a lexicographically sorted expression from a parse tree.
	 * 
	 * @param t		parse tree, output from SNOMED CT ANTLR parser
	 * @return		sorted String representation of expression
	 */
	public static String buildSortedExpression(Tree t) {

		StringBuilder result = new StringBuilder();

		if (t.getType() == se.liu.imt.mi.snomedct.expression.SCTExpressionParser.TOP_AND) {

			Tree genera = t.getChild(0);
			TreeSet<String> sortedGenera = new TreeSet<String>();
			for (int i = 0; i < genera.getChildCount(); i++)
				sortedGenera.add(genera.getChild(i).toString());
			for (Iterator<String> i = sortedGenera.iterator(); i.hasNext();) {
				result.append(i.next());
				if (i.hasNext())
					result.append('+');
			}

			Tree diff = t.getChild(1);
			TreeSet<String> sortedDiff = new TreeSet<String>();
			if (diff.getChildCount() > 0)
				result.append(':');

			for (int i = 0; i < diff.getChildCount(); i++)
				sortedDiff.add(buildSortedExpression(diff.getChild(i)));

			for (Iterator<String> i = sortedDiff.iterator(); i.hasNext();) {
				result.append(i.next());
				if (i.hasNext())
					result.append(',');
			}

		}

		if (t.getType() == se.liu.imt.mi.snomedct.expression.SCTExpressionParser.AND
				|| t.getType() == se.liu.imt.mi.snomedct.expression.SCTExpressionParser.ROLEGROUP) {

			TreeSet<String> sortedList = new TreeSet<String>();

			for (int i = 0; i < t.getChildCount(); i++)
				sortedList.add(buildSortedExpression(t.getChild(i)));

			if (t.getType() == se.liu.imt.mi.snomedct.expression.SCTExpressionParser.ROLEGROUP)
				result.append('{');

			for (Iterator<String> i = sortedList.iterator(); i.hasNext();) {
				result.append(i.next());
				if (i.hasNext())
					result.append(',');
			}

			if (t.getType() == se.liu.imt.mi.snomedct.expression.SCTExpressionParser.ROLEGROUP)
				result.append('}');
		}

		if (t.getType() == se.liu.imt.mi.snomedct.expression.SCTExpressionParser.SOME) {
			Tree attr = t.getChild(0);
			Tree value = t.getChild(1);
			if (value.getType() == se.liu.imt.mi.snomedct.expression.SCTExpressionParser.SCTID) {
				result.append(attr.toString()).append('=')
						.append(value.toString());
			} else {
				result.append(attr.toString()).append("=(")
						.append(buildSortedExpression(value)).append(')');
			}
		}

		return result.toString();
	}

}
