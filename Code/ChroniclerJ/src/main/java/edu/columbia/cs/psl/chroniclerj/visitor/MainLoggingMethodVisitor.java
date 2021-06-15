
package edu.columbia.cs.psl.chroniclerj.visitor;

import edu.columbia.cs.psl.chroniclerj.replay.ReplayUtils;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.InstructionAdapter;

import edu.columbia.cs.psl.chroniclerj.ChroniclerJExportRunner;

public class MainLoggingMethodVisitor extends InstructionAdapter {

    private String className;
    private boolean isStatic;

    protected MainLoggingMethodVisitor(MethodVisitor mv, int access, String name,
            String desc, String className) {
        super(Opcodes.ASM5, mv);
        this.className = className;

        this.isStatic = ((access & Opcodes.ACC_STATIC) != 0);
    }

    @Override
    public void visitCode() {
    	super.visitCode();
        visitLdcInsn(this.className);
        if (this.isStatic)
            super.visitVarInsn(Opcodes.ALOAD, 0);
        else
            super.visitVarInsn(Opcodes.ALOAD, 1);
        super.visitMethodInsn(Opcodes.INVOKESTATIC, Type.getInternalName(ChroniclerJExportRunner.class), "connect", "()V", false);
        super.visitMethodInsn(Opcodes.INVOKESTATIC, Type.getInternalName(ChroniclerJExportRunner.class), "logMain", "(Ljava/lang/String;[Ljava/lang/String;)V", false);
    }
}
