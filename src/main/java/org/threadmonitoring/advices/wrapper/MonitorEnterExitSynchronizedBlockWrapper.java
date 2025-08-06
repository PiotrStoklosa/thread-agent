package org.threadmonitoring.advices.wrapper;

import net.bytebuddy.asm.AsmVisitorWrapper;
import net.bytebuddy.description.field.FieldDescription;
import net.bytebuddy.description.field.FieldList;
import net.bytebuddy.description.method.MethodList;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.implementation.Implementation;
import net.bytebuddy.jar.asm.ClassVisitor;
import net.bytebuddy.jar.asm.ClassWriter;
import net.bytebuddy.jar.asm.MethodVisitor;
import net.bytebuddy.jar.asm.Opcodes;
import net.bytebuddy.pool.TypePool;

public class MonitorEnterExitSynchronizedBlockWrapper implements AsmVisitorWrapper {

    @Override
    public int mergeWriter(int flags) {
        return flags | ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES;
    }

    @Override
    public int mergeReader(int flags) {
        return flags;
    }

    @Override
    public net.bytebuddy.jar.asm.ClassVisitor wrap(TypeDescription typeDescription,
                                                   net.bytebuddy.jar.asm.ClassVisitor classVisitor,
                                                   Implementation.Context context,
                                                   TypePool typePool,
                                                   FieldList<FieldDescription.InDefinedShape> fieldList,
                                                   MethodList<?> methodList, int i, int i1) {

        return new ClassVisitor(Opcodes.ASM9, classVisitor) {
            @Override
            public MethodVisitor visitMethod(int access, String name, String descriptor,
                                             String signature, String[] exceptions) {
                MethodVisitor mv = super.visitMethod(access, name, descriptor, signature, exceptions);
                return new MethodVisitor(Opcodes.ASM9, mv) {

                    @Override
                    public void visitCode() {
                        super.visitCode();
                    }

                    @Override
                    public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
                        super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
                    }

                    @Override
                    public void visitInsn(int opcode) {
                        switch (opcode) {
                            case Opcodes.MONITORENTER:
                                injectLogger("logEnter");
                                super.visitInsn(Opcodes.MONITORENTER);
                                super.visitMethodInsn(
                                        Opcodes.INVOKESTATIC,
                                        "org/threadmonitoring/logger/SynchronizedLogger",
                                        "logEnter2",
                                        "()V",
                                        false
                                );
                                break;
                            case Opcodes.MONITOREXIT:
                                injectLogger("logExit");
                                super.visitInsn(Opcodes.MONITOREXIT);
                                break;
                            default:
                                super.visitInsn(opcode);
                                break;
                        }
                    }

                    private void injectLogger(String methodName) {
                        super.visitInsn(Opcodes.DUP);
                        super.visitMethodInsn(
                                Opcodes.INVOKESTATIC,
                                "org/threadmonitoring/logger/SynchronizedLogger",
                                methodName,
                                "(Ljava/lang/Object;)V",
                                false
                        );
                    }
                };
            }
        };
    }
}
