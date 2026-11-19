// Generated from Policy.g4 by ANTLR 4.13.2

package mrs.policy;

import org.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link PolicyParser}.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 * operations with no return type.
 */
public interface PolicyVisitor<T> extends ParseTreeVisitor<T> {
	/**
	 * Visit a parse tree produced by {@link PolicyParser#policy}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPolicy(PolicyParser.PolicyContext ctx);
	/**
	 * Visit a parse tree produced by the {@code AndExpr}
	 * labeled alternative in {@link PolicyParser#expr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAndExpr(PolicyParser.AndExprContext ctx);
	/**
	 * Visit a parse tree produced by the {@code ParenExpr}
	 * labeled alternative in {@link PolicyParser#expr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitParenExpr(PolicyParser.ParenExprContext ctx);
	/**
	 * Visit a parse tree produced by the {@code OrExpr}
	 * labeled alternative in {@link PolicyParser#expr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOrExpr(PolicyParser.OrExprContext ctx);
	/**
	 * Visit a parse tree produced by the {@code AttrExpr}
	 * labeled alternative in {@link PolicyParser#expr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAttrExpr(PolicyParser.AttrExprContext ctx);
}