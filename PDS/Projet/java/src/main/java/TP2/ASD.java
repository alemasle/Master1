package TP2;

import java.util.*;

import TP2.SymbolTable.FunctionSymbol;
import TP2.SymbolTable.VariableSymbol;

public class ASD {

	static Boolean retour = false;
	static int depth = 0;

	static Stack<String> pile = new Stack<String>();

	static public class Program {

		List<ProtoFonction> lproto;
		List<Fonction> lfunc;

		public Program(List<ProtoFonction> lproto, List<Fonction> lfunc) {
			this.lproto = lproto;
			this.lfunc = lfunc;
		}

		// Pretty-printer
		public String pp() {
			return "PROGRAM";
		}

		// IR generation
		public Llvm.IR toIR() throws TypeException {

			Llvm.IR irProg = new Llvm.IR(Llvm.empty(), Llvm.empty());

			SymbolTable ts = new SymbolTable();

			Fonction main = null;

			for (ProtoFonction p : lproto) {
				irProg.append(p.toIR(ts).ir);
			}

			for (Fonction f : lfunc) {
				if (f.name == "main") {
					main = f;
				} else {
					Fonction.RetFonction retFonc = f.toIR(ts);
					irProg.append(retFonc.ir);
				}
			}

			if (main == null) {
				throw new TypeException("Fonction main() absente du programme.");
			}

			Fonction.RetFonction mRet = main.toIR(ts);
			irProg.append(mRet.ir);

			return irProg;
		}

	}

	static public class ProtoFonction {

		String type;
		String name;
		List<Param> params;

		public ProtoFonction(String type, String name, List<Param> params) {
			this.type = type;
			this.name = name;
			this.params = params;
		}

		public String pp() {
			String str = "PROTO " + type + " " + name + "( ";
			for (Param p : params) {
				str += p.pp() + " ";
			}
			return str + ") ";
		}

		static public class RetProtoFonction {
			// The LLVM IR:
			public Llvm.IR ir;

			public RetProtoFonction(Llvm.IR ir) {
				this.ir = ir;
			}
		}

		public RetProtoFonction toIR(SymbolTable ts) throws TypeException {

			Llvm.IR fonctionIR = new Llvm.IR(Llvm.empty(), Llvm.empty());
			Type t = null;

			if (type == "INT") {
				t = new IntType();
			}

			if (type == "VOID'") {
				t = new VoidType();
			}
			if (ts.lookup(name) != null && ts.lookup(name) instanceof FunctionSymbol) {
				throw new TypeException("Fonction " + name + " est deja declaree");
			} else {
				SymbolTable args = new SymbolTable();
				SymbolTable.VariableSymbol para = null;

				for (Param p : params) {
					Param.RetParam retP = p.toIR(ts);
					para = retP.varSym;

					if (Character.isLetter(para.ident.charAt(0))) {
						args.add(para);
					}
				}
				SymbolTable.FunctionSymbol protoF = new SymbolTable.FunctionSymbol(t, name, args, false);
				ts.add(protoF);
			}

			return new RetProtoFonction(fonctionIR);
		}

	}

	static public class Fonction {

		String type;
		String name;
		List<Param> params;
		FonctionCorps c;

		public Fonction(String type, String name, List<Param> params, FonctionCorps c) {
			this.type = type;
			this.name = name;
			this.params = params;
			this.c = c;
		}

		public String pp() {
			String str = "FUNC " + type + " " + name + "( ";
			for (Param p : params) {
				str += p.pp() + " ";
			}
			return str + ") " + c.pp();
		}

		static public class RetFonction {
			// The LLVM IR:
			public Llvm.IR ir;

			public RetFonction(Llvm.IR ir) {
				this.ir = ir;
			}
		}

		public RetFonction toIR(SymbolTable ts) throws TypeException {

			Llvm.IR fonctionIR = new Llvm.IR(Llvm.empty(), Llvm.empty());
			Type t = null;

			if (type == "INT") {
				t = new IntType();
			}

			if (type == "VOID'") {
				t = new VoidType();
			}

			if (name == "main") {

				if (ts.lookup(name) != null && ts.lookup(name) instanceof FunctionSymbol) {

					FunctionSymbol x = (FunctionSymbol) ts.lookup(name);
					if (x.defined) {
						throw new TypeException("La fonction " + name + " est deja definie");
					}
					x.defined = true;

				} else {

					SymbolTable args = new SymbolTable();
					SymbolTable.VariableSymbol sym = null;

					for (Param p : params) {
						Param.RetParam retP = p.toIR(ts);
						sym = retP.varSym;

						if (Character.isLetter(sym.ident.charAt(0))) {
							args.add(sym);
						}
					}
					SymbolTable.FunctionSymbol main = new SymbolTable.FunctionSymbol(t, name, args, true);
					ts.add(main);
					System.out.println("EHEHIEHEIHIEHEIHZHZBHBHZE " + ts.lookup(main.ident).ident);
				}
			} else if (ts.lookup(name) == null) {

				throw new TypeException("Fonction " + name + " n'est pas declaree par un PROTO");

			} else if (ts.lookup(name) instanceof FunctionSymbol) {

				FunctionSymbol x = (FunctionSymbol) ts.lookup(name);

				if (x.defined) {
					throw new TypeException("La fonction " + name + " est deja definie");
				}

				if (x.arguments.getTable().size() != params.size()) {
					throw new TypeException("La fonction " + name + " a" + x.arguments.getTable().size()
							+ " arguments, vous en avez donne " + params.size());
				}

				x.defined = true;

			}

			List<String> pS = new ArrayList<String>();

			for (Param p : params) {
				Param.RetParam tmp = p.toIR(ts);
				pS.add(tmp.result);
			}

			FonctionCorps.RetFonctionCorps retCorps = c.toIR(ts);

			Llvm.Instruction fct = new Llvm.Fonction(t.toLlvmType(), name, pS);

			Llvm.Instruction fermAcc = new Llvm.FonctionEnd();

			fonctionIR.appendCode(fct);

			fonctionIR.append(retCorps.ir);

			fonctionIR.appendCode(fermAcc);

			return new RetFonction(fonctionIR);
		}

	}

	static public class Param {

		Expression e = null;

		public Param(Expression e) {
			this.e = e;
		}

		public String pp() {
			return e.pp();
		}

		static public class RetParam {
			// The LLVM IR:
			public Llvm.IR ir;
			public String result;
			public SymbolTable.VariableSymbol varSym;

			public RetParam(Llvm.IR ir, String result, VariableSymbol varSym) {
				this.ir = ir;
				this.result = result;
				this.varSym = varSym;
			}
		}

		public RetParam toIR(SymbolTable ts) throws TypeException {

			Llvm.IR paramIR = new Llvm.IR(Llvm.empty(), Llvm.empty());

			Expression.RetExpression retExp = e.toIR(ts);

			SymbolTable.VariableSymbol varS = new VariableSymbol(retExp.type, retExp.result);

			Llvm.Instruction par = new Llvm.constPar(retExp.type.toLlvmType(), retExp.result);

			paramIR.appendCode(par);

			return new RetParam(paramIR, par.toString(), varS);

		}
	}

	static public class FonctionCorps {

		Bloc b = null;

		public FonctionCorps(Bloc b) {
			this.b = b;
		}

		public String pp() {
			return b.pp();
		}

		static public class RetFonctionCorps {
			// The LLVM IR:
			public Llvm.IR ir;

			public RetFonctionCorps(Llvm.IR ir) {
				this.ir = ir;
			}

		}

		public RetFonctionCorps toIR(SymbolTable ts) throws TypeException {

			Llvm.IR funcIR = new Llvm.IR(Llvm.empty(), Llvm.empty());

			Bloc.RetBloc retB = b.toIR(ts);

			funcIR.append(retB.ir);

			return new RetFonctionCorps(funcIR);
		}

	}

	static public class Bloc {

		Declaration dec = null;
		List<Statement> ls = new ArrayList<>();

		public Bloc(Declaration dec, List<Statement> ls) {
			this.ls = ls;
			this.dec = dec;
		}

		// Pretty-printer
		public String pp() {
			String str = dec.pp() + "\n";
			for (Statement s : ls) {
				str += s.pp() + "\n";
			}
			return str;
		}

		static public class RetBloc {
			// The LLVM IR:
			public Llvm.IR ir;

			public RetBloc(Llvm.IR ir) {
				this.ir = ir;
			}
		}

		public RetBloc toIR(SymbolTable ts) throws TypeException {
			Llvm.IR blocIR = new Llvm.IR(Llvm.empty(), Llvm.empty());

			SymbolTable tsF = new SymbolTable(ts);

			if (dec != null) {
				Declaration.RetDeclaration retDecl = dec.toIR(tsF);
				blocIR.append(retDecl.ir);
			}

			for (Statement s : ls) {
				Statement.RetStatement retStat = s.toIR(tsF);
				blocIR.append(retStat.ir);
			}

			return new RetBloc(blocIR);
		}
	}

	static public class Declaration {
		List<DeclVar> lVar = new ArrayList<DeclVar>();

		static public class RetDeclaration {
			// The LLVM IR:
			public Llvm.IR ir = null;
			Type type = null;

			public RetDeclaration(Llvm.IR ir) {
				this.ir = ir;
			}
		}

		public Declaration(List<DeclVar> lVar) {
			this.lVar = lVar;
		}

		public String pp() {
			String str = "";
			for (DeclVar v : lVar) {
				str += v.pp() + "\n";
			}
			return str;
		}

		public RetDeclaration toIR(SymbolTable ts) throws TypeException {
			Llvm.IR declIR = new Llvm.IR(Llvm.empty(), Llvm.empty());

			for (DeclVar declVar : lVar) {

				DeclVar.RetIdentificateur retDeclVar = declVar.toIR(ts);

				declIR.append(retDeclVar.ir);

			}

			return new RetDeclaration(declIR);
		}

	}

	static public abstract class Stat {
		public abstract String pp();

		public abstract RetStatement toIR(SymbolTable ts) throws TypeException;

		static public class RetStatement {
			// The LLVM IR:
			public Llvm.IR ir = null;
			Type type = null;
			String result = null;

			public RetStatement(Llvm.IR ir, Type type, String result) {
				this.ir = ir;
				this.type = type;
				this.result = result;
			}
		}
	}

	static public class Statement extends Stat {

		Instructions i = null;
		Expression e = null;

		public Statement(Expression e) {
			this.e = e;

		}

		public Statement(Instructions i) {
			this.i = i;
		}

		public String pp() {
			if (i == null && e != null)
				return e.pp();
			else if (i != null && e == null)
				return i.pp();
			else
				return "";
		}

		public RetStatement toIR(SymbolTable ts) throws TypeException {

			if (i != null) {

				Instructions.RetInstructions instrRet = i.toIR(ts);
				return new RetStatement(instrRet.ir, null, null);

			} else if (e != null) {
				Expression.RetExpression exprRet = e.toIR(ts);
				return new RetStatement(exprRet.ir, exprRet.type, exprRet.result);
			}
			return null;
		}
	}

	static public abstract class Instructions {
		public abstract String pp();

		public abstract RetInstructions toIR(SymbolTable ts) throws TypeException;

		static public class RetInstructions {
			// The LLVM IR:
			public Llvm.IR ir;

			public RetInstructions(Llvm.IR ir) {
				this.ir = ir;
			}
		}
	}

	static public class WhileInstruction extends Instructions {
		Expression condExpr;
		Bloc doBloc;

		public WhileInstruction(Expression condExpr, Bloc doBloc) {
			this.condExpr = condExpr;
			this.doBloc = doBloc;

		}

		// Pretty-printer
		public String pp() {
			return "(" + "While " + condExpr.pp() + "Do " + doBloc.pp() + "Done )";
		}

		public RetInstructions toIR(SymbolTable ts) throws TypeException {
			Llvm.IR whileRet = new Llvm.IR(Llvm.empty(), Llvm.empty());
			Expression.RetExpression condRet = condExpr.toIR(ts);
			Bloc.RetBloc doRet = doBloc.toIR(ts);

			String labelWhile = Utils.newlab("while");
			String labelDo = Utils.newlab("do");
			String labelDone = Utils.newlab("done");

			ASD.pile.push(labelDone);
			ASD.pile.push(labelWhile);
			ASD.pile.push(labelDo);

			// br while
			Llvm.Instruction labelRetloop = new Llvm.AppelLabel(labelWhile);
			whileRet.appendCode(labelRetloop);

			// while :
			Llvm.Instruction labelNomloop = new Llvm.LabelName(labelWhile);
			whileRet.appendCode(labelNomloop);

			whileRet.append(condRet.ir);

			// le code de while
			Llvm.Instruction whileinstruction = new Llvm.WhileInstr(new BoolType().toLlvmType(), condRet.result,
					labelWhile, labelDo, labelDone);
			whileRet.appendCode(whileinstruction);

			// do
			Llvm.Instruction labelDoNom = new Llvm.LabelName(pile.pop());
			whileRet.appendCode(labelDoNom);

			// code du do
			whileRet.append(doRet.ir);

			// br while
			Llvm.Instruction labelDoRet = new Llvm.AppelLabel(pile.pop());
			whileRet.appendCode(labelDoRet);

			// done
			Llvm.Instruction labelDoneNom = new Llvm.LabelName(pile.pop());
			whileRet.appendCode(labelDoneNom);

			// return the generated IR, plus the type of this expression
			return new RetInstructions(whileRet);
		}
	}

	static public class IfInstruction extends Instructions {
		Expression condExpr;
		Bloc thenBloc;
		Bloc elseBloc;

		public IfInstruction(Expression condExpr, Bloc thenBloc, Bloc elseBloc) {
			this.condExpr = condExpr;
			this.thenBloc = thenBloc;
			this.elseBloc = elseBloc;
		}

		// Pretty-printer
		public String pp() {
			String str = "";
			str = "(" + "if " + condExpr.pp() + "then " + thenBloc.pp();
			if (elseBloc != null) {
				str += "else " + elseBloc.pp();
			}
			str += " )";
			return str;
		}

		public RetInstructions toIR(SymbolTable ts) throws TypeException {
			Expression.RetExpression condRet = condExpr.toIR(ts);
			Bloc.RetBloc thenRet = thenBloc.toIR(ts);

			String labelThen = Utils.newlab("then");
			String labelElse = null;
			String labelFi = Utils.newlab("fi");

			ASD.pile.push(labelFi);
			ASD.pile.push(labelFi);

			if (elseBloc != null) {

				labelElse = Utils.newlab("else");
				ASD.pile.push(labelElse);
				ASD.pile.push(labelFi);

			} else {
				labelElse = labelFi;
			}
			ASD.pile.push(labelThen);

			Llvm.Instruction ifinstruction = new Llvm.IfInstr(new BoolType().toLlvmType(), condRet.result, labelThen,
					labelElse);
			// if
			condRet.ir.appendCode(ifinstruction);

			// then
			Llvm.Instruction labelThenNom = new Llvm.LabelName(pile.pop());
			condRet.ir.appendCode(labelThenNom);

			condRet.ir.append(thenRet.ir);

			Llvm.Instruction labelThenRet = new Llvm.AppelLabel(pile.pop());// fi
			condRet.ir.appendCode(labelThenRet);

			// else
			if (elseBloc != null) {
				Bloc.RetBloc elseRet = elseBloc.toIR(ts);
				Llvm.Instruction labelElseNom = new Llvm.LabelName(pile.pop());
				condRet.ir.appendCode(labelElseNom);

				condRet.ir.append(elseRet.ir);

				Llvm.Instruction labelElseRet = new Llvm.AppelLabel(pile.pop());// fi
				condRet.ir.appendCode(labelElseRet);

			}
			// fi
			Llvm.Instruction labelFiNom = new Llvm.LabelName(pile.pop());
			condRet.ir.appendCode(labelFiNom);

			// return the generated IR, plus the type of this expression
			return new RetInstructions(condRet.ir);
		}
	}

	static public class AffectInstructions extends Instructions {
		Identificateur ident;
		Expression expr;

		public AffectInstructions(Identificateur ident, Expression expr) {
			this.ident = ident;
			this.expr = expr;
		}

		// Pretty-printer
		public String pp() {
			return "(" + ident.pp() + " := " + expr.pp() + ")";
		}

		public RetInstructions toIR(SymbolTable ts) throws TypeException {
			Identificateur.RetIdentificateur identRet = ident.toIR(ts);
			Expression.RetExpression exprRet = expr.toIR(ts);

			// We check if the types mismatches
			if (!identRet.type.equals(exprRet.type)) {
				throw new TypeException("type mismatch: have " + identRet.type + " and " + exprRet.type);
			}

			// We base our build on the left generated IR:
			// append right code
			identRet.ir.append(exprRet.ir);

			// new affect instruction result = affectable := expression
			Llvm.Instruction affect = new Llvm.Affect(identRet.type.toLlvmType(), identRet.result, exprRet.result);

			// append this instruction
			identRet.ir.appendCode(affect);

			// return the generated IR, plus the type of this expression
			return new RetInstructions(identRet.ir);
		}
	}

	static public class ReturnInstructions extends Instructions {
		Expression expr;

		public ReturnInstructions(Expression expr) {
			this.expr = expr;
		}

		// Pretty-printer
		public String pp() {
			return "(" + " return " + expr.pp() + " )";
		}

		public RetInstructions toIR(SymbolTable ts) throws TypeException {

			Expression.RetExpression exprRet = expr.toIR(ts);

			Llvm.Instruction retour = new Llvm.Return(exprRet.type.toLlvmType(), exprRet.result);

			if (ASD.depth == 0) {
				ASD.retour = true;
			}

			// append this instruction
			exprRet.ir.appendCode(retour);

			// return the generated IR, plus the type of this expression
			return new RetInstructions(exprRet.ir);
		}
	}

	static public abstract class Identificateur {
		public abstract String pp();

		public abstract RetIdentificateur toIR(SymbolTable ts) throws TypeException;

		static public class RetIdentificateur {
			// The LLVM IR:
			public Llvm.IR ir;
			// And additional stuff:
			public Type type;
			public String result; // The name containing the expression's result
			// (either an identifier, or an immediate value)

			public RetIdentificateur(Llvm.IR ir, Type type, String result) {
				this.ir = ir;
				this.type = type;
				this.result = result;
			}
		}
	}

	static public class DeclVar extends Identificateur {
		Type type;
		String ident;
		SymbolTable.VariableSymbol vs;

		public DeclVar(Type type, String ident) {
			this.type = type;
			this.ident = ident;
		}

		public String pp() {
			return "" + ident;
		}

		public RetIdentificateur toIR(SymbolTable ts) throws TypeException {
			vs = new VariableSymbol(type, ident);

			if (!ts.add(vs)) {
				throw new TypeException(ident + " est deja definie.");
			}

			Llvm.IR constIR = new Llvm.IR(Llvm.empty(), Llvm.empty());

			Llvm.Instruction cons = new Llvm.DeclVar(type.toLlvmType(), ident);

			constIR.appendCode(cons);

			return new RetIdentificateur(constIR, new IntType(), "" + ident);
		}
	}

	static public class IdentVar extends Identificateur {
		Type type;
		String ident;

		public IdentVar(Type type, String ident) {
			this.type = type;
			this.ident = ident;
		}

		public String pp() {
			return "" + ident;
		}

		public RetIdentificateur toIR(SymbolTable ts) throws TypeException {

			if (ts.lookup(ident) == null) {
				throw new TypeException(ident + " non declaree");
			}

			Llvm.IR constIR = new Llvm.IR(Llvm.empty(), Llvm.empty());

			Llvm.Instruction cons = new Llvm.Var(type.toLlvmType(), ident);

			constIR.appendCode(cons);

			return new RetIdentificateur(constIR, new IntType(), "" + ident);
		}
	}

	/**
	 * All toIR methods returns the IR, plus extra information (synthesized
	 * attributes) They can take extra arguments (inherited attributes)
	 */

	static public abstract class Expression {
		public abstract String pp();

		public abstract RetExpression toIR(SymbolTable ts) throws TypeException;

		// Object returned by toIR on expressions, with IR + synthesized
		// attributes
		static public class RetExpression {
			// The LLVM IR:
			public Llvm.IR ir;
			// And additional stuff:
			public Type type; // The type of the expression
			public String result; // The name containing the expression's result
			// (either an identifier, or an immediate value)

			public RetExpression(Llvm.IR ir, Type type, String result) {
				this.ir = ir;
				this.type = type;
				this.result = result;
			}
		}
	}

	static public class CondExpression extends Expression {

		Expression expr = null;

		public CondExpression(Expression expr) {
			this.expr = expr;
		}

		public String pp() {
			return "( " + expr + " )";
		}

		public RetExpression toIR(SymbolTable ts) throws TypeException {

			RetExpression exprRet = expr.toIR(ts);

			String result = Utils.newtmp();

			Llvm.Instruction equalExpr = new Llvm.Equal(new IntType().toLlvmType(), exprRet.result, "0", result);

			exprRet.ir.appendCode(equalExpr);

			return new RetExpression(exprRet.ir, new BoolType(), result);
		}

	}

	static public class exprIdent extends Expression {

		String ident = null;

		public exprIdent(String ident) {
			this.ident = ident;
		}

		public String pp() {
			return "( " + ident + " )";
		}

		public RetExpression toIR(SymbolTable ts) throws TypeException {

			if (ts.lookup(ident) == null) {
				throw new TypeException(ident + " non declaree");
			}

			Llvm.IR exprIR = new Llvm.IR(Llvm.empty(), Llvm.empty());

			String result = Utils.newtmp();

			Llvm.Instruction identExpr = new Llvm.IdentExpr(new IntType().toLlvmType(), ident, result);

			exprIR.appendCode(identExpr);

			return new RetExpression(exprIR, new IntType(), result);
		}

	}

	// Concrete class for Expression: constant (integer) case
	static public class IntegerExpression extends Expression {
		int value;

		public IntegerExpression(int value) {
			this.value = value;
		}

		public String pp() {
			return "" + value;
		}

		public RetExpression toIR(SymbolTable ts) {
			// Here we simply return an empty IR
			// the `result' of this expression is the integer itself (as string)
			return new RetExpression(new Llvm.IR(Llvm.empty(), Llvm.empty()), new IntType(), "" + value);
		}
	}

	// Warning: this is the type from VSL+, not the LLVM types!
	static public abstract class Type {
		public abstract String pp();

		public abstract Llvm.Type toLlvmType();
	}

	static class IntType extends Type {
		public String pp() {
			return "INT";
		}

		public boolean equals(Object obj) {
			return obj instanceof IntType;
		}

		public Llvm.Type toLlvmType() {
			return new Llvm.IntType();
		}
	}

	static class BoolType extends Type {
		public String pp() {
			return "BOOL";
		}

		public boolean equals(Object obj) {
			return obj instanceof BoolType;
		}

		public Llvm.Type toLlvmType() {
			return new Llvm.BoolType();
		}
	}

	static class VoidType extends Type {
		public String pp() {
			return "VOID";
		}

		public boolean equals(Object obj) {
			return obj instanceof VoidType;
		}

		public Llvm.Type toLlvmType() {
			return new Llvm.VoidType();
		}
	}

	// Concrete class for Expression: add case
	static public class AddExpression extends Expression {

		Expression left;
		Expression right;

		public AddExpression(Expression left, Expression right) {
			this.left = left;
			this.right = right;
		}

		// Pretty-printer
		public String pp() {
			return "(" + left.pp() + " + " + right.pp() + ")";
		}

		// IR generation
		public RetExpression toIR(SymbolTable ts) throws TypeException {
			RetExpression leftRet = left.toIR(ts);
			RetExpression rightRet = right.toIR(ts);
			// We check if the types mismatches
			if (!leftRet.type.equals(rightRet.type)) {
				throw new TypeException("type mismatch: have " + leftRet.type + " and " + rightRet.type);
			}

			// We base our build on the left generated IR:
			// append right code
			leftRet.ir.append(rightRet.ir);

			// allocate a new identifier for the result
			String result = Utils.newtmp();

			// new add instruction result = left + right
			Llvm.Instruction add = new Llvm.Add(leftRet.type.toLlvmType(), leftRet.result, rightRet.result, result);

			// append this instruction
			leftRet.ir.appendCode(add);

			// return the generated IR, plus the type of this expression
			// and where to find its result
			return new RetExpression(leftRet.ir, leftRet.type, result);
		}
	}

	static public class SousExpression extends Expression {
		Expression left;
		Expression right;

		public SousExpression(Expression left, Expression right) {
			this.left = left;
			this.right = right;
		}

		// Pretty-printer
		public String pp() {
			return "(" + left.pp() + " - " + right.pp() + ")";
		}

		// IR generation
		public RetExpression toIR(SymbolTable ts) throws TypeException {
			RetExpression leftRet = left.toIR(ts);
			RetExpression rightRet = right.toIR(ts);

			// We check if the types mismatches
			if (!leftRet.type.equals(rightRet.type)) {
				throw new TypeException("type mismatch: have " + leftRet.type + " and " + rightRet.type);
			}

			// We base our build on the left generated IR:
			// append right code
			leftRet.ir.append(rightRet.ir);

			// allocate a new identifier for the result
			String result = Utils.newtmp();

			// new add instruction result = left - right
			Llvm.Instruction sous = new Llvm.Sous(leftRet.type.toLlvmType(), leftRet.result, rightRet.result, result);

			// append this instruction
			leftRet.ir.appendCode(sous);

			// return the generated IR, plus the type of this expression
			// and where to find its result
			return new RetExpression(leftRet.ir, leftRet.type, result);
		}
	}

	static public class MultExpression extends Expression {
		Expression left;
		Expression right;

		public MultExpression(Expression left, Expression right) {
			this.left = left;
			this.right = right;
		}

		// Pretty-printer
		public String pp() {
			return "(" + left.pp() + " * " + right.pp() + ")";
		}

		// IR generation
		public RetExpression toIR(SymbolTable ts) throws TypeException {
			RetExpression leftRet = left.toIR(ts);
			RetExpression rightRet = right.toIR(ts);

			// We check if the types mismatches
			if (!leftRet.type.equals(rightRet.type)) {
				throw new TypeException("type mismatch: have " + leftRet.type + " and " + rightRet.type);
			}

			// We base our build on the left generated IR:
			// append right code
			leftRet.ir.append(rightRet.ir);

			// allocate a new identifier for the result
			String result = Utils.newtmp();

			// new add instruction result = left * right
			Llvm.Instruction mult = new Llvm.Mult(leftRet.type.toLlvmType(), leftRet.result, rightRet.result, result);

			// append this instruction
			leftRet.ir.appendCode(mult);

			// return the generated IR, plus the type of this expression
			// and where to find its result
			return new RetExpression(leftRet.ir, leftRet.type, result);
		}
	}

	static public class DivExpression extends Expression {
		Expression left;
		Expression right;

		public DivExpression(Expression left, Expression right) {
			this.left = left;
			this.right = right;
		}

		// Pretty-printer
		public String pp() {
			return "(" + left.pp() + " / " + right.pp() + ")";
		}

		// IR generation
		public RetExpression toIR(SymbolTable ts) throws TypeException {
			RetExpression leftRet = left.toIR(ts);
			RetExpression rightRet = right.toIR(ts);

			// We check if the types mismatches
			if (!leftRet.type.equals(rightRet.type)) {
				throw new TypeException("type mismatch: have " + leftRet.type + " and " + rightRet.type);
			}

			// We base our build on the left generated IR:
			// append right code
			leftRet.ir.append(rightRet.ir);

			// allocate a new identifier for the result
			String result = Utils.newtmp();

			// new add instruction result = left / right
			Llvm.Instruction div = new Llvm.Div(leftRet.type.toLlvmType(), leftRet.result, rightRet.result, result);

			// append this instruction
			leftRet.ir.appendCode(div);

			// return the generated IR, plus the type of this expression
			// and where to find its result
			return new RetExpression(leftRet.ir, leftRet.type, result);
		}
	}

}
