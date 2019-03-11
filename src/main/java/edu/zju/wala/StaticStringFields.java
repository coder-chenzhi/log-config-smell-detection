package edu.zju.wala;

import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.ipa.callgraph.IAnalysisCacheView;
import com.ibm.wala.ssa.IR;
import com.ibm.wala.ssa.SSAInstruction;
import com.ibm.wala.ssa.SSAInvokeInstruction;
import com.ibm.wala.ssa.SSAPutInstruction;
import com.ibm.wala.types.FieldReference;

import java.util.HashMap;
import java.util.Map;

public class StaticStringFields {


    private static Map<String, String> fields2String = new HashMap<>();


    public static void addClass(IClass clazz, IAnalysisCacheView cache) {
        for(IMethod method: clazz.getDeclaredMethods()) {
            if ("<clinit>".equals(method.getName().toString())) {
                IR ir = cache.getIR(method); // can be null
                SSAInstruction[] instructions = ir.getInstructions();
                for (int i = 0; i < instructions.length; i++) {
                    SSAInstruction instruction = instructions[i];
                    if (instruction instanceof SSAPutInstruction) {
                        FieldReference field = ((SSAPutInstruction) instruction).getDeclaredField();
                        int varIndex = instruction.getUse(0);
                        if (ir.getSymbolTable().isStringConstant(varIndex)) {
                            String value = ir.getSymbolTable().getValueString(varIndex);
                            fields2String.put(field.toString(), value);
                        }
                    }
                }
            }
        }
    }

    public static String getStaticStringField(FieldReference field) {
        String name = field.toString();
        if (fields2String.containsKey(name)) {
            return fields2String.get(name);
        }
        return null;
    }

}
