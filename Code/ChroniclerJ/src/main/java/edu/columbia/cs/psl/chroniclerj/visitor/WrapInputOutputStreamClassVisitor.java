package edu.columbia.cs.psl.chroniclerj.visitor;

import edu.columbia.cs.psl.chroniclerj.ChroniclerJExportRunner;
import edu.columbia.cs.psl.chroniclerj.replay.ReplayUtils;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

public class WrapInputOutputStreamClassVisitor extends ClassVisitor implements Opcodes {

    public WrapInputOutputStreamClassVisitor(ClassVisitor cv) {
        super(Opcodes.ASM5, cv);
    }

    @Override
    public MethodVisitor visitMethod(int acc, String name, String desc, String signature, String[] exceptions) {
        MethodVisitor mv = new WrapInputOutputStreamMethodVisitor(cv.visitMethod(acc, name, desc, signature, exceptions));
        return mv;
    }

    class WrapInputOutputStreamMethodVisitor extends MethodVisitor {

        public WrapInputOutputStreamMethodVisitor(MethodVisitor mv) {
            super(Opcodes.ASM5, mv);
        }

        @Override
        public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itfc) {
            if (owner.contains("InputStream") && name.equals("<init>")) {
                super.visitMethodInsn(Opcodes.INVOKESTATIC, Type.getInternalName(ReplayUtils.class), "wrap", "()V", false);
            } else {
                super.visitMethodInsn(opcode, owner, name, desc, itfc);
            }
        }
    }
}
