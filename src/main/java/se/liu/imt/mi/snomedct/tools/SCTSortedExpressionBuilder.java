/**
 * 
 */
package se.liu.imt.mi.snomedct.tools;

import java.util.Iterator;
import java.util.TreeSet;

import org.antlr.runtime.tree.Tree;

/**
 * @author Daniel Karlsson, daniel.karlsson@liu.se
 * @author Mikael Nyström, mikael.nystrom@liu.se *
 */
public class SCTSortedExpressionBuilder {
	public static String buildSortedExpression(Tree t) {

		String result = "";

		if (t.getType() == se.liu.imt.mi.snomedct.expression.SCTExpressionParser.TOP_AND) {

			Tree genera = t.getChild(0);
			TreeSet<String> sortedGenera = new TreeSet<String>();
			for (int i = 0; i < genera.getChildCount(); i++) 
				sortedGenera.add(genera.getChild(i).toString());
			for(Iterator<String> i = sortedGenera.iterator(); i.hasNext(); ) {
				result += i.next();
				if(i.hasNext())
					result += "+";
			}
			
			Tree diff = t.getChild(1);
			TreeSet<String> sortedDiff = new TreeSet<String>();
			if(diff.getChildCount() > 0)
				result += ":";
			
			for (int i = 0; i < diff.getChildCount(); i++) 
				sortedDiff.add(buildSortedExpression(diff.getChild(i)));
			
			for(Iterator<String> i = sortedDiff.iterator(); i.hasNext(); ) {
				result += i.next();
				if(i.hasNext())
					result += ",";
			}

		}
		
		if (t.getType() == se.liu.imt.mi.snomedct.expression.SCTExpressionParser.AND ||
				t.getType() == se.liu.imt.mi.snomedct.expression.SCTExpressionParser.ROLEGROUP) {
						
			TreeSet<String> sortedList = new TreeSet<String>();
			
			for (int i = 0; i < t.getChildCount(); i++) 
				sortedList.add(buildSortedExpression(t.getChild(i)));
			
			if(t.getType()  == se.liu.imt.mi.snomedct.expression.SCTExpressionParser.ROLEGROUP)
				result += "{";
			
			for(Iterator<String> i = sortedList.iterator(); i.hasNext(); ) {
				result += i.next();
				if(i.hasNext())
					result += ",";
			}
			
			if(t.getType()  == se.liu.imt.mi.snomedct.expression.SCTExpressionParser.ROLEGROUP)
				result += "}";
		}
		
		if (t.getType() == se.liu.imt.mi.snomedct.expression.SCTExpressionParser.SOME) {
			Tree attr = t.getChild(0);
			Tree value = t.getChild(1);
			if(value.getType() == se.liu.imt.mi.snomedct.expression.SCTExpressionParser.SCTID) {
				result += attr.toString() + "=" + value.toString();
			}
			else {
				result += attr.toString() + "=(" + buildSortedExpression(value) + ")";
			}
		}
		
		return result;
	}

}
