package TP2;

import java.util.*;

// This file contains a simple LLVM IR representation
// and methods to generate its string representation

public class Llvm {
	static public class IR {
		List<Instruction> header; // IR instructions to be placed before the
		// code (global definitions)
		List<Instruction> code; // main code

		public IR(List<Instruction> header, List<Instruction> code) {
			this.header = header;
			this.code = code;
		}

		// append an other IR
		public IR append(IR other) {
			header.addAll(other.header);
			code.addAll(other.code);
			return this;
		}

		// append a code instruction
		public IR appendCode(Instruction inst) {
			code.add(inst);
			return this;
		}

		// append a code header
		public IR appendHeader(Instruction inst) {
			header.add(inst);
			return this;
		}

		// Final string generation
		public String toString() {
			// This header describe to LLVM the target
			// and declare the external function printf
			StringBuilder r = new StringBuilder("; Target\n" + "target triple = \"x86_64-unknown-linux-gnu\"\n"
					+ "; External declaration of the printf function\n"
					+ "declare i32 @printf(i8* noalias nocapture, ...)\n" + "\n; Actual code begins\n\n");

			for (Instruction inst : header)
				r.append(inst);

			r.append("\n\n");

			// We create the function main
			// TODO : remove this when you extend the language
			// r.append("define i32 @main() {\n");

			for (Instruction inst : code)
				r.append(inst);

			// TODO : remove this when you extend the language
			// r.append("}\n");

			return r.toString();

		}
	}

	// Returns a new empty list of instruction, handy
	static public List<Instruction> empty() {
		return new ArrayList<Instruction>();
	}

	// LLVM Types
	static public abstract class Type {
		public abstract String toString();
	}

	// Type Int
	static public class IntType extends Type {
		public String toString() {
			return "i32";
		}
	}

	// Type Bool
	static public class BoolType extends Type {
		public String toString() {
			return "i1";
		}
	}

	// Type void
	static public class VoidType extends Type {
		public String toString() {
			return "void";
		}
	}

	static public class Fonction extends Instruction {
		Type type;
		String name;
		List<String> params;

		public Fonction(Type type, String name, List<String> params) {
			this.type = type;
			this.name = name;
			this.params = params;
		}

		public String toString() {
			String str = "define " + type + " @" + name + "(";
			// PAS TOUCHE
			for (int i = 0; i < params.size(); i++) {
				str += new IntType().toString() + " %" + params.get(i);
				if (i < params.size() - 1) {
					str += ", ";
				}
			}
			str += "){\n";
			// A MODIFIER
			for (int i = 0; i < params.size(); i++) {
				DeclVar dv = new DeclVar(type, params.get(i));
				str += dv.toString();
			}

			return str;
		}
	}

	static public class FonctionEnd extends Instruction {

		@Override
		public String toString() {
			return "}\n\n";
		}

	}

	static public class ConstPar extends Instruction {

		Type type;
		String result;

		public ConstPar(Type type, String result) {
			this.type = type;
			this.result = result;
		}

		public String toString() {
			String pour100 = "";
			if (Character.isLetter(result.charAt(0))) {
				pour100 = "%";
			}
			return type + " " + pour100 + result;
		}

	}

	static public class CallFonction extends Instruction {
		Type type;
		String name;
		List<String> params;

		public CallFonction(Type type, String name, List<String> params) {
			this.type = type;
			this.name = name;
			this.params = params;
		}

		public String toString() {
			String str = Utils.indent(1) + "call " + type + " @" + name + "(";

			for (int i = 0; i < params.size(); i++) {
				str += params.get(i);
				if (i < params.size() - 1) {
					str += ", ";
				}
			}
			str += ")\n";
			return str;
		}
	}

	static public class CallFonctionInt extends Instruction {
		Type type;
		String name;
		List<String> params;
		String result;

		public CallFonctionInt(Type type, String name, List<String> params, String result) {
			this.type = type;
			this.name = name;
			this.params = params;
			this.result = result;
		}

		public String toString() {
			String str = Utils.indent(1) + result + " = " + "call " + type + " @" + name + "(";

			for (int i = 0; i < params.size(); i++) {
				str += params.get(i);
				if (i < params.size() - 1) {
					str += ", ";
				}
			}
			str += ")\n";
			return str;
		}
	}

	// LLVM IR Instructions
	static public abstract class Instruction {
		public abstract String toString();
	}

	static public class WhileInstr extends Instruction {
		Type type;
		String labelWhile;
		String labelDo;
		String labelDone;
		String cond;

		public WhileInstr(Type type, String cond, String labelWhile, String labelDo, String labelDone) {
			this.type = type;
			this.labelWhile = labelWhile;
			this.labelDo = labelDo;
			this.labelDone = labelDone;
			this.cond = cond;
		}

		// TODO OPTIMISATION POSSIBLE EN FUSIONANT AVEC IFINSTR

		public String toString() {
			return Utils.indent(1) + "br " + type + " " + cond + ", label " + "%" + labelDo + ", label " + "%"
					+ labelDone + "\n\n";
		}
	}

	static public class IfInstr extends Instruction {
		Type type;
		String labelThen;
		String labelElse;
		String cond;

		public IfInstr(Type type, String cond, String labelThen, String labelElse) {
			this.type = type;
			this.labelThen = labelThen;
			this.labelElse = labelElse;
			this.cond = cond;
		}

		public String toString() {
			return Utils.indent(1) + "br " + type + " " + cond + ", label " + "%" + labelThen + ", label " + "%"
					+ labelElse + "\n\n";
		}
	}

	static public class LabelName extends Instruction {
		String labelName;

		public LabelName(String labelName) {
			this.labelName = labelName;
		}

		public String toString() {
			return labelName + ":" + "\n";
		}
	}

	static public class AppelLabel extends Instruction {
		String labelName;

		public AppelLabel(String labelName) {
			this.labelName = labelName;
		}

		public String toString() {
			return Utils.indent(1) + "br label %" + labelName + "\n\n";
		}
	}

	static public class Equal extends Instruction {
		Type type;
		String left;
		String right;
		String lvalue;

		public Equal(Type type, String left, String right, String lvalue) {
			this.type = type;
			this.left = left;
			this.right = right;
			this.lvalue = lvalue;
		}

		public String toString() {
			return Utils.indent(1) + lvalue + " = icmp ne " + type + " " + left + ", " + right + "\n";
		}
	}

	static public class Add extends Instruction {
		Type type;
		String left;
		String right;
		String lvalue;

		public Add(Type type, String left, String right, String lvalue) {
			this.type = type;
			this.left = left;
			this.right = right;
			this.lvalue = lvalue;
		}

		public String toString() {
			return Utils.indent(1) + lvalue + " = add " + type + " " + left + ", " + right + "\n";
		}
	}

	static public class Sous extends Instruction {
		Type type;
		String left;
		String right;
		String lvalue;

		public Sous(Type type, String left, String right, String lvalue) {
			this.type = type;
			this.left = left;
			this.right = right;
			this.lvalue = lvalue;
		}

		public String toString() {
			return Utils.indent(1) + lvalue + " = sub " + type + " " + left + ", " + right + "\n";
		}
	}

	static public class Mult extends Instruction {
		Type type;
		String left;
		String right;
		String lvalue;

		public Mult(Type type, String left, String right, String lvalue) {
			this.type = type;
			this.left = left;
			this.right = right;
			this.lvalue = lvalue;
		}

		public String toString() {
			return Utils.indent(1) + lvalue + " = mul " + type + " " + left + ", " + right + "\n";
		}
	}

	static public class Div extends Instruction {
		Type type;
		String left;
		String right;
		String lvalue;

		public Div(Type type, String left, String right, String lvalue) {
			this.type = type;
			this.left = left;
			this.right = right;
			this.lvalue = lvalue;
		}

		public String toString() {
			return Utils.indent(1) + lvalue + " = udiv " + type + " " + left + ", " + right + "\n";
		}
	}

	static public class Affect extends Instruction {
		Type type;
		String identificateur;
		String expression;

		public Affect(Type type, String identificateur, String expression) {
			this.type = type;
			this.identificateur = identificateur;
			this.expression = expression;
		}

		public String toString() {
			String pour100 = "";
			if (Character.isLetter(expression.charAt(0))) {
				pour100 = "%";
			}
			return Utils.indent(1) + "store " + type + " " + pour100 + expression + ", " + type + "* %" + identificateur
					+ "\n";
		}
	}

	static public class IdentExpr extends Instruction {
		Type type;
		String identificateur;
		String tmp;

		public IdentExpr(Type type, String identificateur, String tmp) {
			this.type = type;
			this.identificateur = identificateur;
			this.tmp = tmp;
		}

		public String toString() {
			return Utils.indent(1) + tmp + " = load " + type + ", " + type + "* " + "%" + identificateur + "\n";
		}

	}

	static public class DeclVar extends Instruction {
		Type type;
		String ident;

		public DeclVar(Type type, String ident) {
			this.type = type;
			this.ident = ident;
		}

		public String toString() {
			return Utils.indent(1) + "%" + ident + " = " + "alloca " + type + "\n";
		}

	}

	static public class Var extends Instruction {
		Type type;
		String ident;

		public Var(Type type, String ident) {
			this.type = type;
			this.ident = ident;
		}

		public String toString() {
			return "";
		}

	}

	static public class Return extends Instruction {
		Type type;
		String value;

		public Return(Type type, String value) {
			this.type = type;
			this.value = value;
		}

		public String toString() {
			return Utils.indent(1) + "ret " + type + " " + value + "\n";
		}
	}

	static public class ReturnVoid extends Instruction {

		public String toString() {
			return Utils.indent(1) + "ret void\n";
		}
	}

}
