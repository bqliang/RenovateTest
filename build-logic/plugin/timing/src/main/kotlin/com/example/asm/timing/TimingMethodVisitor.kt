package com.example.asm.timing

import org.objectweb.asm.AnnotationVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Type
import org.objectweb.asm.commons.AdviceAdapter

class TimingMethodVisitor(
    api: Int,
    methodVisitor: MethodVisitor,
    access: Int,
    private val methodName: String?,
    descriptor: String?,
    private val className: String,
    private val logTag: String
) : AdviceAdapter(api, methodVisitor, access, methodName, descriptor) {

    private var hasLogTimeAnnotation = false
    private var startTimeLocalVarIndex: Int = 0

    // 1. 访问方法的注解
    override fun visitAnnotation(descriptor: String?, visible: Boolean): AnnotationVisitor? {
        if (descriptor == "Lcom/example/aop/LogTime;") { // 通过描述符判断注解
            hasLogTimeAnnotation = true
        }
        return super.visitAnnotation(descriptor, visible)
    }

    // 2. 在方法进入时被调用
    override fun onMethodEnter() {
        super.onMethodEnter()
        if (hasLogTimeAnnotation) {
            // newLocal() 是一个便捷方法, 会在当前方法的局部变量表保留一个新的槽位, 返回局部变量索引
            startTimeLocalVarIndex = newLocal(Type.LONG_TYPE /* 指定新局部变量的数据类型 */)

            // 调用静态方法 System.currentTimeMillis()
            // mv 是 MethodVisitor, 在父类中定义的
            mv.visitMethodInsn( // visitMethodInsn 方法用于添加一个方法调用指令
                /* opcode = */ INVOKESTATIC,      // Invoke Static: JVM 字节码指令，它告诉 JVM 去调用一个 static（静态）方法
                /* owner = */ "java/lang/System", // 拥有该方法的类的内部名称
                /* name = */ "currentTimeMillis", // 想要调用的方法的名称
                /* descriptor = */ "()J",         // 方法描述符, () 表示该方法没有参数, J 表示方法的返回值类型是 long (J 是 long 的 JVM 类型签名)
                /* isInterface = */ false         // 指示 owner（即 java/lang/System）是否是一个接口
            )

            // 将上一步的结果（时间戳）存入我们之前创建的局部变量中
            mv.visitVarInsn( // 此方法用于添加一个与局部变量交互的指令（如加载或存储）
                /* opcode = */ LSTORE,        // LSTORE 是一个复合指令: STORE 表示“存储”, 会从操作数栈的栈顶弹出一个值, L 表示 long
                /* varIndex = */ startTimeLocalVarIndex // 局部变量索引, 指定要存储到哪一个局部变量槽位
            )
        }
    }

    // 3. 在方法退出时被调用 (无论是正常返回还是异常抛出)
    override fun onMethodExit(opcode: Int) {
        if (hasLogTimeAnnotation) {
            /**
             * 以下部分的等效代码是：
             * long endTime = System.currentTimeMillis();
             * long duration = endTime - startTime;
             * String msg = new StringBuilder()
             *         .append("MyClass.myMethod execution time: ")
             *         .append(duration)
             *         .append(" ms")
             *         .toString();
             * Log.d(logTag, msg);
             */

            // 再次调用 System.currentTimeMillis() 获取结束时间, 不再赘述
            mv.visitMethodInsn(INVOKESTATIC, "java/lang/System", "currentTimeMillis", "()J", false)
            // 加载方法开始时存储的时间戳
            mv.visitVarInsn(LLOAD, startTimeLocalVarIndex)
            // 计算差值 (结束时间 - 开始时间)
            mv.visitInsn(LSUB)
            // 将差值（long类型）存入一个新的局部变量
            val durationLocalVarIndex = newLocal(Type.LONG_TYPE)
            mv.visitVarInsn(LSTORE, durationLocalVarIndex)


            // 准备打印日志
            mv.visitLdcInsn(logTag) // LDC (Load Constant) 加载一个常量到操作数栈的栈顶
            // 此时的操作数栈: [日志Tag(String)]

            mv.visitTypeInsn( // 访问与“类型”相关的指令
                /* opcode = */ NEW,                    // NEW
                /* type = */ "java/lang/StringBuilder" // 要创建的对象的类型
            ) // 在堆上分配一个 StringBuilder 对象，并将其引用（地址）推入栈顶
            // 此时的操作数栈: [日志Tag(String), sb_ref(uninit)]

            mv.visitInsn(DUP) // DUP (Duplicate) 复制栈顶元素
            // 此时的操作数栈: [日志Tag(String), sb_ref(uninit), sb_ref(uninit)]

            mv.visitMethodInsn(
                /* opcode = */ INVOKESPECIAL,            // invoke special 特殊调用，用于构造函数、super 调用和 private 方法
                /* owner = */ "java/lang/StringBuilder", // 拥有该方法的类的内部名称
                /* name = */ "<init>",                   // 构造函数的固定名称
                /* descriptor = */ "()V",                // 描述符。() 表示无参数，V 表示 void 返回
                /* isInterface = */ false                // // 指示 owner（即 java/lang/StringBuilder）是否是一个接口
            )
            // 此时的操作数栈: [日志Tag(String), sb_ref(uninit)]

            mv.visitLdcInsn("$className.$methodName execution time: ") // 加载常量, 将日志消息的前缀（一个 String）推入栈顶
            // 此时的操作数栈: [日志Tag(String), sb_ref(uninit), 日志消息前缀(String)]

            mv.visitMethodInsn(
                /* opcode = */ INVOKEVIRTUAL,                                       // invoke virtual: 虚拟调用，用于所有实例方法
                /* owner = */ "java/lang/StringBuilder",                            // 拥有该方法的类的内部名称
                /* name = */ "append",                                              // 方法名
                /* descriptor = */ "(Ljava/lang/String;)Ljava/lang/StringBuilder;", // 描述符, 参数类型为 String, 返回类型为 StringBuilder
                /* isInterface = */ false                                           // 指示 owner（即 java/lang/StringBuilder）是否是一个接口
            ) // 调用 append(String)。它会消耗栈顶的 日志消息前缀(String) 和 sb_ref(init), 不过它会返回一个 StringBuilder, 然后将结果推入栈顶
            // 此时的操作数栈: [日志Tag(String), sb_ref(uninit)]

            mv.visitVarInsn(LLOAD, durationLocalVarIndex) // 加载耗时
            // 此时的操作数栈: [日志Tag(String), sb_ref(uninit), 耗时(long)]

            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(J)Ljava/lang/StringBuilder;", false)
            // 调用 append(long)。它会消耗栈顶的 耗时(long) 和 sb_ref(init), 不过它仍然返回一个 StringBuilder, 然后将结果推入栈顶
            // 此时的操作数栈: [日志Tag(String), sb_ref(uninit)]

            mv.visitLdcInsn(" ms") // 加载常量, 将日志消息的后缀（一个 String）推入栈顶
            // 此时的操作数栈: [日志Tag(String), sb_ref(uninit), 日志消息后缀(String)]

            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false)
            // 调用 append(String)。它会消耗栈顶的 日志消息后缀(String) 和 sb_ref(init), 不过它仍然返回一个 StringBuilder, 然后将结果推入栈顶
            // 此时的操作数栈: [日志Tag(String), sb_ref(uninit)]

            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;", false)
            // 调用 toString()。它会消耗栈顶的 sb_ref(init), 然后将结果推入栈顶
            // 此时的操作数栈: [日志Tag(String), 日志消息(String)]

            mv.visitMethodInsn(INVOKESTATIC, "android/util/Log", "d", "(Ljava/lang/String;Ljava/lang/String;)I", false)
            // 调用 Log.d(tag, msg)。它会消耗栈顶的 日志消息(String) 和 日志Tag(String), 然后将结果(写入的字节数)推入栈顶
            // 此时的操作数栈: [result(int)]

            // Log.d 返回了一个 int，我们并不关心它。如果不弹出它，这个 int 值会留在栈上，当原始的 RETURN 指令执行时，
            // 会导致栈帧错误 (StackMapFrameError)。POP 用来清理栈，确保栈在我们的代码执行完毕后是干净的
            mv.visitInsn(POP) // Log.d 返回 int，需要弹出
        }
        super.onMethodExit(opcode) // 这行代码的作用是将原始的退出指令（如 RETURN）写回方法中, 不要忘记！！！
    }
}
