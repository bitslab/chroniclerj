package edu.columbia.cs.psl.chroniclerj.visitor;

import edu.columbia.cs.psl.chroniclerj.Constants;
import edu.columbia.cs.psl.chroniclerj.Instrumenter;
import edu.columbia.cs.psl.chroniclerj.MethodCall;
import edu.columbia.cs.psl.chroniclerj.replay.FinalizerReplayingMethodVisitor;
import edu.columbia.cs.psl.chroniclerj.replay.NonDeterministicReplayMethodVisitor;
import org.apache.log4j.Logger;
import org.objectweb.asm.*;
import org.objectweb.asm.commons.AnalyzerAdapter;
import org.objectweb.asm.commons.JSRInlinerAdapter;
import org.objectweb.asm.commons.LocalVariablesSorter;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.FrameNode;
import org.objectweb.asm.tree.MethodInsnNode;

import java.util.*;

import static edu.columbia.cs.psl.chroniclerj.Instrumenter.transformer;

public class NDCombinedClassVisitor extends ClassVisitor implements Opcodes {

    private String className;
    private String superName;
    private String[] interfaces;

    private boolean lrFlag;

    private boolean isAClass = true;

    private static Logger logger = Logger.getLogger(NDCombinedClassVisitor.class);

    public static HashSet<String> callbackClasses = new HashSet<String>();
    public static HashSet<String> callbackMethods = new HashSet<String>();
    static {
        Scanner s;
        try {
            s = new Scanner(NDCombinedClassVisitor.class
                    .getResourceAsStream("/listenerMethods.txt"));
            while (s.hasNextLine()) {
                String l = s.nextLine();
                callbackMethods.add(l);
                callbackClasses.add(l.substring(0, l.indexOf(".")));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public NDCombinedClassVisitor(ClassVisitor cv, boolean lrFlag) {
        super(Opcodes.ASM5, cv);
        this.lrFlag = lrFlag;
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        super.visit(version, access, name, signature, superName, interfaces);
        this.className = name;
        this.superName = superName;
        this.interfaces = interfaces;
        logger.debug("Visiting " + name + " for instrumentation");
        if ((access & Opcodes.ACC_INTERFACE) != 0)
            isAClass = false;
    }

    private boolean isFirstConstructor = true;
    public static Set<Type> classesToGenerate = transformer.classesToGenerate;

    @Override
    public MethodVisitor visitMethod(int acc, String name, String desc, String signature, String[] exceptions) {
        if (lrFlag) {
            MethodVisitor primaryMV = cv.visitMethod(acc, name, desc, signature, exceptions);
            MethodVisitor smv = new ReflectionInterceptingMethodVisitor(primaryMV);
            smv = new FinalizerLoggingMethodVisitor(smv, name, desc, className);
            if (name.equals("main") && desc.equals("([Ljava/lang/String;)V")) {
                smv = new MainLoggingMethodVisitor(smv, acc, name, desc, className);
            }
            if (Instrumenter.classIsCallback(className, superName, interfaces)) {
                AnalyzerAdapter analyzer = new AnalyzerAdapter(className, acc, name, desc, smv);
                CloningAdviceAdapter caa = new CloningAdviceAdapter(analyzer, acc, name, desc, className, analyzer);
                smv = new CallbackLoggingMethodVisitor(caa, acc, name, desc, className, null, caa, superName, interfaces);
                smv = new JSRInlinerAdapter(smv, acc, name, desc, signature, exceptions);
                smv = new LocalVariablesSorter(acc, desc, smv);
                caa.setLocalVariableSorter((LocalVariablesSorter) smv);
            }
            if (isAClass && !name.equals(Constants.INNER_COPY_METHOD_NAME)
                    && !name.equals(Constants.OUTER_COPY_METHOD_NAME)
                    && !name.equals(Constants.SET_FIELDS_METHOD_NAME)
                    && !className.startsWith("com/thoughtworks")) {
                AnalyzerAdapter analyzer = new AnalyzerAdapter(className, acc, name, desc, smv);
                NonDeterministicLoggingMethodVisitor cloningMV = new NonDeterministicLoggingMethodVisitor(analyzer, acc, name, desc, className, superName, isFirstConstructor, analyzer, classesToGenerate );
                if (name.equals("<init>"))
                    isFirstConstructor = false;
                cloningMV.setClassVisitor(this);
                JSRInlinerAdapter mv2 = new JSRInlinerAdapter(cloningMV, acc, name, desc, signature, exceptions);
                LocalVariablesSorter sorter = new LocalVariablesSorter(acc, desc, mv2);
                cloningMV.setLocalVariableSorter(sorter);
                return sorter;
            } else
                return smv;
        } else {
            if (isAClass) {
                MethodVisitor smv = cv.visitMethod(acc, name, desc, signature, exceptions);
                FinalizerReplayingMethodVisitor fmv = new FinalizerReplayingMethodVisitor(smv,
                        name, desc, this.className);
                AnalyzerAdapter analyzer = new AnalyzerAdapter(className, acc, name, desc, fmv);
                LocalVariablesSorter sorter = new LocalVariablesSorter(acc, desc, analyzer);
                NonDeterministicReplayMethodVisitor cloningMV = new NonDeterministicReplayMethodVisitor(
                        Opcodes.ASM5, sorter, acc, name, desc, className, isFirstConstructor, analyzer,
                        Instrumenter.classIsCallback(className, superName, interfaces) && name.equals("<init>"));
                if (name.equals("<init>"))
                    isFirstConstructor = false;
                cloningMV.setClassVisitor(this);
                JSRInlinerAdapter mv2 = new JSRInlinerAdapter(cloningMV, acc, name, desc, signature,
                        exceptions);

                return mv2;
            } else
                return cv.visitMethod(acc, name, desc, signature, exceptions);
        }
    }

    public HashSet<MethodCall> getLoggedMethodCalls() {
        return loggedMethodCalls;
    }

    private HashSet<MethodCall> loggedMethodCalls = new HashSet<MethodCall>();

    private HashMap<MethodCall, MethodInsnNode> captureMethodsToGenerate = new HashMap<MethodCall, MethodInsnNode>();

    @Override
    public void visitEnd() {
        if (lrFlag) {
            for (MethodCall mc : captureMethodsToGenerate.keySet()) {
                MethodInsnNode mi = captureMethodsToGenerate.get(mc);
                String methodDesc = mi.desc;

                String captureDesc = mi.desc;

                int opcode = Opcodes.ACC_PRIVATE | Opcodes.ACC_STATIC | Opcodes.ACC_FINAL;
                if (mi.getOpcode() == Opcodes.INVOKESPECIAL && !mi.name.equals("<init>"))
                    opcode = Opcodes.ACC_PRIVATE | Opcodes.ACC_FINAL;
                else if (mi.getOpcode() != Opcodes.INVOKESTATIC) {
                    captureDesc = "(L" + mi.owner + ";";
                    for (Type t : Type.getArgumentTypes(mi.desc))
                        captureDesc += t.getDescriptor();
                    captureDesc += ")" + Type.getReturnType(mi.desc).getDescriptor();
                }
                MethodVisitor mv = super.visitMethod(opcode, mc.getCapturePrefix() + "_capture",
                        captureDesc, null, null);
                AnalyzerAdapter analyzer = new AnalyzerAdapter(className, opcode, mc.getCapturePrefix() + "_capture", captureDesc, mv);
                CloningAdviceAdapter caa = new CloningAdviceAdapter(analyzer, opcode,
                        mc.getCapturePrefix() + "_capture", captureDesc, className, analyzer);
                LocalVariablesSorter lvs = new LocalVariablesSorter(opcode, captureDesc, caa);
                caa.setLocalVariableSorter(lvs);
                Type[] args = Type.getArgumentTypes(captureDesc);
                if (mi.name.equals("<init>")) {
                    int j = 0;
                    caa.visitVarInsn(ALOAD, 0);
                    j++;
                    for (int i = 0; i < args.length; i++) {
                        caa.visitVarInsn(args[i].getOpcode(ILOAD), j);
                        j += args[i].getSize();
                    }
                    lvs.visitMethodInsn(Opcodes.INVOKESPECIAL, mi.owner, mi.name, mi.desc, mi.itf);
                    caa.visitVarInsn(ALOAD, 0);
                } else {
                    int j = 0;
                    if ((opcode & Opcodes.ACC_STATIC) == 0) {
                        caa.visitVarInsn(ALOAD, 0);
                        j++;
                    }
                    for (int i = 0; i < args.length; i++) {
                        caa.visitVarInsn(args[i].getOpcode(ILOAD), j);
                        j += args[i].getSize();
                    }
                    lvs.visitMethodInsn(mi.getOpcode(), mi.owner, mi.name, mi.desc, mi.itf);
                    j = 0;
                    for (int i = 0; i < args.length; i++) {
                        if (args[i].getSort() == Type.ARRAY) {
                            boolean minimalCopy = (Type.getReturnType(methodDesc).getSort() == Type.INT);
                            if (minimalCopy) {
                                FrameNode fn = CloningAdviceAdapter.getCurrentFrameNode(analyzer);
                                caa.dup();
                                Label isNegative = new Label();
                                Label notNegative = new Label();
                                caa.visitJumpInsn(Opcodes.IFLT, isNegative);
                                caa.dup();
                                FrameNode fn2 = CloningAdviceAdapter.getCurrentFrameNode(analyzer);
                                caa.visitJumpInsn(Opcodes.GOTO, notNegative);
                                caa.visitLabel(isNegative);
                                fn.accept(caa);
                                caa.visitInsn(ICONST_0);
                                caa.visitLabel(notNegative);
                                fn2.accept(caa);

                            }
                            caa.visitVarInsn(args[i].getOpcode(ILOAD), j);
                            caa.logValueAtTopOfStackToArray(MethodCall.getLogClassName(args[i]),
                                    "aLog", "[Ljava/lang/Object;", args[i], true, mi.owner + "."
                                            + mi.name + "->_" + i + "\t" + args[i].getDescriptor()
                                            + "\t\t" + className, minimalCopy, false);
                            if (args[i].getSize() == 1)
                                caa.pop();
                            else
                                caa.pop2();
                        }
                        j += args[i].getSize();
                    }
                }
                caa.visitInsn(Type.getReturnType(mi.desc).getOpcode(IRETURN));
                caa.visitMaxs(0, 0);
                caa.visitEnd();
            }
            if (isAClass) {
                FieldNode fn = new FieldNode(Opcodes.ACC_PRIVATE, Instrumenter.FIELD_LOGICAL_CLOCK,
                        "J", null, 0L);
                fn.accept(this);
            }
        } else {
            if (isAClass) {
                FieldNode fn = new FieldNode(Opcodes.ACC_PRIVATE, Instrumenter.FIELD_LOGICAL_CLOCK,
                        "J", null, 0);
                fn.accept(this);
            }
        }

        /*if (lrFlag) {
            for (Type t : this.classesToGenerate) {
                MethodVisitor methodVisitor;

                String[] splitName = t.getInternalName().split("/");
                String newClass = "edu/columbia/cs/psl/chroniclerj/visitor/" + splitName[splitName.length - 1] + "$$InputStream";
                this.visit(V1_8, ACC_PUBLIC | ACC_SUPER, newClass, null, t.getInternalName(), null);

                this.visitSource("WrapInputStream.java", null);

                {
                    methodVisitor = this.visitMethod(ACC_PUBLIC, "<init>", "([B)V", null, null);
                    methodVisitor.visitCode();
                    Label label0 = new Label();
                    methodVisitor.visitLabel(label0);
                    methodVisitor.visitLineNumber(8, label0);
                    methodVisitor.visitVarInsn(ALOAD, 0);
                    methodVisitor.visitVarInsn(ALOAD, 1);
                    methodVisitor.visitMethodInsn(INVOKESPECIAL, "java/io/ByteArrayInputStream", "<init>", "([B)V", false);
                    Label label1 = new Label();
                    methodVisitor.visitLabel(label1);
                    methodVisitor.visitLineNumber(9, label1);
                    methodVisitor.visitInsn(RETURN);
                    Label label2 = new Label();
                    methodVisitor.visitLabel(label2);
                    methodVisitor.visitLocalVariable("this", "Ledu/columbia/cs/psl/chroniclerj/visitor/WrapInputStream;", null, label0, label2, 0);
                    methodVisitor.visitLocalVariable("buf", "[B", null, label0, label2, 1);
                    methodVisitor.visitMaxs(2, 2);
                    methodVisitor.visitEnd();
                }
                {
                    methodVisitor = this.visitMethod(ACC_PUBLIC | ACC_SYNCHRONIZED, "read", "()I", null, null);
                    methodVisitor.visitCode();
                    Label label0 = new Label();
                    methodVisitor.visitLabel(label0);
                    methodVisitor.visitLineNumber(13, label0);
                    methodVisitor.visitVarInsn(ALOAD, 0);
                    methodVisitor.visitMethodInsn(INVOKESPECIAL, "java/io/ByteArrayInputStream", "read", "()I", false);
                    methodVisitor.visitInsn(IRETURN);
                    Label label1 = new Label();
                    methodVisitor.visitLabel(label1);
                    methodVisitor.visitLocalVariable("this", "Ledu/columbia/cs/psl/chroniclerj/visitor/WrapInputStream;", null, label0, label1, 0);
                    methodVisitor.visitMaxs(1, 1);
                    methodVisitor.visitEnd();
                }
                {
                    methodVisitor = this.visitMethod(ACC_PUBLIC | ACC_SYNCHRONIZED, "read", "([BII)I", null, null);
                    methodVisitor.visitCode();
                    Label label0 = new Label();
                    methodVisitor.visitLabel(label0);
                    methodVisitor.visitLineNumber(18, label0);
                    methodVisitor.visitVarInsn(ALOAD, 0);
                    methodVisitor.visitVarInsn(ALOAD, 1);
                    methodVisitor.visitVarInsn(ILOAD, 2);
                    methodVisitor.visitVarInsn(ILOAD, 3);
                    methodVisitor.visitMethodInsn(INVOKESPECIAL, "java/io/ByteArrayInputStream", "read", "([BII)I", false);
                    methodVisitor.visitInsn(IRETURN);
                    Label label1 = new Label();
                    methodVisitor.visitLabel(label1);
                    methodVisitor.visitLocalVariable("this", "Ledu/columbia/cs/psl/chroniclerj/visitor/WrapInputStream;", null, label0, label1, 0);
                    methodVisitor.visitLocalVariable("b", "[B", null, label0, label1, 1);
                    methodVisitor.visitLocalVariable("off", "I", null, label0, label1, 2);
                    methodVisitor.visitLocalVariable("len", "I", null, label0, label1, 3);
                    methodVisitor.visitMaxs(4, 4);
                    methodVisitor.visitEnd();
                }
                {
                    methodVisitor = this.visitMethod(ACC_PUBLIC, "read", "([B)I", null, new String[]{"java/io/IOException"});
                    methodVisitor.visitCode();
                    Label label0 = new Label();
                    methodVisitor.visitLabel(label0);
                    methodVisitor.visitLineNumber(23, label0);
                    methodVisitor.visitVarInsn(ALOAD, 0);
                    methodVisitor.visitVarInsn(ALOAD, 1);
                    methodVisitor.visitMethodInsn(INVOKESPECIAL, "java/io/ByteArrayInputStream", "read", "([B)I", false);
                    methodVisitor.visitInsn(IRETURN);
                    Label label1 = new Label();
                    methodVisitor.visitLabel(label1);
                    methodVisitor.visitLocalVariable("this", "Ledu/columbia/cs/psl/chroniclerj/visitor/WrapInputStream;", null, label0, label1, 0);
                    methodVisitor.visitLocalVariable("b", "[B", null, label0, label1, 1);
                    methodVisitor.visitMaxs(2, 2);
                    methodVisitor.visitEnd();
                }
                this.visitEnd();
            }
        }*/

        super.visitEnd();
    }

    public void addFieldMarkup(Collection<MethodCall> calls) {
        logger.debug("Received field markup from method visitor (" + calls.size() + ")");
        loggedMethodCalls.addAll(calls);
    }

    public String getClassName() {
        return className;
    }

    public void addCaptureMethodsToGenerate(HashMap<MethodCall, MethodInsnNode> captureMethodsToGenerate) {
        this.captureMethodsToGenerate.putAll(captureMethodsToGenerate);
    }
}
