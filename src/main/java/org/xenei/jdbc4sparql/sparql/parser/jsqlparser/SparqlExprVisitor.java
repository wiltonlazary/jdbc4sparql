/*
 * This file is part of jdbc4sparql jsqlparser implementation.
 *
 * jdbc4sparql jsqlparser implementation is free software: you can redistribute
 * it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * jdbc4sparql jsqlparser implementation is distributed in the hope that it will
 * be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with jdbc4sparql jsqlparser implementation. If not, see
 * <http://www.gnu.org/licenses/>.
 */
package org.xenei.jdbc4sparql.sparql.parser.jsqlparser;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.sparql.expr.E_Add;
import com.hp.hpl.jena.sparql.expr.E_Divide;
import com.hp.hpl.jena.sparql.expr.E_Equals;
import com.hp.hpl.jena.sparql.expr.E_GreaterThan;
import com.hp.hpl.jena.sparql.expr.E_GreaterThanOrEqual;
import com.hp.hpl.jena.sparql.expr.E_LessThan;
import com.hp.hpl.jena.sparql.expr.E_LessThanOrEqual;
import com.hp.hpl.jena.sparql.expr.E_LogicalAnd;
import com.hp.hpl.jena.sparql.expr.E_LogicalOr;
import com.hp.hpl.jena.sparql.expr.E_Multiply;
import com.hp.hpl.jena.sparql.expr.E_NotEquals;
import com.hp.hpl.jena.sparql.expr.E_OneOf;
import com.hp.hpl.jena.sparql.expr.E_Regex;
import com.hp.hpl.jena.sparql.expr.E_Subtract;
import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.expr.ExprVar;
import com.hp.hpl.jena.sparql.expr.nodevalue.NodeValueDT;
import com.hp.hpl.jena.sparql.expr.nodevalue.NodeValueDouble;
import com.hp.hpl.jena.sparql.expr.nodevalue.NodeValueInteger;
import com.hp.hpl.jena.sparql.expr.nodevalue.NodeValueString;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

import net.sf.jsqlparser.expression.AllComparisonExpression;
import net.sf.jsqlparser.expression.AnyComparisonExpression;
import net.sf.jsqlparser.expression.BinaryExpression;
import net.sf.jsqlparser.expression.CaseExpression;
import net.sf.jsqlparser.expression.DateValue;
import net.sf.jsqlparser.expression.DoubleValue;
import net.sf.jsqlparser.expression.ExpressionVisitor;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.expression.InverseExpression;
import net.sf.jsqlparser.expression.JdbcParameter;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.NullValue;
import net.sf.jsqlparser.expression.Parenthesis;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.expression.TimeValue;
import net.sf.jsqlparser.expression.TimestampValue;
import net.sf.jsqlparser.expression.WhenClause;
import net.sf.jsqlparser.expression.operators.arithmetic.Addition;
import net.sf.jsqlparser.expression.operators.arithmetic.BitwiseAnd;
import net.sf.jsqlparser.expression.operators.arithmetic.BitwiseOr;
import net.sf.jsqlparser.expression.operators.arithmetic.BitwiseXor;
import net.sf.jsqlparser.expression.operators.arithmetic.Concat;
import net.sf.jsqlparser.expression.operators.arithmetic.Division;
import net.sf.jsqlparser.expression.operators.arithmetic.Multiplication;
import net.sf.jsqlparser.expression.operators.arithmetic.Subtraction;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.expression.operators.relational.Between;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.expression.operators.relational.ExistsExpression;
import net.sf.jsqlparser.expression.operators.relational.GreaterThan;
import net.sf.jsqlparser.expression.operators.relational.GreaterThanEquals;
import net.sf.jsqlparser.expression.operators.relational.InExpression;
import net.sf.jsqlparser.expression.operators.relational.IsNullExpression;
import net.sf.jsqlparser.expression.operators.relational.LikeExpression;
import net.sf.jsqlparser.expression.operators.relational.Matches;
import net.sf.jsqlparser.expression.operators.relational.MinorThan;
import net.sf.jsqlparser.expression.operators.relational.MinorThanEquals;
import net.sf.jsqlparser.expression.operators.relational.NotEqualsTo;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.SubSelect;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xenei.jdbc4sparql.iface.ColumnName;
import org.xenei.jdbc4sparql.sparql.SparqlQueryBuilder;

/**
 * An expression visitor. Merges SQL expressions into the SparqlQueryBuilder.
 */
public class SparqlExprVisitor implements ExpressionVisitor
{
	private class RegexNodeValue extends NodeValueString
	{
		private final boolean wildcard;

		public RegexNodeValue( final String str, final boolean wildcard )
		{
			super(str);
			this.wildcard = wildcard;
		}

		public boolean isWildcard()
		{
			return wildcard;
		}

	}

	// the query builder
	private final SparqlQueryBuilder builder;

	// A stack of expression elements.
	private final Stack<Expr> stack;

	private final boolean optionalColumns;

	private static Logger LOG = LoggerFactory
			.getLogger(SparqlExprVisitor.class);

	/**
	 * Constructor
	 *
	 * @param builder
	 *            The SparqlQueryBuilder to use.
	 */
	public SparqlExprVisitor( final SparqlQueryBuilder builder,
			final boolean optionalColumns )
	{
		this.builder = builder;
		this.optionalColumns = optionalColumns;
		stack = new Stack<Expr>();
	}

	/**
	 * Get the final result of the process.
	 *
	 * @return
	 */
	public Expr getResult()
	{
		return stack.pop();
	}

	/**
	 *
	 * @return True if the stack is empty (no result).
	 */
	public boolean isEmpty()
	{
		return stack.isEmpty();
	}

	private RegexNodeValue parseWildCard( final String part )
	{
		final Map<String, String> conversion = new HashMap<String, String>();
		conversion.put("_", ".");
		conversion.put("%", "(.+)");

		final StringTokenizer tokenizer = new StringTokenizer(part, "_%", true);
		final StringBuilder sb = new StringBuilder().append("^");
		final StringBuilder workingToken = new StringBuilder();
		boolean wildcard = false;
		while (tokenizer.hasMoreTokens())
		{
			final String candidate = tokenizer.nextToken();
			if ((candidate.length() == 1)
					&& conversion.keySet().contains(candidate))
			{
				// token
				if ((workingToken.length() > 0)
						&& (workingToken.charAt(workingToken.length() - 1) == '\\'))
				{
					// escaped token
					workingToken.setCharAt(workingToken.length() - 1,
							candidate.charAt(0));
				}
				else
				{
					sb.append(
							workingToken.length() > 0 ? Pattern
									.quote(workingToken.toString()) : "")
									.append(conversion.get(candidate));
					workingToken.setLength(0);
					wildcard = true;
				}
			}
			else
			{
				workingToken.append(candidate);
			}
		}
		// end of while
		if (workingToken.length() > 0)
		{
			sb.append(Pattern.quote(workingToken.toString()));
		}
		sb.append("$");
		final RegexNodeValue retval = new RegexNodeValue(
				wildcard ? sb.toString() : workingToken.toString(), wildcard);
		return retval;
	}

	// process a binary expression.
	private void process( final BinaryExpression biExpr )
	{
		// put on in reverse order so they can be popped back off in the proper
		// order.
		biExpr.getRightExpression().accept(this);
		biExpr.getLeftExpression().accept(this);
	}

	@Override
	public void visit( final Addition addition )
	{
		SparqlExprVisitor.LOG.debug("visit Addition: {}", addition);
		process(addition);
		stack.push(new E_Add(stack.pop(), stack.pop()));
	}

	@Override
	public void visit( final AllComparisonExpression allComparisonExpression )
	{
		throw new UnsupportedOperationException("ALL is not supported");
	}

	@Override
	public void visit( final AndExpression andExpression )
	{
		SparqlExprVisitor.LOG.debug("visit And: {}", andExpression);
		process(andExpression);
		stack.push(new E_LogicalAnd(stack.pop(), stack.pop()));
	}

	@Override
	public void visit( final AnyComparisonExpression anyComparisonExpression )
	{
		throw new UnsupportedOperationException("ANY is not supported");
	}

	@Override
	public void visit( final Between between )
	{
		SparqlExprVisitor.LOG.debug("visit Between: {}", between);
		between.getBetweenExpressionEnd().accept(this);
		between.getBetweenExpressionStart().accept(this);
		between.getLeftExpression().accept(this);
		// rewrite as x <= a >= y
		final Expr a = stack.pop();
		final Expr left = new E_LessThanOrEqual(stack.pop(), a);
		final Expr right = new E_GreaterThanOrEqual(a, stack.pop());
		stack.push(new E_LogicalAnd(left, right));
	}

	@Override
	public void visit( final BitwiseAnd bitwiseAnd )
	{
		throw new UnsupportedOperationException("'&' is not supported");
	}

	@Override
	public void visit( final BitwiseOr bitwiseOr )
	{
		throw new UnsupportedOperationException("'|' is not supported");
	}

	@Override
	public void visit( final BitwiseXor bitwiseXor )
	{
		throw new UnsupportedOperationException("'^' is not supported");
	}

	@Override
	public void visit( final CaseExpression caseExpression )
	{
		throw new UnsupportedOperationException("CASE is not supported");
	}

	@Override
	public void visit( final Column tableColumn )
	{
		SparqlExprVisitor.LOG.debug("visit Column: {}", tableColumn);
		try
		{
			final ColumnName cName = new ColumnName(tableColumn.getTable()
					.getSchemaName(), tableColumn.getTable().getName(),
					tableColumn.getColumnName());

			final Node columnVar = builder.addColumn(cName, optionalColumns);
			stack.push(new ExprVar(columnVar));
		}
		catch (final SQLException e)
		{
			throw new RuntimeException(e);
		}
	}

	@Override
	public void visit( final Concat concat )
	{
		throw new UnsupportedOperationException("CONCAT is not supported");
	}

	@Override
	public void visit( final DateValue dateValue )
	{
		SparqlExprVisitor.LOG.debug("visit DateValue: {}", dateValue);
		final String val = dateValue.getValue().toString();
		final Node n = NodeFactory.createLiteral(val, XSDDatatype.XSDdate);
		stack.push(new NodeValueDT(val, n));
	}

	@Override
	public void visit( final Division division )
	{
		SparqlExprVisitor.LOG.debug("visit Division: {}", division);
		process(division);
		stack.push(new E_Divide(stack.pop(), stack.pop()));
	}

	@Override
	public void visit( final DoubleValue doubleValue )
	{
		SparqlExprVisitor.LOG.debug("visit DoubleValue: {}", doubleValue);
		stack.push(new NodeValueDouble(doubleValue.getValue()));
	}

	@Override
	public void visit( final EqualsTo equalsTo )
	{
		SparqlExprVisitor.LOG.debug("visit EqualsTo: {}", equalsTo);
		process(equalsTo);
		stack.push(new E_Equals(stack.pop(), stack.pop()));
	}

	@Override
	public void visit( final ExistsExpression existsExpression )
	{
		throw new UnsupportedOperationException("EXISTS is not supported");
	}

	@Override
	public void visit( final Function function )
	{
		SparqlExprVisitor.LOG.debug("visit Function: {}", function);
		final StandardFunctionHandler sfh = new StandardFunctionHandler(
				builder, stack);
		try
		{
			sfh.handle(function);
		}
		catch (final SQLException e)
		{
			throw new UnsupportedOperationException(String.format(
					"function %s is not supported", function.getName()));
		}
	}

	@Override
	public void visit( final GreaterThan greaterThan )
	{
		SparqlExprVisitor.LOG.debug("visit GreaterThan: {}", greaterThan);
		process(greaterThan);
		stack.push(new E_GreaterThan(stack.pop(), stack.pop()));
	}

	@Override
	public void visit( final GreaterThanEquals greaterThanEquals )
	{
		SparqlExprVisitor.LOG.debug("visit GreaterThanEquals: {}",
				greaterThanEquals);
		process(greaterThanEquals);
		stack.push(new E_GreaterThanOrEqual(stack.pop(), stack.pop()));
	}

	@Override
	public void visit( final InExpression inExpression )
	{
		SparqlExprVisitor.LOG.debug("visit InExpression: {}", inExpression);

		final SparqlItemsListVisitor listVisitor = new SparqlItemsListVisitor(
				builder);
		inExpression.getItemsList().accept(listVisitor);
		inExpression.getLeftExpression().accept(this);
		stack.push(new E_OneOf(stack.pop(), listVisitor.getResult()));
	}

	@Override
	public void visit( final InverseExpression inverseExpression )
	{
		throw new UnsupportedOperationException(
				"inverse expressions are not supported");
	}

	@Override
	public void visit( final IsNullExpression isNullExpression )
	{
		SparqlExprVisitor.LOG.debug("visit isNull: {}", isNullExpression);

		isNullExpression.getLeftExpression().accept(this);
		stack.push(new E_Equals(stack.pop(), null));
	}

	@Override
	public void visit( final JdbcParameter jdbcParameter )
	{
		throw new UnsupportedOperationException(
				"JDBC Parameters are not supported");
	}

	@Override
	public void visit( final LikeExpression likeExpression )
	{
		SparqlExprVisitor.LOG.debug("visit LikeExpression: {}", likeExpression);
		process(likeExpression);
		final Expr left = stack.pop();
		final Expr right = stack.pop();
		if (right instanceof NodeValueString)
		{
			final RegexNodeValue rnv = parseWildCard(((NodeValueString) right)
					.getString());
			if (rnv.isWildcard())
			{
				stack.push(new E_Regex(left, rnv, new NodeValueString("")));
			}
			else
			{
				stack.push(new E_Equals(left, rnv));
			}
		}
		else
		{
			throw new UnsupportedOperationException(
					"LIKE must have string for right hand argument");
		}
	}

	@Override
	public void visit( final LongValue longValue )
	{
		SparqlExprVisitor.LOG.debug("visit Long: {}", longValue);
		stack.push(new NodeValueInteger(longValue.getValue()));
	}

	@Override
	public void visit( final Matches matches )
	{
		throw new UnsupportedOperationException("MATCHES is not supported");
	}

	@Override
	public void visit( final MinorThan minorThan )
	{
		SparqlExprVisitor.LOG.debug("visit MinorThan: {}", minorThan);
		process(minorThan);
		stack.push(new E_LessThan(stack.pop(), stack.pop()));
	}

	@Override
	public void visit( final MinorThanEquals minorThanEquals )
	{
		SparqlExprVisitor.LOG.debug("visit MinorThanEquals: {}",
				minorThanEquals);
		process(minorThanEquals);
		stack.push(new E_LessThanOrEqual(stack.pop(), stack.pop()));
	}

	@Override
	public void visit( final Multiplication multiplication )
	{
		SparqlExprVisitor.LOG.debug("visit Multiplication: {}", multiplication);
		process(multiplication);
		stack.push(new E_Multiply(stack.pop(), stack.pop()));
	}

	@Override
	public void visit( final NotEqualsTo notEqualsTo )
	{
		SparqlExprVisitor.LOG.debug("visit not wquals: {}", notEqualsTo);
		process(notEqualsTo);
		stack.push(new E_NotEquals(stack.pop(), stack.pop()));
	}

	@Override
	public void visit( final NullValue nullValue )
	{
		SparqlExprVisitor.LOG.debug("visit null value: {}", nullValue);
		throw new UnsupportedOperationException(
				"Figure out how to process NULL");
	}

	@Override
	public void visit( final OrExpression orExpression )
	{
		SparqlExprVisitor.LOG.debug("visit orExpression: {}", orExpression);
		process(orExpression);
		stack.push(new E_LogicalOr(stack.pop(), stack.pop()));
	}

	@Override
	public void visit( final Parenthesis parenthesis )
	{
		SparqlExprVisitor.LOG.debug("visit Parenthesis: {}", parenthesis);
		parenthesis.getExpression().accept(this);
	}

	@Override
	public void visit( final StringValue stringValue )
	{
		SparqlExprVisitor.LOG.debug("visit String Value: {}", stringValue);
		stack.push(new NodeValueString(stringValue.getValue()));
	}

	@Override
	public void visit( final SubSelect subSelect )
	{
		throw new UnsupportedOperationException("SUB SELECT is not supported");
	}

	@Override
	public void visit( final Subtraction subtraction )
	{
		SparqlExprVisitor.LOG.debug("visit Subtraction: {}", subtraction);
		process(subtraction);
		stack.push(new E_Subtract(stack.pop(), stack.pop()));
	}

	@Override
	public void visit( final TimestampValue timestampValue )
	{
		SparqlExprVisitor.LOG.debug("visit Timestamp: {}", timestampValue);
		final String val = timestampValue.getValue().toString();
		final Node n = NodeFactory.createLiteral(val, XSDDatatype.XSDdateTime);
		stack.push(new NodeValueDT(val, n));
	}

	@Override
	public void visit( final TimeValue timeValue )
	{
		SparqlExprVisitor.LOG.debug("visit TimeValue: {}", timeValue);
		final String val = timeValue.getValue().toString();
		final Node n = NodeFactory.createLiteral(val, XSDDatatype.XSDtime);
		stack.push(new NodeValueDT(val, n));
	}

	@Override
	public void visit( final WhenClause whenClause )
	{
		throw new UnsupportedOperationException("WHEN is not supported");
	}
}